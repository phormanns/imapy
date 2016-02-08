package de.jalin.imapy;

import javax.mail.MessagingException;

public class IMAPyException extends Exception {

	private static final long serialVersionUID = 1L;

	public IMAPyException(MessagingException e) {
		super(e);
	}

}
