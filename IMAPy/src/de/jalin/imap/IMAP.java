package de.jalin.imap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
	
	public List<Map<String, String>> getFolders() {
		final List<Map<String, String>> fdList = new ArrayList<Map<String, String>>();
		for (final String fdName : folders.keySet()) {
			final Folder fd = folders.get(fdName);
			final Map<String,String> map = new HashMap<String, String>();
			map.put("folder", fdName);
			map.put("title", fd.getName());
			try {
				final int messageCount = fd.getMessageCount();
				final int unreadMessageCount = fd.getUnreadMessageCount();
				final int newMessageCount = fd.getNewMessageCount();
				map.put("cssclass", (unreadMessageCount > 0) ? "foldernewmsgs": "folderread");
				map.put("ntotal", Integer.toString(messageCount));
				map.put("nunread", Integer.toString(unreadMessageCount));
				map.put("nunseen", Integer.toString(newMessageCount));
			} catch (MessagingException e) {
				map.put("ntotal", "?");
				map.put("nunread", "?");
				map.put("nunseen", "?");
			}
			fdList.add(map);
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
			final Message msg = folder.getMessage(Integer.parseInt(msgId));
			item.put("folder", folderName);
			item.put("idx", msgId);
			item.put("date", DF.format(msg.getSentDate()));
			item.put("title", shorten(msg));
			item.put("author", MimeParser.getFromAddress(msg));
			item.put("subject", shorten(msg));
			item.put("from", MimeParser.getFromAddress(msg));
			item.put("to", MimeParser.getToAddress(msg));
			if (msg instanceof MimeMessage) {
				final MessageData messageData = MimeParser.parseMimeMessage((MimeMessage) msg);
				item.put("content", "<pre>" + messageData.getFormattedText() + "</pre>");
			}
			msg.setFlag(Flag.SEEN, true);
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
