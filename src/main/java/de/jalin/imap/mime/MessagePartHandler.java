package de.jalin.imap.mime;

import javax.mail.BodyPart;

import de.jalin.imap.IMAPyException;

public interface MessagePartHandler {

	public void handle(BodyPart part) throws IMAPyException;
	
}
