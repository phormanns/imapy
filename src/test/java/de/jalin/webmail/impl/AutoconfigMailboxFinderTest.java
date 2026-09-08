package de.jalin.webmail.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import de.jalin.imap.IMAPyException;

public class AutoconfigMailboxFinderTest {

    private InputStream fixture(String name) {
        final InputStream in = AutoconfigMailboxFinderTest.class.getResourceAsStream(name);
        assertNotNull(in, "Fixture fehlt: " + name);
        return in;
    }

    @Test
    public void extractsHostAndUserWithPlaceholders() throws Exception {
        final AutoconfigMailboxFinder finder = new AutoconfigMailboxFinder();
        finder.parseAutoconfig(fixture("config-placeholders.xml"), "paul@example.org");
        assertEquals("example.org", finder.getHost());
        assertEquals("paul", finder.getUser());
    }

    @Test
    public void extractsFullEmailAddressAsUser() throws Exception {
        final AutoconfigMailboxFinder finder = new AutoconfigMailboxFinder();
        finder.parseAutoconfig(fixture("config-emailaddress.xml"), "paul@example.org");
        assertEquals("imap.provider.example", finder.getHost());
        assertEquals("paul@example.org", finder.getUser());
    }

    @Test
    public void selectsImapServerAndSkipsPop3() throws Exception {
        final AutoconfigMailboxFinder finder = new AutoconfigMailboxFinder();
        finder.parseAutoconfig(fixture("config-pop-and-imap.xml"), "paul@example.org");
        assertEquals("imap.example.org", finder.getHost());
        assertEquals("paul", finder.getUser());
    }

    @Test
    public void leavesHostAndUserUnsetWithoutImapServer() throws Exception {
        final AutoconfigMailboxFinder finder = new AutoconfigMailboxFinder();
        finder.parseAutoconfig(fixture("config-no-imap.xml"), "paul@example.org");
        assertNull(finder.getHost());
        assertNull(finder.getUser());
    }

    @Test
    public void rejectsMalformedConfig() {
        final AutoconfigMailboxFinder finder = new AutoconfigMailboxFinder();
        assertThrows(SAXException.class, () -> finder.parseAutoconfig(fixture("config-broken.xml"), "paul@example.org"));
    }

    @Test
    public void fallsBackToWellKnownUrlWhenSubdomainIsUnknown() throws Exception {
        final FakeStreamFinder finder = new FakeStreamFinder();
        finder.streams.put(
                "https://example.org/.well-known/autoconfig/mail/config-v1.1.xml?emailaddress=paul@example.org",
                fixture("config-placeholders.xml"));
        finder.setLogin("paul@example.org");
        assertEquals("example.org", finder.getHost());
        assertEquals("paul", finder.getUser());
        assertEquals(2, finder.requestedUrls.size());
        assertEquals("https://autoconfig.example.org/mail/config-v1.1.xml?emailaddress=paul@example.org",
                finder.requestedUrls.get(0));
        assertEquals("https://example.org/.well-known/autoconfig/mail/config-v1.1.xml?emailaddress=paul@example.org",
                finder.requestedUrls.get(1));
    }

    @Test
    public void usesSubdomainUrlWhenAvailable() throws Exception {
        final FakeStreamFinder finder = new FakeStreamFinder();
        finder.streams.put(
                "https://autoconfig.example.org/mail/config-v1.1.xml?emailaddress=paul@example.org",
                fixture("config-emailaddress.xml"));
        finder.setLogin("paul@example.org");
        assertEquals("imap.provider.example", finder.getHost());
        assertEquals("paul@example.org", finder.getUser());
        assertEquals(1, finder.requestedUrls.size());
    }

    @Test
    public void reportsErrorWhenBothUrlsFail() {
        final FakeStreamFinder finder = new FakeStreamFinder();
        assertThrows(IMAPyException.class, () -> finder.setLogin("paul@example.org"));
        assertEquals(2, finder.requestedUrls.size());
    }

    @Test
    public void rejectsLoginWithoutAtSign() {
        final AutoconfigMailboxFinder finder = new AutoconfigMailboxFinder();
        assertThrows(IMAPyException.class, () -> finder.setLogin("paulexample.org"));
    }

    @Test
    public void rejectsBlankDomain() {
        assertThrows(IMAPyException.class, () -> AutoconfigMailboxFinder.normalizeAndValidateDomain(null));
        assertThrows(IMAPyException.class, () -> AutoconfigMailboxFinder.normalizeAndValidateDomain("   "));
    }

    @Test
    public void rejectsDomainWithoutDot() {
        assertThrows(IMAPyException.class, () -> AutoconfigMailboxFinder.normalizeAndValidateDomain("example"));
    }

    @Test
    public void rejectsLocalDomains() {
        assertThrows(IMAPyException.class, () -> AutoconfigMailboxFinder.normalizeAndValidateDomain("localhost"));
        assertThrows(IMAPyException.class, () -> AutoconfigMailboxFinder.normalizeAndValidateDomain("mail.localhost"));
        assertThrows(IMAPyException.class, () -> AutoconfigMailboxFinder.normalizeAndValidateDomain("example.local"));
    }

    @Test
    public void rejectsInvalidCharacters() {
        assertThrows(IMAPyException.class, () -> AutoconfigMailboxFinder.normalizeAndValidateDomain("exa_mple.org"));
    }

    @Test
    public void rejectsOverlongDomain() {
        assertThrows(IMAPyException.class, () -> AutoconfigMailboxFinder.normalizeAndValidateDomain("a".repeat(300) + ".org"));
    }

    @Test
    public void trimsAndLowercasesValidDomain() throws IMAPyException {
        assertEquals("example.org", AutoconfigMailboxFinder.normalizeAndValidateDomain(" Example.ORG "));
    }

    @Test
    public void convertsInternationalizedDomain() throws IMAPyException {
        final String ascii = AutoconfigMailboxFinder.normalizeAndValidateDomain("exämple.org");
        assertTrue(ascii.startsWith("xn--"));
        assertTrue(ascii.endsWith(".org"));
    }

    @Test
    public void marksPrivateAndSpecialAddressesUnsafe() throws Exception {
        assertTrue(AutoconfigMailboxFinder.isUnsafeAddress(InetAddress.getByName("127.0.0.1")));
        assertTrue(AutoconfigMailboxFinder.isUnsafeAddress(InetAddress.getByName("::1")));
        assertTrue(AutoconfigMailboxFinder.isUnsafeAddress(InetAddress.getByName("0.0.0.0")));
        assertTrue(AutoconfigMailboxFinder.isUnsafeAddress(InetAddress.getByName("192.168.1.1")));
        assertTrue(AutoconfigMailboxFinder.isUnsafeAddress(InetAddress.getByName("10.0.0.1")));
        assertTrue(AutoconfigMailboxFinder.isUnsafeAddress(InetAddress.getByName("169.254.1.1")));
        assertTrue(AutoconfigMailboxFinder.isUnsafeAddress(InetAddress.getByName("224.0.0.1")));
    }

    @Test
    public void marksPublicAddressesSafe() throws Exception {
        assertFalse(AutoconfigMailboxFinder.isUnsafeAddress(InetAddress.getByName("93.184.216.34")));
        assertFalse(AutoconfigMailboxFinder.isUnsafeAddress(InetAddress.getByName("8.8.8.8")));
    }

    private static class FakeStreamFinder extends AutoconfigMailboxFinder {

        final Map<String, InputStream> streams = new HashMap<>();
        final List<String> requestedUrls = new ArrayList<>();

        @Override
        InputStream openStream(URI uri) throws IOException {
            requestedUrls.add(uri.toString());
            final InputStream stream = streams.get(uri.toString());
            if (stream == null) {
                throw new UnknownHostException(uri.getHost());
            }
            return stream;
        }
    }

}
