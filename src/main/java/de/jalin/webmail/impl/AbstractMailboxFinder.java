package de.jalin.webmail.impl;

import de.jalin.webmail.MailboxFinder;

public abstract class AbstractMailboxFinder implements MailboxFinder {

	protected String host;
	protected String user;
	protected String password;
	
	@Override
	public String getHost() {
		return host;
	}

	@Override
	public String getUser() {
		return user;
	}

	@Override
	public String getPassword() {
		return password;
	}

}
