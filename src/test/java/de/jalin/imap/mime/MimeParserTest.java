package de.jalin.imap.mime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import jakarta.mail.Flags;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

public class MimeParserTest {

    private MimeMessage fixture(String name) throws Exception {
        try (InputStream in = MimeParserTest.class.getResourceAsStream(name)) {
            assertNotNull(in, "Fixture fehlt: " + name);
            return new MimeMessage(Session.getInstance(new Properties()), in);
        }
    }

    private MimeMessage rawMessage(String eml) throws Exception {
        return new MimeMessage(Session.getInstance(new Properties()),
                new ByteArrayInputStream(eml.getBytes(StandardCharsets.UTF_8)));
    }

    private MimeMessage messageWithContent(final Object messageContent, final String contentTypeValue) throws Exception {
        return new MimeMessage(fixture("plain-text.eml")) {
            @Override
            public Object getContent() {
                return messageContent;
            }

            @Override
            public String getContentType() {
                return contentTypeValue;
            }
        };
    }

    @Test
    public void readsFromAddressLowercased() throws Exception {
        assertEquals("alice@example.org", MimeParser.getFromAddress(fixture("plain-text.eml")));
    }

    @Test
    public void usesFallbackWhenSenderMissing() throws Exception {
        assertEquals("Unbekannter Absender", MimeParser.getFromAddress(fixture("no-from.eml")));
    }

    @Test
    public void readsFirstRecipient() throws Exception {
        assertEquals("bob@example.org", MimeParser.getToAddress(fixture("plain-text.eml")));
    }

    @Test
    public void usesFallbackWhenRecipientsMissing() throws Exception {
        assertEquals("Unbekannt", MimeParser.getToAddress(
                rawMessage("From: alice@example.org\nSubject: Ohne Empfaenger\n\nInhalt\n")));
    }

    @Test
    public void readsSubject() throws Exception {
        assertEquals("Einfacher Text", MimeParser.getSubject(fixture("plain-text.eml")));
    }

    @Test
    public void usesFallbackWhenSubjectMissing() throws Exception {
        assertEquals("(kein Betreff)", MimeParser.getSubject(rawMessage("From: alice@example.org\n\nInhalt\n")));
    }

    @Test
    public void stripsAngleBracketsFromMessageId() throws Exception {
        assertEquals("plain-1@example.org", MimeParser.getMessageID(fixture("plain-text.eml")));
    }

    @Test
    public void generatesFallbackMessageIdWhenMissing() throws Exception {
        final String messageId = MimeParser.getMessageID(fixture("no-from.eml"));
        assertNotNull(messageId);
        assertFalse(messageId.isBlank());
        assertTrue(messageId.contains("@"));
    }

    @Test
    public void parsesSentDate() throws Exception {
        assertEquals(Instant.parse("2026-09-03T08:15:00Z"),
                MimeParser.getSentDate(fixture("plain-text.eml")).toInstant());
    }

    @Test
    public void usesCurrentDateWhenSentDateMissing() throws Exception {
        final Date before = new Date();
        assertFalse(MimeParser.getSentDate(fixture("no-from.eml")).before(before));
    }

    @Test
    public void parsesPlainTextBody() throws Exception {
        final MessageData msg = MimeParser.parseMimeMessage(fixture("plain-text.eml"), new AttachmentsCollector());
        assertTrue(msg.getFormattedText().contains("Grüße, Alice"));
        assertTrue(msg.isNew());
        assertFalse(msg.isFlagged());
    }

    @Test
    public void keepsReferencesHeader() throws Exception {
        final MessageData msg = MimeParser.parseMimeMessage(fixture("multipart-mixed.eml"), new AttachmentsCollector());
        assertEquals("<ref-1@example.org> <ref-2@example.org>", msg.getReferences());
    }

    @Test
    public void returnsNullWhenReferencesMissing() throws Exception {
        assertNull(MimeParser.parseMimeMessage(fixture("plain-text.eml"), new AttachmentsCollector()).getReferences());
    }

    @Test
    public void cleansHtmlBodyAndRemovesScript() throws Exception {
        final MessageData msg = MimeParser.parseMimeMessage(fixture("html.eml"), new AttachmentsCollector());
        final String formatted = msg.getFormattedText();
        assertTrue(formatted.contains("Hallo Welt"));
        assertFalse(formatted.contains("alert"));
        assertFalse(formatted.contains("<script"));
    }

    @Test
    public void prefersPlainTextPartOfAlternative() throws Exception {
        final MessageData msg = MimeParser.parseMimeMessage(
                fixture("multipart-alternative.eml"), new AttachmentsCollector());
        final String formatted = msg.getFormattedText();
        assertTrue(formatted.contains("Text-Version"));
        assertFalse(formatted.contains("HTML-Version"));
    }

    @Test
    public void replacesShortPlainTextWithFollowingHtmlPart() throws Exception {
        final MessageData msg = MimeParser.parseMimeMessage(
                fixture("multipart-short-plain.eml"), new AttachmentsCollector());
        final String formatted = msg.getFormattedText();
        assertTrue(formatted.contains("Die laengere HTML-Version"));
        assertFalse(formatted.contains("Kurz"));
    }

    @Test
    public void collectsAttachmentsFromMixedMessage() throws Exception {
        final AttachmentsCollector collector = new AttachmentsCollector();
        final MessageData msg = MimeParser.parseMimeMessage(fixture("multipart-mixed.eml"), collector);
        assertTrue(msg.getFormattedText().contains("Anbei die Unterlagen."));
        assertEquals(2, collector.getAttachmentsList().size());
        assertTrue(collector.getAttachmentsList().get("bericht.pdf").startsWith("application/pdf"));
        assertTrue(collector.getAttachmentsList().get("bild.png").startsWith("image/png"));
    }

    @Test
    public void marksSeenMessageAsNotNew() throws Exception {
        final MimeMessage mimeMsg = fixture("plain-text.eml");
        mimeMsg.setFlag(Flags.Flag.SEEN, true);
        assertFalse(MimeParser.parseMimeMessage(mimeMsg, new AttachmentsCollector()).isNew());
    }

    @Test
    public void marksFlaggedMessage() throws Exception {
        final MimeMessage mimeMsg = fixture("plain-text.eml");
        mimeMsg.setFlag(Flags.Flag.FLAGGED, true);
        final MessageData msg = MimeParser.parseMimeMessage(mimeMsg, new AttachmentsCollector());
        assertTrue(msg.isFlagged());
        assertTrue(msg.isNew());
    }

    @Test
    public void readsInputStreamBody() throws Exception {
        final MimeMessage mimeMsg = messageWithContent(
                new ByteArrayInputStream("Zeile eins\nZeile zwei".getBytes(StandardCharsets.UTF_8)), "text/plain");
        final MessageData msg = MimeParser.parseMimeMessage(mimeMsg, new AttachmentsCollector());
        assertTrue(msg.getFormattedText().contains("Zeile eins"));
        assertTrue(msg.getFormattedText().contains("Zeile zwei"));
    }

    @Test
    public void labelsNonTextInputStreamContent() throws Exception {
        final MimeMessage mimeMsg = messageWithContent(
                new ByteArrayInputStream(new byte[0]), "application/octet-stream");
        assertEquals("IMAPInputStream type=application/octet-stream",
                MimeParser.parseMimeMessage(mimeMsg, new AttachmentsCollector()).getText());
    }

    @Test
    public void reportsUnknownContent() throws Exception {
        final MimeMessage mimeMsg = messageWithContent(new Object(), "application/octet-stream");
        assertEquals("Unbekannter Inhalt",
                MimeParser.parseMimeMessage(mimeMsg, new AttachmentsCollector()).getText());
    }

    @Test
    public void reportsIoErrorForUnreadableContent() throws Exception {
        final MimeMessage mimeMsg = new MimeMessage(fixture("plain-text.eml")) {
            @Override
            public Object getContent() throws IOException {
                throw new IOException("kaputt");
            }
        };
        assertTrue(MimeParser.parseMimeMessage(mimeMsg, new AttachmentsCollector())
                .getText().startsWith("I-/O-Fehler beim Lesen der Nachricht"));
    }

}
