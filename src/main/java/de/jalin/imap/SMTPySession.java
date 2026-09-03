package de.jalin.imap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class SMTPySession {

    private static final Pattern MESSAGE_ID_PATTERN = Pattern.compile("<[^<>\\s]+>");
    private static final String MSG_ID_ATOM = "[A-Za-z0-9!#$%&'*+/=?^_`{|}~.\\-]+";
    private static final Set<String> BLOCK_TAGS = Set.of(
            "p", "div", "section", "article", "header", "footer",
            "h1", "h2", "h3", "h4", "h5", "h6",
            "blockquote", "pre", "table", "tr", "ul", "ol", "hr");
    private static final int PLAIN_TEXT_LINE_LENGTH = 78;

    private final String host;
    private final int port;
    private final String user;
    private final char[] password;

    public SMTPySession(final String host, final int port, final String user, final String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password.toCharArray();
    }

    public void sendMail(final String from, final String to, final String subject, final String htmlBody) throws IMAPyException {
        sendMail(from, to, subject, htmlBody, null, null);
    }

    public void sendMail(final String from, final String to, final String subject, final String htmlBody,
                         final String inReplyTo, final String references) throws IMAPyException {
        try {
            final Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", String.valueOf(port));
            props.put("mail.smtp.auth", "true");
            if (port == 465) {
                props.put("mail.smtp.ssl.enable", "true");
            } else {
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
            }
            final Session session = Session.getInstance(props);
            final MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            if (from != null && !from.isBlank()) {
                msg.setRecipient(Message.RecipientType.BCC, new InternetAddress(from));
            }
            if (subject != null && !subject.isBlank()) {
                msg.setSubject(subject, "UTF-8");
            }
            msg.setSentDate(new Date());
            setThreadingHeaders(msg, inReplyTo, references);
            final MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(toPlainText(htmlBody), "UTF-8");
            final MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody == null ? "" : htmlBody, "text/html; charset=UTF-8");
            final Multipart multipart = new MimeMultipart("alternative");
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(htmlPart);
            msg.setContent(multipart);
            msg.saveChanges();
            try (Transport transport = session.getTransport("smtp")) {
                transport.connect(host, port, user, new String(password));
                transport.sendMessage(msg, msg.getAllRecipients());
            }
        } catch (MessagingException e) {
            throw new IMAPyException(e);
        }
    }

    private static void setThreadingHeaders(final MimeMessage msg, final String inReplyTo, final String references) throws MessagingException {
        final List<String> referenceIds = extractMessageIds(references);
        if (isValidMessageId(inReplyTo)) {
            msg.setHeader("In-Reply-To", "<" + inReplyTo + ">");
            referenceIds.add("<" + inReplyTo + ">");
        }
        if (!referenceIds.isEmpty()) {
            msg.setHeader("References", String.join(" ", referenceIds));
        }
    }

    private static List<String> extractMessageIds(final String headerValue) {
        final List<String> ids = new ArrayList<>();
        if (headerValue != null) {
            final Matcher matcher = MESSAGE_ID_PATTERN.matcher(headerValue);
            while (matcher.find()) {
                ids.add(matcher.group());
            }
        }
        return ids;
    }

    private static boolean isValidMessageId(final String messageId) {
        return messageId != null && !messageId.isBlank() && messageId.matches(MSG_ID_ATOM + "@" + MSG_ID_ATOM);
    }

    private static String toPlainText(final String htmlBody) {
        if (htmlBody == null || htmlBody.isBlank()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        appendPlainText(Jsoup.parse(htmlBody).body(), sb);
        final String text = sb.toString()
                .replaceAll("[ \\t\\u00a0]+", " ")
                .replaceAll(" ?\n ?", "\n")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
        return wrapLines(text);
    }

    private static void appendPlainText(final Element element, final StringBuilder sb) {
        for (final Node node : element.childNodes()) {
            if (node instanceof TextNode) {
                sb.append(((TextNode) node).text());
            } else if (node instanceof Element) {
                final Element child = (Element) node;
                switch (child.tagName()) {
                    case "br" -> sb.append('\n');
                    case "li" -> {
                        sb.append("\n- ");
                        appendPlainText(child, sb);
                    }
                    case "img", "script", "style" -> {
                    }
                    default -> {
                        if (BLOCK_TAGS.contains(child.tagName()) && needsNewline(sb)) {
                            sb.append('\n');
                        }
                        appendPlainText(child, sb);
                    }
                }
            }
        }
    }

    private static boolean needsNewline(final StringBuilder sb) {
        for (int i = sb.length() - 1; i >= 0; i--) {
            final char c = sb.charAt(i);
            if (c == '\n') {
                return false;
            }
            if (c != ' ' && c != '\t') {
                return true;
            }
        }
        return false;
    }

    private static String wrapLines(final String text) {
        final StringBuilder result = new StringBuilder();
        for (final String line : text.split("\n", -1)) {
            if (line.length() <= PLAIN_TEXT_LINE_LENGTH) {
                result.append(line);
            } else {
                String rest = line;
                while (rest.length() > PLAIN_TEXT_LINE_LENGTH) {
                    int cut = rest.lastIndexOf(' ', PLAIN_TEXT_LINE_LENGTH);
                    if (cut < PLAIN_TEXT_LINE_LENGTH / 2) {
                        cut = PLAIN_TEXT_LINE_LENGTH;
                    }
                    result.append(rest, 0, cut);
                    result.append('\n');
                    rest = rest.substring(cut).stripLeading();
                }
                result.append(rest);
            }
            result.append('\n');
        }
        return result.toString().stripTrailing();
    }

}
