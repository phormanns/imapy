package de.jalin.jspwiki.plugin.imap;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

public class IMAPProxy {

	private static Map<String, IMAPMailbox> mailBoxes = new HashMap<String, IMAPMailbox>();
	private static IMAPProxy theProxy = null;
	
	public static IMAPProxy getInstance() {
		if (theProxy == null) {
			theProxy = new IMAPProxy();
		}
		return theProxy;
	}
	
	private IMAPProxy() {
	}

	public Store getStore(String imapHost, String imapUser, String imapPassword) {
		IMAPMailbox mbox = getOrCreateMBox(imapHost, imapUser, imapPassword);
		return mbox.getIMAPStore();
	}

	public MessageBox getMessageBox(String imapHost, String imapUser, String imapPassword) {
		IMAPMailbox mbox = getOrCreateMBox(imapHost, imapUser, imapPassword);
		return mbox.getMessageBox();
	}
 
	class IMAPMailbox {
		
		private Store imapStore;
		private MessageBox messageBox;
		private long timeLastUsage;
		private long timeCreation;
		
		IMAPMailbox() {
			timeCreation = System.currentTimeMillis();
			timeLastUsage = timeCreation;
		}

		public void setMessageBox(MessageBox messageBox2) {
			messageBox = messageBox2;
		}
		
		public MessageBox getMessageBox() {
			return messageBox;
		}

		public void setIMAPStore(Store store) {
			imapStore = store;
		}

		public Store getIMAPStore() {
			timeLastUsage = System.currentTimeMillis();
			return imapStore;
		}

	}

	private IMAPMailbox getOrCreateMBox(String imapHost, String imapUser,
			String imapPassword) {
		IMAPMailbox mbox = mailBoxes.get(imapUser + "@" + imapHost);
		if (mbox == null) {
			try {
				mbox = new IMAPMailbox();
				Session session = Session.getDefaultInstance(new Properties());
				Store store = session.getStore("imap");
				store.connect(imapHost, imapUser, imapPassword);
				mbox.setIMAPStore(store);
				mbox.setMessageBox(new MessageBox(store));
				mailBoxes.put(imapUser + "@" + imapHost, mbox);
			} catch (NoSuchProviderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mbox;
	}

}
