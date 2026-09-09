package de.jalin.imap;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import jakarta.mail.FetchProfile;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.FolderClosedException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.StoreClosedException;
import jakarta.mail.UIDFolder;
import jakarta.mail.internet.MimeMessage;

import de.jalin.imap.mime.MessageData;
import de.jalin.imap.mime.MessagePartHandler;
import de.jalin.imap.mime.MimeParser;
import de.jalin.imap.text.HtmlHelper;

public class IMAPySession {

    public static final String NEW = "unread";
    public static final String SEEN = "seen";

    final static private DateTimeFormatter DF = DateTimeFormatter.ofPattern("EEE dd.MM.yyyy  HH:mm", Locale.GERMANY);

    final private String user;
    final private char[] password;
    final private String host;
    final private Store store;
    final private SortedMap<String, String> folders;

    public IMAPySession(String host, String user, String password) throws IMAPyException {
        this(defaultStore(), host, user, password);
    }

    IMAPySession(final Store store, final String host, final String user, final String password) throws IMAPyException {
        this.user = user;
        this.password = password.toCharArray();
        this.host = host;
        this.folders = new TreeMap<>();
        this.store = store;
        ensureConnected();
        try {
            refreshFolders();
        } catch (MessagingException e) {
            throw new IMAPyException(e);
        }
    }

    private static Store defaultStore() throws IMAPyException {
        try {
            final Session session = Session.getInstance(new Properties());
            return session.getStore("imaps");
        } catch (NoSuchProviderException e) {
            throw new IMAPyException(e);
        }
    }

    public synchronized void disconnect() {
        try {
            if (store.isConnected()) {
                store.close();
            }
        } catch (MessagingException e) {
        }
    }

    private static final List<String> PRIORITY_FOLDERS = List.of("INBOX", "Sent", "Drafts", "Junk", "Trash");

    public List<IMAPyFolder> getFolders() throws IMAPyException {
        ensureConnected();
        final List<IMAPyFolder> fdList = new ArrayList<>();
        for (final String fdName : folders.keySet()) {
            final IMAPyFolder yFolder = new IMAPyFolder();
            yFolder.setName(fdName);
            yFolder.setTitle(folders.get(fdName));
            try {
                final Folder fd = store.getFolder(fdName);
                yFolder.setTotalMessageCount(fd.getMessageCount());
                yFolder.setUnreadMessageCount(fd.getUnreadMessageCount());
                yFolder.setNewMessageCount(fd.getNewMessageCount());
            } catch (MessagingException e) {
                yFolder.setNewMessageCount(-999);
                yFolder.setUnreadMessageCount(-999);
                yFolder.setTotalMessageCount(-999);
            }
            fdList.add(yFolder);
        }
        fdList.sort(comparingFolderPriority());
        return fdList;
    }

    private static Comparator<IMAPyFolder> comparingFolderPriority() {
        return Comparator.comparingInt((IMAPyFolder folder) -> priority(folder.getName()))
                .thenComparingInt((IMAPyFolder folder) -> isTopLevel(folder.getName()) ? 0 : 1)
                .thenComparing(IMAPyFolder::getName);
    }

    private static boolean isTopLevel(final String name) {
        return name.indexOf('/') < 0 && name.indexOf('.') < 0;
    }

    private static int priority(final String name) {
        final String normalized = lastSegment(name).toUpperCase(Locale.ROOT);
        for (int i = 0; i < PRIORITY_FOLDERS.size(); i++) {
            if (PRIORITY_FOLDERS.get(i).toUpperCase(Locale.ROOT).equals(normalized)) {
                return i;
            }
        }
        return PRIORITY_FOLDERS.size();
    }

    private static String lastSegment(final String name) {
        int idx = Math.max(name.lastIndexOf('/'), name.lastIndexOf('.'));
        return idx < 0 ? name : name.substring(idx + 1);
    }

    public List<IMAPyMessage> getMessages(final String folderName) throws IMAPyException {
        return onFolder(folderName, Folder.READ_ONLY, false, folder -> {
            final List<IMAPyMessage> yMessages = new ArrayList<>();
            final Message[] messages = folder.getMessages();
            final FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.CONTENT_INFO);
            fp.add(FetchProfile.Item.FLAGS);
            fp.add(UIDFolder.FetchProfileItem.UID);
            folder.fetch(messages, fp);
            for (final Message msg : messages) {
                final IMAPyMessage yMsg = new IMAPyMessage();
                yMsg.setUid(((UIDFolder) folder).getUID(msg));
                yMsg.setTitle(shorten(msg));
                yMsg.setAuthor(MimeParser.getFromAddress(msg));
                yMsg.setFolder(folderName);
                yMsg.setStatus(msg.isSet(Flag.SEEN) ? SEEN : NEW);
                final Date sentDate = msg.getSentDate();
                if (sentDate != null) {
                    yMsg.setDate(formatDate(sentDate));
                }
                yMessages.add(0, yMsg);
            }
            return yMessages;
        });
    }

    public IMAPyMessage getMessage(final String folderName, final String uid, final MessagePartHandler partHandler) throws IMAPyException {
        final long uidValue = parseUid(uid);
        return onFolder(folderName, Folder.READ_WRITE, false, folder -> {
            final Message msg = ((UIDFolder) folder).getMessageByUID(uidValue);
            if (msg == null) {
                throw new IMAPyException("Nachricht mit UID " + uidValue + " nicht gefunden in Ordner " + folderName);
            }
            final IMAPyMessage yMsg = new IMAPyMessage();
            yMsg.setFolder(folderName);
            yMsg.setUid(uidValue);
            final Date sentDate = msg.getSentDate();
            if (sentDate != null) {
                yMsg.setDate(formatDate(sentDate));
            } else {
                yMsg.setDate(formatDate(new Date()));
            }
            yMsg.setTitle(shorten(msg));
            yMsg.setAuthor(MimeParser.getFromAddress(msg));
            yMsg.setSubject(shorten(msg));
            yMsg.setFrom(MimeParser.getFromAddress(msg));
            yMsg.setTo(MimeParser.getToAddress(msg));
            yMsg.setStatus(msg.isSet(Flag.SEEN) ? SEEN : NEW);
            if (msg instanceof MimeMessage) {
                final MessageData messageData = MimeParser.parseMimeMessage((MimeMessage) msg, partHandler);
                yMsg.setContent(messageData.getFormattedText());
                yMsg.setMessageId(messageData.getMessageID());
                yMsg.setReferences(messageData.getReferences());
            } else {
                throw new IMAPyException("unknown message type");
            }
            msg.setFlag(Flag.SEEN, true);
            return yMsg;
        });
    }

    public IMAPyMessage removeMessage(final String folderName, final String uid, final String messageId) throws IMAPyException {
        final long uidValue = parseUid(uid);
        final IMAPyMessage yMsg = new IMAPyMessage();
        yMsg.setFolder(folderName);
        yMsg.setUid(uidValue);
        return onFolder(folderName, Folder.READ_WRITE, true, folder -> {
            final Message msg = ((UIDFolder) folder).getMessageByUID(uidValue);
            if (msg == null) {
                throw new IMAPyException("Nachricht mit UID " + uidValue + " nicht gefunden in Ordner " + folderName);
            }
            String messageIDtoCheck = null;
            if (msg instanceof MimeMessage) {
                messageIDtoCheck = MimeParser.getMessageID((MimeMessage) msg);
            }
            final boolean messageIdChecked = (messageIDtoCheck != null && messageIDtoCheck.equals(messageId)) || (messageId == null && messageIDtoCheck == null);
            if (messageIdChecked) {
                msg.setFlag(Flag.DELETED, true);
            }
            return yMsg;
        });
    }

    public int removeMessages(final String folderName, final List<Long> uids) throws IMAPyException {
        return onFolder(folderName, Folder.READ_WRITE, true, folder -> {
            int removed = 0;
            for (final long uidValue : uids) {
                final Message msg = ((UIDFolder) folder).getMessageByUID(uidValue);
                if (msg != null) {
                    msg.setFlag(Flag.DELETED, true);
                    removed++;
                }
            }
            return removed;
        });
    }

    public IMAPyMessage moveMessageToFolder(final String sourceFolderName, final String uid, final String targetFolderName) throws IMAPyException {
        final long uidValue = parseUid(uid);
        return withRetry(() -> {
            final Folder srcFolder = getStoreFolder(sourceFolderName);
            final Folder targetFolder = getStoreFolder(targetFolderName);
            try {
                srcFolder.open(Folder.READ_WRITE);
                final Message msg = ((UIDFolder) srcFolder).getMessageByUID(uidValue);
                if (msg == null) {
                    throw new IMAPyException("Nachricht mit UID " + uidValue + " nicht gefunden in Ordner " + sourceFolderName);
                }
                targetFolder.open(Folder.READ_WRITE);
                try {
                    srcFolder.copyMessages(new Message[]{msg}, targetFolder);
                    msg.setFlag(Flag.DELETED, true);
                } finally {
                    closeQuietly(targetFolder, false);
                }
            } finally {
                closeQuietly(srcFolder, true);
            }
            final IMAPyMessage yMsg = new IMAPyMessage();
            yMsg.setFolder(targetFolderName);
            yMsg.setUid(uidValue);
            return yMsg;
        });
    }

    private synchronized void ensureConnected() throws IMAPyException {
        try {
            if (!store.isConnected()) {
                store.connect(host, user, new String(password));
            }
        } catch (MessagingException e) {
            throw new IMAPyException(e);
        }
    }

    private <T> T onFolder(final String folderName, final int mode, final boolean expunge, final FolderOperation<T> op) throws IMAPyException {
        return withRetry(() -> {
            final Folder folder = getStoreFolder(folderName);
            try {
                folder.open(mode);
                return op.run(folder);
            } finally {
                closeQuietly(folder, expunge);
            }
        });
    }

    private <T> T withRetry(final StoreOperation<T> op) throws IMAPyException {
        try {
            ensureConnected();
            return op.run();
        } catch (final MessagingException | IMAPyException e) {
            if (!isConnectionFailure(e)) {
                throw asImapy(e);
            }
            try {
                ensureConnected();
            } catch (final IMAPyException connectError) {
                throw asImapy(e);
            }
            try {
                return op.run();
            } catch (final MessagingException | IMAPyException e2) {
                throw asImapy(e2);
            }
        }
    }

    private Folder getStoreFolder(final String folderName) throws IMAPyException {
        if (!folders.containsKey(folderName)) {
            throw new IMAPyException("Ordner nicht gefunden: " + folderName);
        }
        try {
            return store.getFolder(folderName);
        } catch (MessagingException e) {
            throw new IMAPyException(e);
        }
    }

    private void refreshFolders() throws MessagingException {
        folders.clear();
        collectFolders(store.getDefaultFolder());
        folders.putIfAbsent("INBOX", "INBOX");
    }

    private void collectFolders(final Folder parent) throws MessagingException {
        final Folder[] children = parent.listSubscribed();
        for (final Folder child : children) {
            final int type = child.getType();
            final String fullName = child.getFullName();
            if (("INBOX".equalsIgnoreCase(fullName)) || (type & Folder.HOLDS_MESSAGES) > 0) {
                folders.put(fullName, child.getName());
            }
            if ((type & Folder.HOLDS_FOLDERS) > 0) {
                collectFolders(child);
            }
        }
    }

    private static void closeQuietly(final Folder folder, final boolean expunge) {
        try {
            if (folder.isOpen()) {
                folder.close(expunge);
            }
        } catch (MessagingException e) {
        }
    }

    private static boolean isConnectionFailure(final Throwable t) {
        Throwable cause = t;
        while (cause != null) {
            if (cause instanceof FolderClosedException
                    || cause instanceof StoreClosedException
                    || cause instanceof IllegalStateException
                    || cause instanceof IOException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private static IMAPyException asImapy(final Exception e) {
        if (e instanceof IMAPyException) {
            return (IMAPyException) e;
        }
        return new IMAPyException(e);
    }

    private static long parseUid(final String uid) throws IMAPyException {
        try {
            return Long.parseLong(uid);
        } catch (NumberFormatException e) {
            throw new IMAPyException(e);
        }
    }

    private static String formatDate(final Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).format(DF);
    }

    private String shorten(final Message msg) {
        final String subject = MimeParser.getSubject(msg);
        if (subject.length() > 80) {
            return subject.substring(0, 79);
        }
        return HtmlHelper.replaceEntities(subject);
    }

    @FunctionalInterface
    private interface StoreOperation<T> {
        T run() throws MessagingException, IMAPyException;
    }

    @FunctionalInterface
    private interface FolderOperation<T> {
        T run(Folder folder) throws MessagingException, IMAPyException;
    }

}
