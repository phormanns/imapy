package de.jalin.webmail;

import de.jalin.imap.IMAPyException;

public interface MailboxFinder {

	public void setLogin(String login, String password) throws IMAPyException;
	
	public String getHost();
	
	public String getUser();
	
	public String getPassword();
	
}
