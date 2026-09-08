package de.jalin.webmail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class AvatarColorTest {

    @Test
    public void returnsNullForNullOrEmptyAuthor() {
        assertNull(AvatarColor.backgroundStyle(null));
        assertNull(AvatarColor.backgroundStyle(""));
    }

    @Test
    public void returnsNullWithoutEmailAddress() {
        assertNull(AvatarColor.backgroundStyle("Unbekannter Absender"));
        assertNull(AvatarColor.backgroundStyle("john@"));
    }

    @Test
    public void sameDomainYieldsSameStyle() {
        assertEquals(
                AvatarColor.backgroundStyle("john@example.com"),
                AvatarColor.backgroundStyle("jane@example.com"));
    }

    @Test
    public void differentDomainsYieldDifferentHues() {
        assertNotEquals(
                AvatarColor.backgroundStyle("john@example.com"),
                AvatarColor.backgroundStyle("john@web.de"));
    }

    @Test
    public void subdomainUsesMainDomain() {
        assertEquals(
                AvatarColor.backgroundStyle("jane@mail.example.com"),
                AvatarColor.backgroundStyle("jane@example.com"));
    }

    @Test
    public void displayNameIsIgnored() {
        assertEquals(
                AvatarColor.backgroundStyle("John Doe <john@example.com>"),
                AvatarColor.backgroundStyle("john@example.com"));
    }

    @Test
    public void authorIsCaseInsensitive() {
        assertEquals(
                AvatarColor.backgroundStyle("JOHN@EXAMPLE.COM"),
                AvatarColor.backgroundStyle("john@example.com"));
    }

    @Test
    public void styleContainsComputedGradient() {
        final String style = AvatarColor.backgroundStyle("john@example.com");
        assertTrue(style.startsWith("background:linear-gradient(135deg,hsl("));
        assertTrue(style.contains("70%,45%"));
        assertTrue(style.endsWith("));"));
    }

    @Test
    public void mainDomainCombinesLastTwoLabels() {
        assertEquals("example.com", AvatarColor.mainDomain("mail.example.com"));
        assertEquals("example.com", AvatarColor.mainDomain("example.com"));
        assertEquals("localhost", AvatarColor.mainDomain("localhost"));
    }

    @Test
    public void hueIsStableAndInRange() {
        final int hue = AvatarColor.hueFor("example.com");
        assertTrue(hue >= 0 && hue < 360);
        assertEquals(hue, AvatarColor.hueFor("example.com"));
    }

    @Test
    public void hueVariesWithDomainPrefix() {
        assertNotEquals(AvatarColor.hueFor("aaa.com"), AvatarColor.hueFor("zzz.com"));
    }

    @Test
    public void digitsAreSupported() {
        assertEquals(
                AvatarColor.backgroundStyle("user@mail2.example.com"),
                AvatarColor.backgroundStyle("user@example.com"));
    }

}