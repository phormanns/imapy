package de.jalin.imap.mime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.SortedMap;

import org.junit.jupiter.api.Test;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;

import de.jalin.imap.IMAPyException;

public class AttachmentsCollectorTest {

    private MimeBodyPart bodyPart(String fileName, String contentType, String content) throws MessagingException {
        final MimeBodyPart part = new MimeBodyPart();
        part.setText(content, "UTF-8");
        if (contentType != null) {
            part.setHeader("Content-Type", contentType);
        }
        if (fileName != null) {
            part.setFileName(fileName);
        }
        return part;
    }

    @Test
    public void startsEmpty() {
        assertTrue(new AttachmentsCollector().getAttachmentsList().isEmpty());
    }

    @Test
    public void collectsFileNameAndContentType() throws IMAPyException, MessagingException {
        final AttachmentsCollector collector = new AttachmentsCollector();
        collector.handle(bodyPart("bericht.pdf", "application/pdf", "Inhalt"));
        final SortedMap<String, String> attachments = collector.getAttachmentsList();
        assertEquals(1, attachments.size());
        assertTrue(attachments.get("bericht.pdf").startsWith("application/pdf"));
    }

    @Test
    public void sanitizesSpecialCharactersInFileName() throws IMAPyException, MessagingException {
        final AttachmentsCollector collector = new AttachmentsCollector();
        collector.handle(bodyPart("Bericht 2026.pdf", "application/pdf", "Inhalt"));
        assertTrue(collector.getAttachmentsList().containsKey("Bericht_2026.pdf"));
    }

    @Test
    public void generatesFallbackNamesForMissingFileNames() throws IMAPyException, MessagingException {
        final AttachmentsCollector collector = new AttachmentsCollector();
        collector.handle(bodyPart(null, "text/plain", "eins"));
        collector.handle(bodyPart(null, "text/plain", "zwei"));
        final SortedMap<String, String> attachments = collector.getAttachmentsList();
        assertTrue(attachments.containsKey("attachment1"));
        assertTrue(attachments.containsKey("attachment2"));
        assertEquals(2, attachments.size());
    }

    @Test
    public void returnsDefensiveCopy() throws IMAPyException, MessagingException {
        final AttachmentsCollector collector = new AttachmentsCollector();
        collector.handle(bodyPart("bericht.pdf", "application/pdf", "Inhalt"));
        collector.getAttachmentsList().put("extra.txt", "text/plain");
        assertFalse(collector.getAttachmentsList().containsKey("extra.txt"));
        assertEquals(1, collector.getAttachmentsList().size());
    }

}
