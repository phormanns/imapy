package de.jalin.imap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.mail.FetchProfile;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import de.jalin.imap.mime.MessageData;
import de.jalin.imap.mime.MimeParser;

public class IMAPySession {

	public static final String NEW = "new";
	public static final String SEEN = "seen";

	final static private DateFormat DF = new SimpleDateFormat("EEE dd.MM.yyyy  HH:mm");
	
	final private String user;
	final private String password;
	final private String host;
	final private SortedMap<String, Folder> folders;
	
	public IMAPySession(String host, String user, String password) throws IMAPyException {
		this.user = user;
		this.password = password;
		this.host = host;
		folders = new TreeMap<String, Folder>();
		initFolders();
	}
	
	public List<IMAPyFolder> getFolders() {
		final List<IMAPyFolder> fdList = new ArrayList<>();
		for (final String fdName : folders.keySet()) {
			final Folder fd = folders.get(fdName);
			final IMAPyFolder yFolder = new IMAPyFolder();
			yFolder.setName(fdName);
			yFolder.setTitle(fd.getName());
			try {
				final int messageCount = fd.getMessageCount();
				final int unreadMessageCount = fd.getUnreadMessageCount();
				final int newMessageCount = fd.getNewMessageCount();
				yFolder.setNewMessageCount(newMessageCount);
				yFolder.setUnreadMessageCount(unreadMessageCount);
				yFolder.setTotalMessageCount(messageCount);
			} catch (MessagingException e) {
				yFolder.setNewMessageCount(-999);
				yFolder.setUnreadMessageCount(-999);
				yFolder.setTotalMessageCount(-999);
			}
			fdList.add(yFolder);
		}
		return fdList;
	}
	
	public List<IMAPyMessage> getMessages(final String folderName) throws IMAPyException {
		final List<IMAPyMessage> yMessages = new ArrayList<>();
		try {
			final Folder folder = folders.get(folderName);
			if (!folder.isOpen()) {
				folder.open(Folder.READ_ONLY);
			}
			final Message[] messages = folder.getMessages();
			final FetchProfile fp = new FetchProfile();
			fp.add(FetchProfile.Item.ENVELOPE);
			fp.add(FetchProfile.Item.CONTENT_INFO);
			fp.add(FetchProfile.Item.FLAGS);
			folder.fetch(messages, fp);
			folder.close(false);
			for (Message msg : messages) {
				final IMAPyMessage yMsg = new IMAPyMessage();
				yMsg.setIndex(msg.getMessageNumber());
				yMsg.setTitle(shorten(msg));
				yMsg.setAuthor(MimeParser.getFromAddress(msg));
				yMsg.setFolder(folderName);
				yMsg.setStatus(msg.isSet(Flag.SEEN) ? SEEN : NEW);
				yMessages.add(0, yMsg);
			}
		} catch (MessagingException e) {
			throw new IMAPyException(e);
		}
		return yMessages;
	}

	private String shorten(final Message msg) {
		final String subject = MimeParser.getSubject(msg);
		if (subject.length() > 80) {
			return subject.substring(0, 79);
		}
		return subject;
	}
	
	public IMAPyMessage getMessage(final String folderName, final String msgId) throws IMAPyException {
		final IMAPyMessage yMsg = new IMAPyMessage(); 
		try {
			final Folder folder = folders.get(folderName);
			folder.open(Folder.READ_WRITE);
			final int messageCount = folder.getMessageCount();
			int msgIndx = Integer.parseInt(msgId);
			if (msgIndx > messageCount) {
				msgIndx = messageCount - 1;
			}
			final Message msg = folder.getMessage(msgIndx);
			yMsg.setFolder(folderName);
			yMsg.setIndex(msgIndx);
			final Date sentDate = msg.getSentDate();
			if (sentDate != null) {
				yMsg.setDate(DF.format(sentDate));
			} else {
				yMsg.setDate(DF.format(new Date()));
			}
			yMsg.setTitle(shorten(msg));
			yMsg.setAuthor(MimeParser.getFromAddress(msg));
			yMsg.setSubject(shorten(msg));
			yMsg.setFrom(MimeParser.getFromAddress(msg));
			yMsg.setTo(MimeParser.getToAddress(msg));
			yMsg.setStatus(msg.isSet(Flag.SEEN) ? SEEN : NEW);
			if (msg instanceof MimeMessage) {
				final MessageData messageData = MimeParser.parseMimeMessage((MimeMessage) msg);
				yMsg.addAttachments(messageData.getAttachmentsHash());
				yMsg.setContent(messageData.getFormattedText());
				yMsg.setMessageId(messageData.getMessageID());
			} else {
				throw new IMAPyException("unknown message type");
			}
			msg.setFlag(Flag.SEEN, true);
			folder.close(true);
		} catch (MessagingException e) {
			throw new IMAPyException(e);
		}
		return yMsg;
	}

	public IMAPyMessage removeMessage(final String folderName, final String msgId, final String messageId) throws IMAPyException {
		final IMAPyMessage yMsg = new IMAPyMessage(); 
		try {
			final Folder folder = folders.get(folderName);
			folder.open(Folder.READ_WRITE);
			int msgIndx = Integer.parseInt(msgId);
			final Message msg = folder.getMessage(msgIndx);
			String messageIDtoCheck = null;
			if (msg instanceof MimeMessage) {
				messageIDtoCheck = MimeParser.getMessageID((MimeMessage) msg);
			}
			yMsg.setFolder(folderName);
			yMsg.setIndex(msgIndx);
			final boolean messageIdChecked = (messageIDtoCheck != null && messageIDtoCheck.equals(messageId)) || (messageId == null && messageIDtoCheck == null);
			if (messageIdChecked) {
				msg.setFlag(Flag.DELETED, true);
			}
			folder.close(true);
		} catch (MessagingException e) {
			throw new IMAPyException(e);
		}
		return yMsg;
	}

	private void initFolders() throws IMAPyException {
		try {
			final Session session = Session.getDefaultInstance(new Properties());
			final Store store = session.getStore("imaps");
			store.connect(host, user, password);
			getChildren(store.getDefaultFolder());
			store.close();
		} catch (NoSuchProviderException e) {
			throw new IMAPyException(e);
		} catch (MessagingException e) {
			throw new IMAPyException(e);
		}
	}

	private void getChildren(final Folder parent) throws MessagingException {
		final Folder[] children = parent.listSubscribed();
		for (final Folder child : children) {
			final int type = child.getType();
			if ((type & Folder.HOLDS_MESSAGES) > 0) {
				folders.put(child.getFullName(), child);
			}
			if ((type & Folder.HOLDS_FOLDERS) > 0) {
				getChildren(child);
			}
		}
	}
	
}
