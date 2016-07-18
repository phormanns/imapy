package de.jalin.imap.mime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.mail.BodyPart;
import javax.mail.MessagingException;

import de.jalin.imap.IMAPyException;

public class AttachmentStreamer implements MessagePartHandler {

	final private String attachmentName;
	final private OutputStream outputStream;
	
	private int counter;
	private boolean outputStreamIsOpen;
	
	public AttachmentStreamer(final String attachmentName, final OutputStream outputStream) {
		this.attachmentName = attachmentName;
		this.outputStream = outputStream;
		outputStreamIsOpen = true;
		counter = 0;
	}
	
	@Override
	public void handle(final BodyPart part) throws IMAPyException {
		try {
			counter++;
			String fileName = part.getFileName().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
			if (fileName == null || fileName.isEmpty()) {
				fileName = "attachment" + counter;
			}
			if (attachmentName.equals(fileName) && outputStreamIsOpen) {
				final InputStream inputStream = part.getInputStream();
				final byte[] buffer = new byte[4096];
				int read = inputStream.read(buffer, 0, 4096);
				while (read > 0) {
					outputStream.write(buffer, 0, read);
					read = inputStream.read(buffer, 0, 4096);
				}
				inputStream.close();
				outputStream.close();
				outputStreamIsOpen = false;
			}
		} catch (MessagingException | IOException e) {
			throw new IMAPyException(e);
		}
	}

}
