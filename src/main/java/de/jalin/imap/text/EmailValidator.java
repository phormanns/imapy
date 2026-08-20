package de.jalin.imap.text;

import java.net.IDN;
import java.util.regex.Pattern;

public class EmailValidator {

    private static final Pattern LOCAL_PART_PATTERN
            = Pattern.compile("^[\\p{L}\\p{N}!#$%&'*+/=?^_`{|}~-]([\\p{L}\\p{N}!#$%&'*+/=?^_`{|}~.\\-]*[\\p{L}\\p{N}!#$%&'*+/=?^_`{|}~-])?$");

    private static final Pattern DOMAIN_PATTERN
            = Pattern.compile("^([a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$");

    public static boolean isValidEmailAddress(String email) {
        if (email == null) {
            return false;
        }
        email = email.trim();
        if (email.isEmpty()) {
            return false;
        }

        int atIndex = email.indexOf('@');
        if (atIndex == -1 || atIndex != email.lastIndexOf('@')) {
            return false;
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);

        // === LOCAL PART VALIDIERUNG ===
        if (localPart.isEmpty() || localPart.length() > 64) {
            return false;
        }

        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            return false;
        }

        if (localPart.contains("..")) {
            return false;
        }

        if (!LOCAL_PART_PATTERN.matcher(localPart).matches()) {
            return false;
        }

        // === DOMAIN VALIDIERUNG ===
        if (domain.isEmpty()) {
            return false;
        }

        if (domain.startsWith(".") || domain.endsWith(".")
                || domain.startsWith("-") || domain.endsWith("-")) {
            return false;
        }

        if (domain.contains("..")) {
            return false;
        }

        if (!domain.contains(".")) {
            return false;
        }

        // IDN zu ASCII/Punycode konvertieren
        String asciiDomain;
        try {
            asciiDomain = IDN.toASCII(domain);
        } catch (IllegalArgumentException e) {
            return false;
        }

        // WICHTIG: Längenchecks NACH Punycode-Konvertierung!
        if (asciiDomain.length() > 255) {
            return false;
        }

        // Domain-Labels validieren
        String[] labels = asciiDomain.split("\\.");
        for (String label : labels) {
            if (label.isEmpty() || label.length() > 63) {
                return false;
            }
        }

        // Regex-Validierung
        return DOMAIN_PATTERN.matcher(asciiDomain).matches();
    }
}
