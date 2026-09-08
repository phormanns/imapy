package de.jalin.imap.mime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import de.jalin.imap.text.HtmlHelper;

public class MessageData implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String REGEXP_HTTP_TEXT_LINK = "(https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])";
    private static final String HTTP_LINK_REPLACEMENT = "<a href=\"$1\" target=\"_new\">$1</a>";

    final private String from;
    final private String subject;
    final private String sentTimestamp;
    final private String messageID;

    private String text;
    private String references;
    private boolean isHtml;
    private boolean isNew;
    private boolean isFlagged;

    public MessageData(final String from, final String subject, final String sent, final String messageID) {
        this.isNew = true;
        this.isFlagged = false;
        this.isHtml = false;
        this.from = from;
        this.subject = subject;
        this.sentTimestamp = sent;
        this.messageID = messageID;
        this.text = "";
    }

    public void setFlagged(boolean flagged) {
        isFlagged = flagged;
    }

    public void setNew(boolean newmsg) {
        isNew = newmsg;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(final String references) {
        this.references = references;
    }

    public String getSubject() {
        return subject;
    }

    public String getFrom() {
        return from;
    }

    public void setText(final String text) {
        if (text == null) {
            return;
        }
        final String leadingText29 = text.length() > 29 ? text.substring(0, 29) : text;
        if (leadingText29.startsWith("<p")
                || leadingText29.startsWith("<span")
                || leadingText29.contains("<html")
                || leadingText29.contains("<div")) {
            setHtmlText(text);
            return;
        }
        if (text.length() > 59) {
            final String leadingText59 = text.substring(0, 59);
            if (leadingText59.contains("<!DOCTYPE html")
                    || leadingText59.contains("<!DOCTYPE HTML")) {
                setHtmlText(text);
                return;
            }
        }
        this.text = text;
    }

    public String getFormattedText() {
        if (isHtml) {
            return text;
        }
        final int maxlen = 84;
        final StringBuffer formated = new StringBuffer("<p>\n");
        int blank;
        boolean isPreFormatted = false;
        try {
            final BufferedReader reader = new BufferedReader(new StringReader(text));
            String s = reader.readLine();
            while (s != null) { // Schleife liest zeilenweise
                if (s.trim().isEmpty()) {
                    formated.append("</p>\n");
                    formated.append("<p>\n");
                    isPreFormatted = false;
                } else {
                    isPreFormatted = s.startsWith(">") || s.startsWith(" ") || s.startsWith("--") || s.startsWith("==");
                    if (isPreFormatted) {
                        formated.append("<br />\n");
                    }
                }
                while (s.length() > maxlen) { // lange Zeile werden zerlegt
                    blank = maxlen - 10; // Blank vor dem Umbruch suchen
                    while (blank < s.length() && s.charAt(blank) != ' ') {
                        blank++;
                    }
                    formated.append(HtmlHelper.replaceEntities(s.substring(0, blank)).replaceAll(REGEXP_HTTP_TEXT_LINK, HTTP_LINK_REPLACEMENT));
                    formated.append('\n');
                    if (isPreFormatted) {
                        formated.append("<br />\n");
                    }
                    s = s.substring(blank);
                    while (s.length() > 0 && s.charAt(0) == ' ') {
                        s = s.substring(1); // ggf. fuehrende Blank abschneiden
                    }
                }
                formated.append(HtmlHelper.replaceEntities(s).replaceAll(REGEXP_HTTP_TEXT_LINK, HTTP_LINK_REPLACEMENT));
                if (s.endsWith("--") || s.endsWith("-- ") || s.endsWith("==")) {
                    formated.append("<br />\n");
                }
                formated.append('\n');
                s = reader.readLine(); // naechste Zeile
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        formated.append("</p>\n");
        return formated.toString();
    }

    public String getText() {
        return text;
    }

    public String getSentTimestamp() {
        return sentTimestamp;
    }

    public boolean isNew() {
        return isNew;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public void setHtmlText(final String uncleanHtml) {
        isHtml = true;
        text = Jsoup.clean(uncleanHtml, Safelist.basic());
    }

}
