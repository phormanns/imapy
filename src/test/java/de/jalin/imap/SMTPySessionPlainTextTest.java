package de.jalin.imap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SMTPySessionPlainTextTest {

    private static String toPlainText(final String html) {
        return SMTPySession.toPlainText(html);
    }

    @Test
    public void keepsParagraphBreaksFromEditorDivs() {
        final String html = "Hallo Paul,<div><br></div><div>hier ist <b>fette</b> und <i>kursive</i> Nachricht.</div><div><br></div><div>Viele Gruesse</div>";
        final String plain = toPlainText(html);
        assertTrue(plain.contains("Hallo Paul,\n\nhier ist fette und kursive Nachricht.\n\nViele Gruesse"));
    }

    @Test
    public void rendersListItemsWithDashPrefix() {
        final String html = "<div>Und eine Liste:</div><div><ul><li>erster Punkt</li><li>zweiter Punkt</li></ul></div>";
        final String plain = toPlainText(html);
        assertTrue(plain.contains("Und eine Liste:\n\n- erster Punkt\n- zweiter Punkt"));
    }

    @Test
    public void keepsQuotedReplyLines() {
        final String html = "<div>Am 03.09.2026 schrieb x@y.z:</div><div>&gt; erste zitierte Zeile</div><div>&gt;</div><div>&gt; zweite zitierte Zeile</div>";
        final String plain = toPlainText(html);
        assertTrue(plain.contains("> erste zitierte Zeile\n>\n> zweite zitierte Zeile"));
    }

    @Test
    public void wrapsLongLinesAtWordBoundaries() {
        final String html = "<div>Das ist eine sehr lange Zeile ohne Umbrueche die das Zeilenlimit von achtundsiebzig Zeichen deutlich ueberschreitet und deshalb an einer Wortgrenze umgebrochen werden sollte.</div>";
        final String plain = toPlainText(html);
        for (final String line : plain.split("\n")) {
            assertTrue(line.length() <= 78, "Zeile zu lang: " + line);
        }
        assertTrue(plain.contains("umgebrochen werden sollte."));
    }

    @Test
    public void collapsesMultipleBlankLines() {
        final String html = "<div>eins</div><div><br></div><div><br></div><div><br></div><div>zwei</div>";
        final String plain = toPlainText(html);
        assertFalse(plain.contains("\n\n\n"));
        assertTrue(plain.contains("eins\n\nzwei"));
    }

    @Test
    public void convertsEmptyAndNullToEmptyText() {
        assertEquals("", toPlainText(""));
        assertEquals("", toPlainText(null));
        assertEquals("", toPlainText("   "));
    }

}
