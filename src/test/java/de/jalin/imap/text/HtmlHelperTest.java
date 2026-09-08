package de.jalin.imap.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class HtmlHelperTest {

    @Test
    public void escapesSpecialCharacters() {
        assertEquals("a &amp; b &lt; c &gt; d &quot; e", HtmlHelper.replaceEntities("a & b < c > d \" e"));
    }

    @Test
    public void leavesPlainTextUnchanged() {
        assertEquals("Hallo Welt 123", HtmlHelper.replaceEntities("Hallo Welt 123"));
    }

    @Test
    public void escapesExistingEntities() {
        assertEquals("&amp;amp;", HtmlHelper.replaceEntities("&amp;"));
    }

    @Test
    public void escapesEveryOccurrence() {
        assertEquals("&amp;&amp;&amp;", HtmlHelper.replaceEntities("&&&"));
    }

    @Test
    public void returnsNullForNull() {
        assertNull(HtmlHelper.replaceEntities(null));
    }

    @Test
    public void handlesEmptyString() {
        assertEquals("", HtmlHelper.replaceEntities(""));
    }

}
