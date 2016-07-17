package de.jalin.imap.mime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import de.jalin.imap.text.HtmlHelper;


public class MessageData implements Serializable {


	private static final long serialVersionUID = 1L;

	private static final String REGEXP_HTTP_TEXT_LINK = "(https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])";
	private static final String HTTP_LINK_REPLACEMENT = "<a href=\"$1\" target=\"_new\">$1</a>";
	
	final private Map<String, String> attachmentsHash;
	final private String from;
	final private String subject;
	final private String sentTimestamp;
	final private String messageID;
	
	private String text;
	private boolean isHtml;
	private boolean isNew;
	private boolean isFlagged;


	public MessageData(final String from, final String subject, final String sent, final String messageID) 
	{
		this.attachmentsHash = new HashMap<>();
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

	public String getSubject() {
		return subject;
	}

	public String getFrom() {
		return from;
	}

	public void setText(final String text) {
		if (text != null && text.length() > 29) {
			final String textBeginning = text.substring(0, 29);
			if (textBeginning.startsWith("<p") || textBeginning.startsWith("<span") || textBeginning.contains("<html") || textBeginning.contains("<div") || textBeginning.contains("<!DOCTYPE html")) {
				setHtmlText(text);
			} else {
				this.text = text;
			}
		} else {
			this.text = text;
		}
	}

	public String getFormattedText() {
		if (isHtml) {
			return text;
		}
		final int maxlen = 84;
		final StringBuffer formated = new StringBuffer("<p>\n");
		int blank = maxlen - 10;
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
		text = Jsoup.clean(uncleanHtml, Whitelist.basic());
	}

	public void addAttachment(String attName, String contentType) {
		attachmentsHash.put(attName, contentType);
	}

	public Map<String, String> getAttachmentsHash() {
		return attachmentsHash;
	}

}
