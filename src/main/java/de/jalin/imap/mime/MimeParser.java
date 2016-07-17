package de.jalin.imap.mime;

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
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class MimeParser {

	public static final DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

	public static MessageData parseMimeMessage(final MimeMessage mimeMsg) {
		final String fromEMail = getFromAddress(mimeMsg);
		final String subject = getSubject(mimeMsg);
		final Date sentDate = getSentDate(mimeMsg);
		final String messageID = getMessageID(mimeMsg);
		final MessageData msg = new MessageData(fromEMail, subject, df.format(sentDate), messageID);
		try {
			final Flags flags = mimeMsg.getFlags();
			final boolean seen = flags.contains(Flags.Flag.SEEN);
			msg.setNew(!seen);
			msg.setFlagged(flags.contains(Flags.Flag.FLAGGED));
		} catch (MessagingException e) {
			msg.setText("Fehler beim Lesen der Flags " + e.getMessage());
		}
		try {
			BufferedReader reader = null;
			try {
				final Object contentObj = mimeMsg.getContent();
				final String type = mimeMsg.getContentType();
				if (contentObj instanceof String) {
					if (type.startsWith("text/html")) {
						msg.setHtmlText((String) contentObj);
					} else {
						msg.setText((String) contentObj);
					}
				} else if (contentObj instanceof Multipart) {
					final Multipart multipartContent = (Multipart) contentObj;
					parseMultipart(msg, multipartContent);
				} else if (contentObj instanceof InputStream) {
					final InputStream streamContent = (InputStream) contentObj;
					msg.setText("IMAPInputStream type=" + type);
					if (type.startsWith("text/")) {
						reader = new BufferedReader(new InputStreamReader(streamContent));
						String l = reader.readLine();
						final StringBuffer textBuffer = new StringBuffer();
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
				msg.setText("Fehler beim Lesen des Inhalts der Nachricht " + e.getLocalizedMessage());
			} finally {
				if (reader != null) try { reader.close(); } catch (IOException e) { } 
			}
		} catch (IOException e) {
			msg.setText("I-/O-Fehler beim Lesen der Nachricht " + e.getLocalizedMessage());
		}
		return msg;
	}

	public static String getFromAddress(Message msg) {
		try {
			final Address[] allSenders = msg.getFrom();
			if (allSenders == null || allSenders.length < 1) {
				return "Unbekannter Absender";
			}
			final Address address = allSenders[0];
			String fromEMail = 
				address instanceof InternetAddress 
					? ((InternetAddress) address).getAddress().toLowerCase()
					: address.toString();
			return fromEMail;
		} catch (MessagingException e) {
			return "Unbekannter Absender";
		}
	}

	public static String getToAddress(Message msg) {
		try {
			final Address[] allRecipients = msg.getAllRecipients();
			if (allRecipients == null || allRecipients.length < 1) {
				return "Unbekannt";
			}
			final Address address = allRecipients[0];
			String fromEMail = 
				address instanceof InternetAddress 
					? ((InternetAddress) address).getAddress().toLowerCase()
					: address.toString();
			return fromEMail;
		} catch (MessagingException e) {
			return "Unbekannt";
		}
	}

	public static Date getSentDate(Message mimeMsg) {
		try {
			final Date sentDate = mimeMsg.getSentDate();
			if (sentDate == null) {
				return new Date();
			}
			return sentDate;
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

	private static void parseMultipart(MessageData msg, Multipart multipartContent) throws MessagingException, IOException 
	{
		int i = 0;
		final int parts = multipartContent.getCount();
		while (i < parts) {
			final BodyPart bodyPart = multipartContent.getBodyPart(i);
			final String disposition = bodyPart.getDisposition();
			String contentType = bodyPart.getContentType();
			if (contentType != null && ( Part.ATTACHMENT.equalsIgnoreCase(disposition) || Part.INLINE.equalsIgnoreCase(disposition) )) {
				String attName = "attachment" + i;
				if (bodyPart.getFileName() != null && !bodyPart.getFileName().isEmpty()) {
					attName = bodyPart.getFileName();
				}
				msg.addAttachment(attName, contentType);
			} else {
				contentType = "text/plain";
			}
			final Object object = bodyPart.getContent();
			if (object instanceof String && msg.getFormattedText().length() < 17) {
				final String text = (String) object;
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
