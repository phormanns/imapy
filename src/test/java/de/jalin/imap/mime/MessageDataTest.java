package de.jalin.imap.mime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MessageDataTest {

    private MessageData newMessageData() {
        return new MessageData("absender@example.org", "Betreff", "03.09.2026", "id@example.org");
    }

    @Test
    public void keepsShortPlainText() {
        final MessageData msg = newMessageData();
        msg.setText("Danke!");
        assertTrue(msg.getFormattedText().contains("Danke!"));
    }

    @Test
    public void keepsTextOfExactlyThirtyCharacters() {
        final MessageData msg = newMessageData();
        msg.setText("123456789012345678901234567890");
        assertTrue(msg.getFormattedText().contains("123456789012345678901234567890"));
    }

    @Test
    public void keepsLongPlainText() {
        final MessageData msg = newMessageData();
        msg.setText("Das ist eine laengere Nachricht mit deutlich mehr als neunundzwanzig Zeichen Inhalt.");
        assertTrue(msg.getFormattedText().contains("laengere Nachricht"));
    }

    @Test
    public void detectsShortHtmlText() {
        final MessageData msg = newMessageData();
        msg.setText("<p>kurz</p>");
        final String formatted = msg.getFormattedText();
        assertTrue(formatted.contains("kurz"));
        assertFalse(formatted.contains("&lt;p&gt;"));
    }

    @Test
    public void detectsHtmlDoctype() {
        final MessageData msg = newMessageData();
        msg.setText("<!DOCTYPE html><html><body>Inhalt der Nachricht</body></html>");
        final String formatted = msg.getFormattedText();
        assertTrue(formatted.contains("Inhalt der Nachricht"));
        assertFalse(formatted.contains("&lt;html&gt;"));
    }

    @Test
    public void keepsQuotedLinesWithPrefix() {
        final MessageData msg = newMessageData();
        msg.setText("> erste zitierte Zeile\n> zweite zitierte Zeile");
        final String formatted = msg.getFormattedText();
        assertTrue(formatted.contains("&gt; erste zitierte Zeile") || formatted.contains("> erste zitierte Zeile"));
    }

    @Test
    public void handlesNullText() {
        final MessageData msg = newMessageData();
        msg.setText(null);
        assertFalse(msg.getFormattedText().isEmpty());
    }

}
