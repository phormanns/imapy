package de.jalin.imap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import de.jalin.imap.mime.AttachmentsCollector;
import de.jalin.imap.mime.MimeParser;

import jakarta.activation.DataHandler;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

public class IMAPySessionTest {

    private static final String USER = "paul@example.org";
    private static final String PASSWORD = "geheim";

    private GreenMail greenMail;
    private IMAPySession imap;

    @BeforeEach
    public void startImapServer() throws Exception {
        greenMail = new GreenMail(new ServerSetup(0, "127.0.0.1", "imap"));
        greenMail.start();
        greenMail.setUser(USER, PASSWORD);
        final Store store = newRawStore();
        store.connect("127.0.0.1", USER, PASSWORD);
        store.getFolder("INBOX").setSubscribed(true);
        store.close();
    }

    @AfterEach
    public void tearDown() {
        if (imap != null) {
            imap.disconnect();
        }
        greenMail.stop();
    }

    private Store newRawStore() throws NoSuchProviderException {
        final Properties props = new Properties();
        props.setProperty("mail.imap.host", "127.0.0.1");
        props.setProperty("mail.imap.port", String.valueOf(greenMail.getImap().getPort()));
        return Session.getInstance(props).getStore("imap");
    }

    private IMAPySession newSession() throws IMAPyException, NoSuchProviderException {
        return new IMAPySession(newRawStore(), "127.0.0.1", USER, PASSWORD);
    }

    private GreenMailUser user() {
        return greenMail.getUserManager().getUser(USER);
    }

    private MimeMessage deliveredMessage(String from, String subject, String body) throws Exception {
        final MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()));
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(USER, false));
        msg.setSubject(subject, "UTF-8");
        msg.setText(body, "UTF-8");
        msg.setSentDate(new Date());
        msg.setHeader("Message-ID", "<" + subject.replace(" ", "-").toLowerCase() + "@example.org>");
        msg.saveChanges();
        user().deliver(msg);
        return msg;
    }

    private void createFolder(String name) throws Exception {
        final Store store = newRawStore();
        store.connect("127.0.0.1", USER, PASSWORD);
        final Folder folder = store.getFolder(name);
        folder.create(Folder.HOLDS_MESSAGES);
        folder.setSubscribed(true);
        store.close();
    }

    @Test
    public void listsInboxWithMessageCounts() throws Exception {
        deliveredMessage("alice@example.org", "Erste Nachricht", "hallo");
        deliveredMessage("bob@example.org", "Zweite Nachricht", "welt");
        imap = newSession();
        final List<IMAPyFolder> folders = imap.getFolders();
        assertEquals(1, folders.size());
        final IMAPyFolder inbox = folders.get(0);
        assertEquals("INBOX", inbox.getName());
        assertEquals(2, inbox.getTotalMessageCount());
        assertEquals(2, inbox.getUnreadMessageCount());
    }

    @Test
    public void listsStandardFoldersFirst() throws Exception {
        createFolder("Archiv");
        createFolder("Sent");
        createFolder("Drafts");
        createFolder("Junk");
        createFolder("Trash");
        createFolder("INBOX.Sent");
        imap = newSession();
        final List<IMAPyFolder> folders = imap.getFolders();
        final List<String> names = folders.stream().map(IMAPyFolder::getName).toList();
        assertEquals("INBOX", names.get(0));
        assertTrue(names.indexOf("Sent") < names.indexOf("INBOX.Sent"));
        assertTrue(names.indexOf("Sent") < names.indexOf("Drafts"));
        assertTrue(names.indexOf("Drafts") < names.indexOf("Junk"));
        assertTrue(names.indexOf("Junk") < names.indexOf("Trash"));
        assertTrue(names.indexOf("Trash") < names.indexOf("Archiv"));
        assertTrue(names.indexOf("INBOX.Sent") < names.indexOf("Archiv"));
        assertEquals("Archiv", names.get(names.size() - 1));
    }

    @Test
    public void listsMessagesNewestFirst() throws Exception {
        deliveredMessage("alice@example.org", "Eins", "eins");
        deliveredMessage("alice@example.org", "Zwei", "zwei");
        deliveredMessage("alice@example.org", "Drei", "drei");
        imap = newSession();
        final List<IMAPyMessage> messages = imap.getMessages("INBOX");
        assertEquals(3, messages.size());
        assertEquals("Drei", messages.get(0).getTitle());
        assertEquals("Zwei", messages.get(1).getTitle());
        assertEquals("Eins", messages.get(2).getTitle());
        assertEquals("alice@example.org", messages.get(0).getAuthor());
        assertEquals("INBOX", messages.get(0).getFolder());
        assertEquals(IMAPySession.NEW, messages.get(0).getStatus());
        assertNotEquals(0, messages.get(0).getUid());
        assertNotNull(messages.get(0).getDate());
    }

    @Test
    public void readsMessageAndMarksItSeen() throws Exception {
        deliveredMessage("alice@example.org", "Hallo Bob", "Hallo Bob, gruesse Alice");
        imap = newSession();
        final long uid = imap.getMessages("INBOX").get(0).getUid();
        final IMAPyMessage full = imap.getMessage("INBOX", String.valueOf(uid), new AttachmentsCollector());
        assertTrue(full.getContent().contains("gruesse Alice"));
        assertEquals("alice@example.org", full.getFrom());
        assertNotNull(full.getMessageId());
        final List<IMAPyMessage> afterRead = imap.getMessages("INBOX");
        assertEquals(IMAPySession.SEEN, afterRead.get(0).getStatus());
    }

    @Test
    public void readsMessageWithAttachment() throws Exception {
        final MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()));
        msg.setFrom(new InternetAddress("alice@example.org"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(USER, false));
        msg.setSubject("Mit Anhang", "UTF-8");
        msg.setSentDate(new Date());
        msg.setHeader("Message-ID", "<anhang-1@example.org>");
        final MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Anbei der Bericht.", "UTF-8");
        final MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(new DataHandler(
                new ByteArrayDataSource("PDF-DOKUMENT".getBytes(StandardCharsets.UTF_8), "application/pdf")));
        attachmentPart.setFileName("bericht.pdf");
        final MimeMultipart mixed = new MimeMultipart("mixed");
        mixed.addBodyPart(textPart);
        mixed.addBodyPart(attachmentPart);
        msg.setContent(mixed);
        msg.saveChanges();
        user().deliver(msg);

        imap = newSession();
        final long uid = imap.getMessages("INBOX").get(0).getUid();
        final AttachmentsCollector collector = new AttachmentsCollector();
        final IMAPyMessage full = imap.getMessage("INBOX", String.valueOf(uid), collector);
        assertTrue(full.getContent().contains("Anbei der Bericht."));
        assertTrue(collector.getAttachmentsList().containsKey("bericht.pdf"));
    }

    @Test
    public void removesMessageWithMatchingMessageId() throws Exception {
        final MimeMessage delivered = deliveredMessage("alice@example.org", "Loesch mich", "bitte loeschen");
        imap = newSession();
        final long uid = imap.getMessages("INBOX").get(0).getUid();
        imap.removeMessage("INBOX", String.valueOf(uid), MimeParser.getMessageID(delivered));
        assertEquals(0, imap.getMessages("INBOX").size());
    }

    @Test
    public void keepsMessageWhenMessageIdDoesNotMatch() throws Exception {
        deliveredMessage("alice@example.org", "Bleib liegen", "nicht loeschen");
        imap = newSession();
        final long uid = imap.getMessages("INBOX").get(0).getUid();
        imap.removeMessage("INBOX", String.valueOf(uid), "andere-id@example.org");
        assertEquals(1, imap.getMessages("INBOX").size());
    }

    @Test
    public void movesMessageToTargetFolder() throws Exception {
        createFolder("Archiv");
        deliveredMessage("alice@example.org", "Verschieb mich", "ab ins archiv");
        imap = newSession();
        final long uid = imap.getMessages("INBOX").get(0).getUid();
        final IMAPyMessage moved = imap.moveMessageToFolder("INBOX", String.valueOf(uid), "Archiv");
        assertEquals("Archiv", moved.getFolder());
        assertEquals(0, imap.getMessages("INBOX").size());
        assertEquals(1, imap.getMessages("Archiv").size());
        assertEquals("Archiv", imap.getMessages("Archiv").get(0).getFolder());
    }

    @Test
    public void reconnectsAfterDisconnect() throws Exception {
        deliveredMessage("alice@example.org", "Nach dem Trennen", "noch da");
        imap = newSession();
        assertEquals(1, imap.getMessages("INBOX").size());
        imap.disconnect();
        assertEquals(1, imap.getMessages("INBOX").size());
    }

    @Test
    public void throwsForUnknownFolder() throws Exception {
        imap = newSession();
        assertThrows(IMAPyException.class, () -> imap.getMessages("Unbekannt"));
    }

    @Test
    public void throwsForInvalidUid() throws Exception {
        imap = newSession();
        assertThrows(IMAPyException.class, () -> imap.getMessage("INBOX", "keine-zahl", new AttachmentsCollector()));
    }

}
