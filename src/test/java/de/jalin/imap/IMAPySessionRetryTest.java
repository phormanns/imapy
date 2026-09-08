package de.jalin.imap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.jalin.imap.mime.AttachmentsCollector;

import jakarta.mail.Folder;
import jakarta.mail.FolderClosedException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;

public class IMAPySessionRetryTest {

    private Store store;
    private Folder inbox;

    @BeforeEach
    public void setUp() throws Exception {
        store = mock(Store.class);
        inbox = mock(Folder.class);
        when(inbox.getFullName()).thenReturn("INBOX");
        when(inbox.getName()).thenReturn("INBOX");
        when(inbox.getType()).thenReturn(Folder.HOLDS_MESSAGES);
        final Folder root = mock(Folder.class);
        when(root.listSubscribed()).thenReturn(new Folder[]{inbox});
        when(store.getDefaultFolder()).thenReturn(root);
        when(store.isConnected()).thenReturn(true);
    }

    private IMAPySession newSession() throws IMAPyException {
        return new IMAPySession(store, "imap.test.example.org", "paul", "geheim");
    }

    @Test
    public void connectsWhenStoreNotConnected() throws Exception {
        when(store.isConnected()).thenReturn(false, true);
        newSession();
        verify(store).connect("imap.test.example.org", "paul", "geheim");
    }

    @Test
    public void doesNotRetryWhenErrorIsNoConnectionFailure() throws Exception {
        when(store.getFolder("INBOX")).thenThrow(new MessagingException("kaputt"));
        final IMAPySession imap = newSession();
        assertThrows(IMAPyException.class, () -> imap.getMessages("INBOX"));
        verify(store, times(1)).getFolder("INBOX");
        verify(store, never()).connect(anyString(), anyString(), anyString());
    }

    @Test
    public void retriesOnceAfterFolderWasClosed() throws Exception {
        when(store.isConnected()).thenReturn(true, false, true);
        when(store.getFolder("INBOX")).thenReturn(inbox);
        doThrow(new FolderClosedException(inbox, "geschlossen")).doNothing().when(inbox).open(anyInt());
        when(inbox.getMessages()).thenReturn(new Message[0]);
        final IMAPySession imap = newSession();
        final List<IMAPyMessage> messages = imap.getMessages("INBOX");
        assertTrue(messages.isEmpty());
        verify(store, times(1)).connect(anyString(), anyString(), anyString());
        verify(inbox, times(2)).open(anyInt());
    }

    @Test
    public void givesUpAfterSecondFailure() throws Exception {
        when(store.isConnected()).thenReturn(true, false, true);
        when(store.getFolder("INBOX")).thenReturn(inbox);
        doThrow(new FolderClosedException(inbox, "geschlossen")).when(inbox).open(anyInt());
        final IMAPySession imap = newSession();
        assertThrows(IMAPyException.class, () -> imap.getMessages("INBOX"));
        verify(inbox, times(2)).open(anyInt());
        verify(store, times(1)).connect(anyString(), anyString(), anyString());
    }

    @Test
    public void reportsMinus999CountsOnFolderError() throws Exception {
        when(store.getFolder("INBOX")).thenThrow(new MessagingException("kaputt"));
        final IMAPySession imap = newSession();
        final List<IMAPyFolder> folders = imap.getFolders();
        assertEquals(1, folders.size());
        assertEquals("INBOX", folders.get(0).getName());
        assertEquals(-999, folders.get(0).getTotalMessageCount());
        assertEquals(-999, folders.get(0).getUnreadMessageCount());
    }

    @Test
    public void throwsForUnknownFolderWithoutStoreAccess() throws Exception {
        final IMAPySession imap = newSession();
        assertThrows(IMAPyException.class, () -> imap.getMessages("Unbekannt"));
        verify(store, never()).getFolder(anyString());
    }

    @Test
    public void throwsForInvalidUidWithoutStoreAccess() throws Exception {
        final IMAPySession imap = newSession();
        assertThrows(IMAPyException.class, () -> imap.getMessage("INBOX", "keine-zahl", new AttachmentsCollector()));
        verify(store, never()).getFolder(anyString());
    }

    @Test
    public void disconnectClosesStoreAndSwallowsErrors() throws Exception {
        doThrow(new MessagingException("boom")).when(store).close();
        final IMAPySession imap = newSession();
        assertDoesNotThrow(imap::disconnect);
        verify(store, times(1)).close();
    }

}
