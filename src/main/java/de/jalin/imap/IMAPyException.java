package de.jalin.imap;

public class IMAPyException extends Exception {

	private static final long serialVersionUID = 1L;

	public IMAPyException(Exception e) {
		super(e);
	}

	public IMAPyException(String message) {
		super(message);
	}

}
