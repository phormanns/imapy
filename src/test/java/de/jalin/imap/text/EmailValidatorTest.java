package de.jalin.imap.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("EmailValidator Tests")
class EmailValidatorTest {
    
    // ============ Tests für gültige E-Mail-Adressen ============
    
    @ParameterizedTest(name = "{0} sollte gültig sein")
    @ValueSource(strings = {
        "user@example.com",
        "john.doe@example.com",
        "user+tag@example.co.uk",
        "jürgen@müller.de",
        "café@example.fr",
        "user_name@sub.example.com",
        "123@example.com",
        "a@b.co",
        "test.email.with.multiple.dots@example.com",
        "user+mailbox@example.com",
        "customer/department=shipping@example.com",
        "x@example.com",
        "_user@example.com",
        "user-name@example-domain.com",
        "user@subdomain.example.com",
        "user@sub.sub.example.com"
    })
    @DisplayName("Gültige E-Mail-Adressen sollten akzeptiert werden")
    void testValidEmails(String email) {
        assertTrue(EmailValidator.isValidEmailAddress(email),
                "E-Mail sollte gültig sein: " + email);
    }
    
    // ============ Tests für ungültige E-Mail-Adressen ============
    
    @ParameterizedTest(name = "{0} sollte ungültig sein")
    @ValueSource(strings = {
        "",                              // leer
        "invalid.email",                 // kein @
        "@example.com",                  // kein Local-Part
        "user@",                         // keine Domain
        "user@@example.com",             // zwei @
        ".user@example.com",             // Punkt am Anfang
        "user.@example.com",             // Punkt am Ende
        "user..name@example.com",        // aufeinanderfolgende Punkte
        "user@example",                  // keine TLD
        "user@.example.com",             // Domain beginnt mit Punkt
        "user@-example.com",             // Domain beginnt mit Bindestrich
        "user@example.com-",             // Domain endet mit Bindestrich
        "user name@example.com",         // Leerzeichen im Local-Part
        "user@exam ple.com",             // Leerzeichen in Domain
        "user@@@@example.com",           // mehrfache @
        "user@example..com",             // aufeinanderfolgende Punkte in Domain
        "user@example.c",                // TLD zu kurz (1 Zeichen)
        "plainaddress",                  // kein Domain-Teil
        "user.@.example.com",            // Punkt am Ende des Local-Parts
        "..user@example.com",            // doppelte Punkte am Anfang
        "user@..example.com",            // doppelte Punkte in Domain
    })
    @DisplayName("Ungültige E-Mail-Adressen sollten abgelehnt werden")
    void testInvalidEmails(String email) {
        assertFalse(EmailValidator.isValidEmailAddress(email),
                "E-Mail sollte ungültig sein: " + (email.isEmpty() ? "(leer)" : email));
    }
    
    // ============ Tests für Null und Whitespace ============
    
    @Test
    @DisplayName("Null sollte abgelehnt werden")
    void testNullEmail() {
        assertFalse(EmailValidator.isValidEmailAddress(null),
                "Null sollte als ungültig gewertet werden");
    }
    
    @Test
    @DisplayName("Nur Whitespace sollte abgelehnt werden")
    void testWhitespaceOnlyEmail() {
        assertFalse(EmailValidator.isValidEmailAddress("   "),
                "Nur Whitespace sollte als ungültig gewertet werden");
    }
    
    @Test
    @DisplayName("Whitespace sollte getrimmt werden")
    void testEmailWithLeadingTrailingWhitespace() {
        assertTrue(EmailValidator.isValidEmailAddress("  user@example.com  "),
                "Whitespace sollte getrimmt werden");
    }
    
    // ============ Tests für Local-Part Längenbeschränkung ============
    
    @Test
    @DisplayName("Local-Part mit maximal 64 Zeichen sollte gültig sein")
    void testMaxLocalPartLength() {
        String localPart = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        String email = localPart + "@example.com";
        assertTrue(EmailValidator.isValidEmailAddress(email),
                "Local-Part mit 64 Zeichen sollte gültig sein");
    }
    
    @Test
    @DisplayName("Local-Part mit über 64 Zeichen sollte ungültig sein")
    void testExceedMaxLocalPartLength() {
        String localPart = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabb";
        String email = localPart + "@example.com";
        assertFalse(EmailValidator.isValidEmailAddress(email),
                "Local-Part mit 65 Zeichen sollte ungültig sein");
    }
    
    // ============ Tests für Domain Längenbeschränkung ============
    
    @Test
    @DisplayName("Domain mit maximal 255 Zeichen sollte gültig sein")
    void testMaxDomainLength() {
        // Konstruiere eine Domain mit 255 Zeichen
        String domain = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + ".example.com";
        String email = "user@" + domain;
        assertTrue(EmailValidator.isValidEmailAddress(email),
                "Domain mit 255 Zeichen sollte gültig sein");
    }
    
    @Test
    @DisplayName("Domain mit über 255 Zeichen sollte ungültig sein")
    void testExceedMaxDomainLength() {
        String domain = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + ".example.com";
        String email = "user@" + domain;
        assertFalse(EmailValidator.isValidEmailAddress(email),
                "Domain mit über 255 Zeichen sollte ungültig sein");
    }
    
    // ============ Tests für Internationalisierte Domains (IDN) ============
    
    @ParameterizedTest(name = "{0} mit Umlauten/Akzenten sollte gültig sein")
    @ValueSource(strings = {
        "user@müller.de",
        "user@café.fr",
        "user@münchen.de",
        "user@zürich.ch",
        "jürgen@müller.de",
        "françois@société.fr",
        "josé@españa.es"
    })
    @DisplayName("E-Mails mit internationalisierten Domains sollten akzeptiert werden")
    void testInternationalizedDomains(String email) {
        assertTrue(EmailValidator.isValidEmailAddress(email),
                "E-Mail mit internationalisierter Domain sollte gültig sein: " + email);
    }
    
    // ============ Tests für Unicode im Local-Part ============
    
    @ParameterizedTest(name = "{0} mit Unicode sollte gültig sein")
    @ValueSource(strings = {
        "jürgen@example.com",
        "françois@example.com",
        "josé@example.com",
        "мария@example.com",  // kyrillisch
        "日本@example.com"      // japanisch
    })
    @DisplayName("E-Mails mit Unicode im Local-Part sollten akzeptiert werden")
    void testUnicodeInLocalPart(String email) {
        assertTrue(EmailValidator.isValidEmailAddress(email),
                "E-Mail mit Unicode sollte gültig sein: " + email);
    }
    
    // ============ Tests für Sonderzeichen ============
    
    @ParameterizedTest(name = "{0} mit Sonderzeichen sollte gültig sein")
    @ValueSource(strings = {
        "user+tag@example.com",
        "user_name@example.com",
        "user-name@example.com",
        "user'name@example.com",
        "user=name@example.com",
        "user?name@example.com",
        "user*name@example.com",
        "user^name@example.com",
        "user`name@example.com",
        "user{name}@example.com",
        "user|name@example.com",
        "user~name@example.com"
    })
    @DisplayName("E-Mails mit RFC-konformen Sonderzeichen sollten akzeptiert werden")
    void testSpecialCharacters(String email) {
        assertTrue(EmailValidator.isValidEmailAddress(email),
                "E-Mail mit Sonderzeichen sollte gültig sein: " + email);
    }
    
    // ============ Tests für Subdomains ============
    
    @ParameterizedTest(name = "{0} mit Subdomain sollte gültig sein")
    @ValueSource(strings = {
        "user@mail.example.com",
        "user@sub.example.com",
        "user@sub.sub.example.com",
        "user@mail.co.uk",
        "user@deep.sub.domain.example.com"
    })
    @DisplayName("E-Mails mit Subdomains sollten akzeptiert werden")
    void testSubdomains(String email) {
        assertTrue(EmailValidator.isValidEmailAddress(email),
                "E-Mail mit Subdomain sollte gültig sein: " + email);
    }
    
    // ============ Tests für Edge Cases ============
    
    @Test
    @DisplayName("Minimale gültige E-Mail (a@b.co)")
    void testMinimalValidEmail() {
        assertTrue(EmailValidator.isValidEmailAddress("a@b.co"),
                "Minimale E-Mail sollte gültig sein");
    }
    
    @Test
    @DisplayName("Local-Part mit nur Ziffern sollte gültig sein")
    void testNumericLocalPart() {
        assertTrue(EmailValidator.isValidEmailAddress("123456@example.com"),
                "Rein numerischer Local-Part sollte gültig sein");
    }
    
    @Test
    @DisplayName("Domain mit nur Buchstaben in jedem Label")
    void testAlphabeticDomain() {
        assertTrue(EmailValidator.isValidEmailAddress("user@example.museum"),
                "Domain mit längerer TLD sollte gültig sein");
    }
    
    @Test
    @DisplayName("Domain mit Ziffern (nicht am Anfang/Ende von Labels)")
    void testDomainWithDigits() {
        assertTrue(EmailValidator.isValidEmailAddress("user@ex4mpl3.com"),
                "Domain mit Ziffern sollte gültig sein");
    }
    
    @Test
    @DisplayName("Domain mit Bindestrich (nicht am Anfang/Ende von Labels)")
    void testDomainWithHyphen() {
        assertTrue(EmailValidator.isValidEmailAddress("user@my-example.com"),
                "Domain mit Bindestrich sollte gültig sein");
    }
    
    // ============ Parametrisierte Tests mit CSV ============
    
    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "user@example.com, true",
        "invalid@, false",
        "@invalid.com, false",
        "user+tag@example.com, true",
        "user@exam ple.com, false",
        "user..name@example.com, false",
        "jürgen@müller.de, true",
        "a@b.co, true",
        "user name@example.com, false"
    })
    @DisplayName("Verschiedene E-Mail-Adressen mit erwartetem Ergebnis")
    void testEmailWithExpectedResult(String email, boolean expected) {
        assertEquals(expected, EmailValidator.isValidEmailAddress(email),
                "E-Mail '" + email + "' sollte " + (expected ? "gültig" : "ungültig") + " sein");
    }
}
