package de.jalin.imap.mime;

import java.util.SortedMap;
import java.util.TreeMap;

import javax.mail.BodyPart;
import javax.mail.MessagingException;

import de.jalin.imap.IMAPyException;

public class AttachmentsCollector implements MessagePartHandler {

	private final SortedMap<String, String> attachmentsList;
	
	private int counter;

	public AttachmentsCollector() {
		attachmentsList = new TreeMap<>();
		counter = 0;
	}
	
	public SortedMap<String, String> getAttachmentsList() {
		final TreeMap<String, String> copiedMap = new TreeMap<>();
		copiedMap.putAll(attachmentsList);
		return copiedMap;
	}

	@Override
	public void handle(final BodyPart part) throws IMAPyException {
		try {
			counter++;
			final String contentType = part.getContentType();
			String fileName = part.getFileName();
			if (fileName == null || fileName.isEmpty()) {
				fileName = "attachment" + counter;
			} else {
				fileName = fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
			}
			attachmentsList.put(fileName, contentType);
		} catch (MessagingException e) {
			throw new IMAPyException(e);
		}
	}

}
