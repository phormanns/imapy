package de.jalin.imap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import de.jalin.imapy.IMAPyException;

public class IMAP {

	final static private DateFormat DF = new SimpleDateFormat("EEE dd.MM.yyyy  HH:mm");
	
	final private String user;
	final private String password;
	final private String host;
	final private SortedMap<String, Folder> folders;
	
	public IMAP(String host, String user, String password) throws IMAPyException {
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
	
	public List<Map<String, String>> getMessages(final String folderName) throws IMAPyException {
		final List<Map<String, String>> msgList = new ArrayList<Map<String, String>>();
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
				final Map<String,String> map = new HashMap<String, String>();
				map.put("idx", Integer.toString(msg.getMessageNumber()));
				map.put("title", shorten(msg));
				map.put("author", MimeParser.getFromAddress(msg));
				map.put("folder", folderName);
				map.put("status", msg.isSet(Flag.SEEN) ? "seen" : "new");
				msgList.add(0, map);
			}
		} catch (MessagingException e) {
			throw new IMAPyException(e);
		}
		return msgList;
	}

	private String shorten(final Message msg) {
		final String subject = MimeParser.getSubject(msg);
		if (subject.length() > 80) {
			return subject.substring(0, 79);
		}
		return subject;
	}
	
	public Map<String, String> getMessage(final String folderName, final String msgId) throws IMAPyException {
		final Map<String, String> item = new HashMap<String, String>();
		try {
			final Folder folder = folders.get(folderName);
			folder.open(Folder.READ_WRITE);
			final int messageCount = folder.getMessageCount();
			int msgIndx = Integer.parseInt(msgId);
			if (msgIndx > messageCount) {
				msgIndx = messageCount - 1;
			}
			final Message msg = folder.getMessage(msgIndx);
			item.put("folder", folderName);
			item.put("idx", msgId);
			final Date sentDate = msg.getSentDate();
			if (sentDate != null) {
				item.put("date", DF.format(sentDate));
			} else {
				item.put("date", DF.format(new Date()));
			}
			item.put("title", shorten(msg));
			item.put("author", MimeParser.getFromAddress(msg));
			item.put("subject", shorten(msg));
			item.put("from", MimeParser.getFromAddress(msg));
			item.put("to", MimeParser.getToAddress(msg));
			item.put("status", msg.isSet(Flag.SEEN) ? "seen" : "new");
			if (msg instanceof MimeMessage) {
				final MessageData messageData = MimeParser.parseMimeMessage((MimeMessage) msg);
				item.put("content", messageData.getFormattedText());
				item.put("message-id", messageData.getMessageID());
			}
			msg.setFlag(Flag.SEEN, true);
			folder.close(true);
		} catch (MessagingException e) {
			throw new IMAPyException(e);
		}
		return item;
	}

	public Map<String, String> removeMessage(final String folderName, final String msgId, final String messageId) throws IMAPyException {
		final Map<String, String> item = new HashMap<String, String>();
		try {
			final Folder folder = folders.get(folderName);
			folder.open(Folder.READ_WRITE);
			final Message msg = folder.getMessage(Integer.parseInt(msgId));
			String messageIDtoCheck = null;
			if (msg instanceof MimeMessage) {
				messageIDtoCheck = MimeParser.getMessageID((MimeMessage) msg);
			}
			item.put("folder", folderName);
			item.put("idx", msgId);
			final boolean messageIdChecked = (messageIDtoCheck != null && messageIDtoCheck.equals(messageId)) || (messageId == null && messageIDtoCheck == null);
			if (messageIdChecked) {
				msg.setFlag(Flag.DELETED, true);
			}
			folder.close(true);
		} catch (MessagingException e) {
			throw new IMAPyException(e);
		}
		return item;
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
