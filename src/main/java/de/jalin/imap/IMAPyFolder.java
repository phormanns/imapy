package de.jalin.imap;

import java.io.Serializable;

public class IMAPyFolder implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private String title;
	private int totalMessageCount;
	private int newMessageCount;
	private int unreadMessageCount;


	public int getTotalMessageCount() {
		return totalMessageCount;
	}

	public void setTotalMessageCount(int totalMessageCount) {
		this.totalMessageCount = totalMessageCount;
	}

	public int getNewMessageCount() {
		return newMessageCount;
	}

	public void setNewMessageCount(int newMessageCount) {
		this.newMessageCount = newMessageCount;
	}

	public int getUnreadMessageCount() {
		return unreadMessageCount;
	}

	public void setUnreadMessageCount(int unreadMessageCount) {
		this.unreadMessageCount = unreadMessageCount;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name= name;
	}

}
