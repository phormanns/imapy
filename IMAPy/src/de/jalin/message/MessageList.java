package de.jalin.message;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.jalin.javamail.EMailReader;


public class MessageList {

	private long timeStamp;
	private String folderName;
	private String displayName;
	private int folderLevel;
	private int countAllMessages;
	private int countNewMessages;
	private int countUnreadMessages;
	private List<MessageData> idxSentTimestamp;
	private Map<String, MessageData> messages;
	private boolean noMaildir;

	public MessageList(String folderName, String displayName, int level) {
		timeStamp = 0L;
		setFolderName(folderName);
		setDisplayName(displayName);
		setIndentationLevel(level);
		setMessageCount(0, 0, 0);
		noMaildir = false;
		messages = new HashMap<String, MessageData>();
		idxSentTimestamp = new ArrayList<MessageData>();
	}
	
	public void setIndentationLevel(int level) {
		folderLevel = level;
	}

	public void setDisplayName(String display) {
		this.displayName = display;
	}

	public void setMessageCount(int countNew, int countUnread, int countTotal) {
		countNewMessages = countNew;
		countUnreadMessages = countUnread;
		countAllMessages = countTotal;
	}

	public void put(String id, MessageData msg) {
		messages.put(id, msg);
		idxSentTimestamp.add(msg);
		countAllMessages = idxSentTimestamp.size();
	}
	
	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public List<MessageData> getMessagesList() {
		Collections.sort(idxSentTimestamp, new Comparator<MessageData>() {
			public int compare(MessageData msg1, MessageData msg2) {
				try {
					return - EMailReader.df.parse(msg1.getSentTimestamp()).compareTo(EMailReader.df.parse(msg2.getSentTimestamp()));
				} catch (ParseException e) {
					return 0;
				}
			}});
		return idxSentTimestamp;
	}

	public void setMessagesList(List<MessageData> messagesList) {
		this.idxSentTimestamp = messagesList;
	}

	public void setTimeStamp() {
		timeStamp = System.currentTimeMillis();
	}

	public boolean isUpToDate() {
		// max. alle 10 Minuten aktualisieren
		if (timeStamp + 600000L > System.currentTimeMillis()) {
			return true;
		} else {
			setMessageCount(0, 0, 0);
			messages = new HashMap<String, MessageData>();
			idxSentTimestamp = new ArrayList<MessageData>();
			return false;
		}
	}

	public void setNotReadable(boolean b) {
		noMaildir = b;
	}

	public boolean isNotReadable() {
		return noMaildir;
	}

	public int getFolderLevel() {
		return folderLevel;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getCountNewMessages() {
		return countNewMessages;
	}

	public int getCountUnreadMessages() {
		return countUnreadMessages;
	}

	public int getCountAllMessages() {
		return countAllMessages;
	}

}
