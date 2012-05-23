package de.jalin.imap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.jsoup.Jsoup;


public class MessageData {

	private String from;
	private String subject;
	private String sentTimestamp;
	private String messageID;
	private String text;
	private String attached;
	private boolean isNew;
	private boolean isFlagged;

	public MessageData(String from, String subject, String sent, String messageID) {
		this.isNew = true;
		this.isFlagged = false;
		this.setFrom(from);
		this.setSubject(subject);
		this.setSentTimestamp(sent);
		this.messageID = messageID;
		this.setText("");
		this.setAttached("");
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

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getFrom() {
		return from;
	}

	public void setAttached(String attached) {
		this.attached = attached;
	}

	public String getAttached() {
		return attached;
	}

	public void setText(String text) {
		if (text != null && text.length() > 29 && text.substring(0, 29).contains("<html")) {
			setHtmlText(text);
		} else {
			this.text = text;
		}
	}

	public String getFormattedText() {
		int maxlen = 64;
		int blank = maxlen - 10;
		StringBuffer formated = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader(new StringReader(text));
			String s = reader.readLine();
			while (s != null) { // Schleife liest zeilenweise
				while (s.length() > maxlen) { // lange Zeile werden zerlegt
					blank = maxlen - 10; // Blank vor dem Umbruch suchen
					while (blank < s.length() && s.charAt(blank) != ' ') {
						blank++;
					}
					formated.append(TextUtil.replaceEntities(s.substring(0, blank)));
					formated.append('\n');
					s = s.substring(blank);
					while (s.length() > 0 && s.charAt(0) == ' ') {
						s = s.substring(1); // ggf. fuehrende Blank abschneiden
					}
				}
				formated.append(TextUtil.replaceEntities(s));
				formated.append('\n');
				s = reader.readLine(); // naechste Zeile
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return formated.toString();
	}

	public String getText() {
		return text;
	}

	public void setSentTimestamp(String sent) {
		this.sentTimestamp = sent;
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

	public void setHtmlText(String translate) {
		text = Jsoup.parse(translate).text();
	}

}
