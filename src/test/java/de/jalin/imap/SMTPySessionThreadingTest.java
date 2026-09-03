package de.jalin.imap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

public class SMTPySessionThreadingTest {

    private MimeMessage newMessage() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    @Test
    public void acceptsValidMessageId() {
        assertTrue(SMTPySession.isValidMessageId("abc@example.org"));
        assertTrue(SMTPySession.isValidMessageId("Abc.Def-12_x@sub.domain.example"));
    }

    @Test
    public void rejectsInvalidMessageIds() {
        assertFalse(SMTPySession.isValidMessageId("16f3a1@Thread[pool-1-thread-2,5,main]"));
        assertFalse(SMTPySession.isValidMessageId("kein-at-zeichen"));
        assertFalse(SMTPySession.isValidMessageId(""));
        assertFalse(SMTPySession.isValidMessageId(null));
        assertFalse(SMTPySession.isValidMessageId("a@b\r\nX-Inject: 1"));
        assertFalse(SMTPySession.isValidMessageId("a@b c"));
    }

    @Test
    public void extractsOnlyCompleteMessageIds() {
        final List<String> ids = SMTPySession.extractMessageIds("<a@b.c> <d@e.f> <x@y.z");
        assertEquals(List.of("<a@b.c>", "<d@e.f>"), ids);
        assertTrue(SMTPySession.extractMessageIds(null).isEmpty());
        assertTrue(SMTPySession.extractMessageIds("keine ids hier").isEmpty());
    }

    @Test
    public void setsInReplyToAndReferences() throws Exception {
        final MimeMessage msg = newMessage();
        SMTPySession.setThreadingHeaders(msg, "abc@example.org", "<old1@x> <old2@y>");
        assertEquals("<abc@example.org>", msg.getHeader("In-Reply-To", null));
        assertEquals("<old1@x> <old2@y> <abc@example.org>", msg.getHeader("References", null));
    }

    @Test
    public void setsReferencesWithoutOriginalReferences() throws Exception {
        final MimeMessage msg = newMessage();
        SMTPySession.setThreadingHeaders(msg, "abc@example.org", null);
        assertEquals("<abc@example.org>", msg.getHeader("In-Reply-To", null));
        assertEquals("<abc@example.org>", msg.getHeader("References", null));
    }

    @Test
    public void skipsInReplyToForInvalidMessageIdButKeepsReferences() throws Exception {
        final MimeMessage msg = newMessage();
        SMTPySession.setThreadingHeaders(msg, "16f3a1@Thread[pool-1-thread-2,5,main]", "<old1@x>");
        assertNull(msg.getHeader("In-Reply-To", null));
        assertEquals("<old1@x>", msg.getHeader("References", null));
    }

    @Test
    public void sanitizesInjectedHeaderValues() throws Exception {
        final MimeMessage msg = newMessage();
        SMTPySession.setThreadingHeaders(msg, "a@b\r\nX-Inject: 1", "<a@b.c> evil <d@e.f>\r\nBcc: victim@example.org");
        assertNull(msg.getHeader("In-Reply-To", null));
        assertNull(msg.getHeader("Bcc", null));
        assertEquals("<a@b.c> <d@e.f>", msg.getHeader("References", null));
    }

    @Test
    public void doesNotTouchMessageWithoutThreadingData() throws Exception {
        final MimeMessage msg = newMessage();
        SMTPySession.setThreadingHeaders(msg, null, null);
        assertNull(msg.getHeader("In-Reply-To", null));
        assertNull(msg.getHeader("References", null));
        assertTrue(msg.getAllRecipients() == null);
    }

    @Test
    public void messageKeepsRecipientsUntouched() throws Exception {
        final MimeMessage msg = newMessage();
        msg.setRecipients(Message.RecipientType.TO, "empfaenger@example.org");
        SMTPySession.setThreadingHeaders(msg, "abc@example.org", null);
        assertEquals("empfaenger@example.org", msg.getRecipients(Message.RecipientType.TO)[0].toString());
    }

}
