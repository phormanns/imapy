package de.jalin.imap;

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

	private String user;
	private String password;
	private String host;
	private SortedMap<String, Folder> folders;
	
	public IMAP(String host, String user, String password) throws IMAPyException {
		this.user = user;
		this.password = password;
		this.host = host;
		folders = new TreeMap<String, Folder>();
		initFolders();
	}
	
	public List<Map<String, String>> getFolders() {
		List<Map<String, String>> fdList = new ArrayList<Map<String, String>>();
		for (String fdName : folders.keySet()) {
			Folder fd = folders.get(fdName);
			Map<String,String> map = new HashMap<String, String>();
			map.put("folder", fdName);
			map.put("title", fd.getName());
			try {
				map.put("ntotal", Integer.toString(fd.getMessageCount()));
				map.put("nunread", Integer.toString(fd.getUnreadMessageCount()));
				map.put("nunseen", Integer.toString(fd.getNewMessageCount()));
			} catch (MessagingException e) {
				map.put("ntotal", "?");
				map.put("nunread", "?");
				map.put("nunseen", "?");
			}
			fdList.add(map);
		}
		return fdList;
	}
	
	public List<Map<String, String>> getMessages(String folderName) throws IMAPyException {
		List<Map<String, String>> msgList = new ArrayList<Map<String, String>>();
		try {
			Folder folder = folders.get(folderName);
			if (!folder.isOpen()) {
				folder.open(Folder.READ_ONLY);
			}
			Message[] messages = folder.getMessages();
			FetchProfile fp = new FetchProfile();
			fp.add(FetchProfile.Item.ENVELOPE);
			fp.add(FetchProfile.Item.CONTENT_INFO);
			fp.add(FetchProfile.Item.FLAGS);
			folder.fetch(messages, fp);
			folder.close(false);
			for (Message msg : messages) {
				Map<String,String> map = new HashMap<String, String>();
				map.put("idx", Integer.toString(msg.getMessageNumber()));
				map.put("title", MimeParser.getSubject(msg));
				map.put("author", MimeParser.getFromAddress(msg));
				map.put("folder", folderName);
				map.put("status", msg.isSet(Flag.SEEN) ? "seen" : "new");
				msgList.add(map);
			}
		} catch (MessagingException e) {
			throw new IMAPyException(e);
		}
		return msgList;
	}
	
	public Map<String, String> getMessage(String folderName, String msgId) throws IMAPyException {
		Map<String, String> item = new HashMap<String, String>();
		try {
			Folder folder = folders.get(folderName);
			folder.open(Folder.READ_WRITE);
			Message msg = folder.getMessage(Integer.parseInt(msgId));
			item.put("folder", folderName);
			item.put("idx", msgId);
			item.put("title", MimeParser.getSubject(msg));
			item.put("author", MimeParser.getFromAddress(msg));
			if (msg instanceof MimeMessage) {
				MessageData messageData = MimeParser.parseMimeMessage((MimeMessage) msg);
				item.put("content", messageData.getFormattedText());
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
			Session session = Session.getDefaultInstance(new Properties());
			Store store = session.getStore("imaps");
			store.connect(host, user, password);
			getChildren(store.getDefaultFolder());
			store.close();
		} catch (NoSuchProviderException e) {
			throw new IMAPyException(e);
		} catch (MessagingException e) {
			throw new IMAPyException(e);
		}
	}

	private void getChildren(Folder parent) throws MessagingException {
		Folder[] children = parent.listSubscribed();
		for (Folder child : children) {
			int type = child.getType();
			if ((type & Folder.HOLDS_MESSAGES) > 0) {
				folders.put(child.getFullName(), child);
			}
			if ((type & Folder.HOLDS_FOLDERS) > 0) {
				getChildren(child);
			}
		}
	}
	
}
