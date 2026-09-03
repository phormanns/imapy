package de.jalin.imap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SMTPySessionSendMailTest {

    private SMTPySession newSession(final FakeSmtpServer server) {
        return new SMTPySession("localhost", server.getPort(), "benutzer", "geheim", false);
    }

    @Test
    public void sendsMessageWithThreadingHeadersAndBccCopy() throws Exception {
        try (FakeSmtpServer server = new FakeSmtpServer()) {
            final SMTPySession smtp = newSession(server);
            smtp.sendMail("paul@example.org", "empfaenger@example.org", "Testbetreff",
                    "<div>Hallo</div><div>Zeile zwei</div>", "abc@example.org", "<old1@x> <old2@y>", null);
            assertTrue(server.isMessageReceived());
            final List<String> rcptCommands = server.getRecipientCommands().stream()
                    .filter(cmd -> cmd.toUpperCase().startsWith("RCPT TO"))
                    .toList();
            assertTrue(rcptCommands.stream().anyMatch(cmd -> cmd.contains("empfaenger@example.org")));
            assertTrue(rcptCommands.stream().anyMatch(cmd -> cmd.contains("paul@example.org")));
            assertEqualsBccHidden(server.getMessageData());
            assertTrue(server.getMessageData().contains("In-Reply-To: <abc@example.org>"));
            assertTrue(server.getMessageData().contains("References: <old1@x> <old2@y> <abc@example.org>"));
        }
    }

    private void assertEqualsBccHidden(final String messageData) {
        assertFalse(messageData.contains("Bcc:"), "Bcc-Header darf nicht mitgesendet werden");
    }

    @Test
    public void sendsPlainTextPartWithLineBreaks() throws Exception {
        try (FakeSmtpServer server = new FakeSmtpServer()) {
            final SMTPySession smtp = newSession(server);
            smtp.sendMail("paul@example.org", "empfaenger@example.org", "Zeilen",
                    "<div>erste Zeile</div><div>zweite Zeile</div>", null, null, null);
            final String data = server.getMessageData();
            assertTrue(data.contains("Content-Type: text/plain"));
            assertTrue(data.contains("erste Zeile") && data.contains("zweite Zeile"));
        }
    }

    @Test
    public void sendsAttachmentsAsMultipartMixed() throws Exception {
        try (FakeSmtpServer server = new FakeSmtpServer()) {
            final SMTPySession smtp = newSession(server);
            final List<MailAttachment> attachments = List.of(
                    new MailAttachment("test.txt", "text/plain", "Anhanginhalt".getBytes(StandardCharsets.UTF_8)));
            smtp.sendMail("paul@example.org", "empfaenger@example.org", "Mit Anhang",
                    "<div>Anbei der Anhang</div>", null, null, attachments);
            final String data = server.getMessageData();
            assertTrue(data.contains("multipart/mixed"));
            assertTrue(data.contains("multipart/alternative"));
            assertTrue(data.contains("filename=test.txt"));
            assertTrue(data.contains("Content-Disposition: attachment"));
        }
    }

    @Test
    public void sendsSimpleMessageAsAlternativeOnly() throws Exception {
        try (FakeSmtpServer server = new FakeSmtpServer()) {
            final SMTPySession smtp = newSession(server);
            smtp.sendMail("paul@example.org", "empfaenger@example.org", "Einfach",
                    "<div>ohne Anhang</div>", null, null, List.of());
            final String data = server.getMessageData();
            assertTrue(data.contains("multipart/alternative"));
            assertFalse(data.contains("multipart/mixed"));
        }
    }

}
