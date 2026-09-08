package de.jalin.webmail;

import java.util.Locale;

public final class AvatarColor {

    private AvatarColor() {
    }

    public static String backgroundStyle(final String author) {
        if (author == null || author.isEmpty()) {
            return null;
        }
        final String email = extractEmail(author);
        final int at = email.lastIndexOf('@');
        if (at < 0 || at >= email.length() - 1) {
            return null;
        }
        final int hue = hueFor(email.substring(at + 1));
        return "background:linear-gradient(135deg,hsl(" + hue + ",70%,45%),hsl(" + ((hue + 40) % 360) + ",70%,60%));";
    }

    static String extractEmail(final String author) {
        final int lt = author.indexOf('<');
        final int gt = author.indexOf('>');
        if (lt >= 0 && gt > lt) {
            return author.substring(lt + 1, gt).trim().toLowerCase(Locale.ROOT);
        }
        return author.trim().toLowerCase(Locale.ROOT);
    }

    static String mainDomain(final String domain) {
        final String[] labels = domain.split("\\.");
        if (labels.length >= 2) {
            return labels[labels.length - 2] + "." + labels[labels.length - 1];
        }
        return domain;
    }

    static int hueFor(final String domain) {
        final String prefix = mainDomain(domain);
        final char c0 = prefix.isEmpty() ? 'a' : prefix.charAt(0);
        final char c1 = prefix.length() > 1 ? prefix.charAt(1) : 'a';
        return (valueOf(c0) * 37 + valueOf(c1) * 11) % 360;
    }

    private static int valueOf(final char c) {
        if (c >= 'a' && c <= 'z') {
            return c - 'a';
        }
        if (c >= '0' && c <= '9') {
            return 26 + c - '0';
        }
        return 0;
    }

}