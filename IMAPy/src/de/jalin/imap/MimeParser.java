package de.jalin.imap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class MimeParser {

	public static final DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

	public static MessageData parseMimeMessage(MimeMessage mimeMsg) {
		String fromEMail = getFromAddress(mimeMsg);
		String subject = getSubject(mimeMsg);
		Date sentDate = getSentDate(mimeMsg);
		String messageID = getMessageID(mimeMsg);
		MessageData msg = new MessageData(fromEMail, subject, 
				df.format(sentDate),
				messageID);
		msg.setAttached("");
		msg.setText("");
		try {
			Flags flags = mimeMsg.getFlags();
			boolean seen = flags.contains(Flags.Flag.SEEN);
			msg.setNew(!seen);
			msg.setFlagged(flags.contains(Flags.Flag.FLAGGED));
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
		}
		try {
			Object contentObj;
			try {
				contentObj = mimeMsg.getContent();
				if (contentObj instanceof String) {
					msg.setText((String) contentObj);
				} else if (contentObj instanceof Multipart) {
					Multipart multipartContent = (Multipart) contentObj;
					parseMultipart(msg, multipartContent);
				} else if (contentObj instanceof InputStream) {
					String type = mimeMsg.getContentType();
					InputStream streamContent = (InputStream) contentObj;
					msg.setText("IMAPInputStream type=" + type);
					if (type.startsWith("text/")) {
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(streamContent));
						String l = reader.readLine();
						StringBuffer textBuffer = new StringBuffer();
						while (l != null) {
							textBuffer.append(l);
							textBuffer.append('\n');
							l = reader.readLine();
						}
						msg.setText(textBuffer.toString());
					}
				} else {
					msg.setText("Unbekannter Inhalt");
				}
			} catch (MessagingException e) {
				msg.setText("Fehler beim Lesen des Inhalts der Nachricht");
			}
		} catch (IOException e) {
			msg.setText("I-/O-Fehler beim Lesen der Nachricht");
		}
		return msg;
	}

	public static String getFromAddress(Message msg) {
		try {
			Address address = msg.getFrom()[0];
			String fromEMail = 
				address instanceof InternetAddress 
					? ((InternetAddress) address).getAddress().toLowerCase()
					: address.toString();
			return fromEMail;
		} catch (MessagingException e) {
			return "Unbekannter Absender";
		}
	}

	public static Date getSentDate(Message mimeMsg) {
		try {
			return mimeMsg.getSentDate();
		} catch (MessagingException e) {
			return new Date();
		}
	}

	public static String getMessageID(MimeMessage mimeMsg) {
		try {
			String messageID = mimeMsg.getMessageID();
			return messageID.substring(1, messageID.length() - 1);
		} catch (NullPointerException e) {
			return Long.toHexString(System.currentTimeMillis())
				+ "@" + Thread.currentThread().toString();
		} catch (MessagingException e) {
			return Long.toHexString(System.currentTimeMillis())
				+ "@" + Thread.currentThread().toString();
		}
	}

	public static String getSubject(Message msg) {
		try {
			String subj = msg.getSubject();
			if (subj != null && subj.length() > 0) {
				return subj;
			} else {
				return "(kein Betreff)";
			}
		} catch (MessagingException e) {
			return "(Nachricht nicht lesbar)";
		}
	}

	private static void parseMultipart(MessageData msg, Multipart multipartContent)
			throws MessagingException, IOException {
		int i = 0;
		int parts = multipartContent.getCount();
		while (i < parts) {
			BodyPart bodyPart = multipartContent.getBodyPart(i);
			String contentType = bodyPart.getContentType();
			if (contentType != null) {
				msg.setAttached(msg.getAttached() + contentType);
				msg.setAttached(msg.getAttached() + "\n");
			} else {
				contentType = "text/plain";
			}
			Object object = bodyPart.getContent();
			if (object instanceof String
					&& msg.getFormattedText().length() < 2) {
				String text = (String) object;
				if (contentType.contains("html")) {
					msg.setHtmlText(text);
				} else {
					msg.setText(text);
				}
			}
			if (object instanceof Multipart) {
				parseMultipart(msg, (Multipart) object);
			}
			i++;
		}
	}

}
