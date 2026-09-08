package de.jalin.imap.mime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import jakarta.activation.DataHandler;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.util.ByteArrayDataSource;

import de.jalin.imap.IMAPyException;

public class AttachmentStreamerTest {

    private MimeBodyPart bodyPart(String fileName, byte[] data) throws Exception {
        final MimeBodyPart part = new MimeBodyPart();
        part.setDataHandler(new DataHandler(new ByteArrayDataSource(data, "application/octet-stream")));
        if (fileName != null) {
            part.setFileName(fileName);
        }
        return part;
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void streamsMatchingAttachmentOnce() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final AttachmentStreamer streamer = new AttachmentStreamer("b.txt", out);
        streamer.handle(bodyPart("a.txt", bytes("AAA")));
        streamer.handle(bodyPart("b.txt", bytes("BBB")));
        assertArrayEquals(bytes("BBB"), out.toByteArray());
        streamer.handle(bodyPart("b.txt", bytes("CCC")));
        assertArrayEquals(bytes("BBB"), out.toByteArray());
    }

    @Test
    public void matchesSanitizedFileName() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        new AttachmentStreamer("Bericht_2026.pdf", out).handle(bodyPart("Bericht 2026.pdf", bytes("PDF")));
        assertArrayEquals(bytes("PDF"), out.toByteArray());
    }

    @Test
    public void matchesGeneratedNameForMissingFileName() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        new AttachmentStreamer("attachment1", out).handle(bodyPart(null, bytes("Ohne Namen")));
        assertArrayEquals(bytes("Ohne Namen"), out.toByteArray());
    }

    @Test
    public void leavesNonMatchingAttachmentsUntouched() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final AttachmentStreamer streamer = new AttachmentStreamer("c.txt", out);
        streamer.handle(bodyPart("a.txt", bytes("AAA")));
        streamer.handle(bodyPart("b.txt", bytes("BBB")));
        assertEquals(0, out.size());
    }

}
