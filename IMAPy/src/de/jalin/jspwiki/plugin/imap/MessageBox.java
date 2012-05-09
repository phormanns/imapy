package de.jalin.jspwiki.plugin.imap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;

import de.jalin.message.MessageList;

public class MessageBox {

	private long timeStamp;
	private List<MessageList> folders;
	
	public MessageBox(Store store) {
		timeStamp = 0L;
		try {
			update(store.getDefaultFolder());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<MessageList> getFolders() {
		return folders;
	}

	public void setFolders(List<MessageList> folders) {
		this.folders = folders;
	}

	public void update(Folder inbox) throws MessagingException {
		// max. alle 10 Minuten
		if (timeStamp + 600000L < System.currentTimeMillis()) {
			timeStamp = System.currentTimeMillis();
			folders = new ArrayList<MessageList>();
			try {
				inbox.open(Folder.READ_ONLY);
				MessageList messageList = new MessageList(
						inbox.getFullName(), 
						getFolderDisplayName(inbox.getFullName()), 0);
				messageList.setMessageCount(
						inbox.getNewMessageCount(), 
						inbox.getUnreadMessageCount(),
						inbox.getMessageCount()); 
				folders.add(messageList);
				inbox.close(false);
			} catch (MessagingException e) {
				MessageList messageList = new MessageList(
						inbox.getFullName(), 
						getFolderDisplayName(inbox.getFullName()), 
						0);
				messageList.setNotReadable(true);
				folders.add(messageList);
			}
			listChildren(inbox, 1);
		}
	}

	@SuppressWarnings("unchecked")
	private void listChildren(Folder subFolder, int level) throws MessagingException {
		Folder[] subFolders = subFolder.listSubscribed();
		// Sub-Folder alphabetisch sortieren
		Arrays.sort(subFolders, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return ((Folder) arg0).getFullName().compareTo(((Folder) arg1).getFullName());
			}
		});
		for (int fldrIdx = 0; fldrIdx < subFolders.length; fldrIdx++) {
			try {
				subFolders[fldrIdx].open(Folder.READ_ONLY);
				MessageList messageList = new MessageList(
						subFolders[fldrIdx].getFullName(), 
						getFolderDisplayName(subFolders[fldrIdx].getFullName()), 
						level);
				messageList.setMessageCount(
						subFolders[fldrIdx].getNewMessageCount(), 
						subFolders[fldrIdx].getUnreadMessageCount(),
						subFolders[fldrIdx].getMessageCount()); 
				folders.add(messageList);
				subFolders[fldrIdx].close(false);
			} catch (MessagingException e) {
				MessageList messageList = new MessageList(
						subFolders[fldrIdx].getFullName(), 
						getFolderDisplayName(subFolders[fldrIdx].getFullName()), 
						level);
				messageList.setNotReadable(true);
				folders.add(messageList);
			}
			listChildren(subFolders[fldrIdx], level + 1);
		}
	}

	private String getFolderDisplayName(String fullName) {
		if ("INBOX".equals(fullName)) {
			return "Posteingang";
		}
		if ("INBOX.Trash".equals(fullName)) {
			return "Paperkorb";
		}
		if ("INBOX.Sent".equals(fullName)) {
			return "Gesendet";
		}
		if ("INBOX.Drafts".equals(fullName)) {
			return "Entwürfe";
		}
		if ("INBOX/Trash".equals(fullName)) {
			return "Paperkorb";
		}
		if ("INBOX/Sent".equals(fullName)) {
			return "Gesendet";
		}
		if ("INBOX/Drafts".equals(fullName)) {
			return "Entwürfe";
		}
		return fullName.substring(fullName.lastIndexOf('.') + 1).substring(fullName.lastIndexOf('/') + 1);
	}
}
