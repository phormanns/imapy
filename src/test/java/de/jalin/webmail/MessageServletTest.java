package de.jalin.webmail;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.jalin.imap.IMAPySession;
import de.jalin.imap.IMAPyMessage;

import jakarta.servlet.ServletException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class MessageServletTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession httpSession;
    private IMAPySession imap;
    private IMAPyMessage message;
    private RequestDispatcher dispatcher;
    private MessageServlet servlet;

    @BeforeEach
    public void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        httpSession = mock(HttpSession.class);
        imap = mock(IMAPySession.class);
        message = mock(IMAPyMessage.class);
        dispatcher = mock(RequestDispatcher.class);
        servlet = new MessageServlet();
        when(request.getSession()).thenReturn(httpSession);
        when(httpSession.getAttribute("imap")).thenReturn(imap);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    public void forwardsMessageJspOnGet() throws Exception {
        when(request.getPathInfo()).thenReturn("/INBOX/42");
        when(imap.getMessage(eq("INBOX"), eq("42"), any())).thenReturn(message);

        servlet.doGet(request, response);

        verify(httpSession).setAttribute("folder", "INBOX");
        verify(httpSession).setAttribute("message", message);
        verify(message).addAttachments(any());
        verify(response).setHeader("HX-Trigger", "messages-changed");
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void rejectsGetWithoutPathInfo() {
        when(request.getPathInfo()).thenReturn(null);
        assertThrows(ServletException.class, () -> servlet.doGet(request, response));
    }

    @Test
    public void rejectsGetWithWrongPathSegments() {
        when(request.getPathInfo()).thenReturn("/INBOX/42/extra");
        assertThrows(ServletException.class, () -> servlet.doGet(request, response));
    }

    @Test
    public void movesMessageOnMovetoPath() throws Exception {
        when(request.getPathInfo()).thenReturn("/INBOX/42/moveto/Archiv");

        servlet.doPost(request, response);

        verify(imap).moveMessageToFolder("INBOX", "42", "Archiv");
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    public void confirmsDelete() throws Exception {
        final IMAPyMessage stored = mock(IMAPyMessage.class);
        when(stored.getMessageId()).thenReturn("id-1");
        when(httpSession.getAttribute("message")).thenReturn(stored);
        when(request.getPathInfo()).thenReturn("/INBOX/42");
        when(request.getParameter("msgop")).thenReturn("confirmdel");
        when(request.getRequestDispatcher("/WEB-INF/jsp/message-deleted.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(imap).removeMessage("INBOX", "42", "id-1");
        verify(httpSession).setAttribute("folder", "INBOX");
        verify(httpSession).setAttribute("deletedFolder", "INBOX");
        verify(httpSession).setAttribute("deletedMessageSubject", stored.getSubject());
        verify(response).setHeader("HX-Trigger", "messages-changed");
        verify(response).setContentType("text/html;charset=UTF-8");
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void showsMessageByDefaultOnPost() throws Exception {
        when(request.getPathInfo()).thenReturn("/INBOX/42");
        when(request.getParameter("msgop")).thenReturn(null);
        when(imap.getMessage(eq("INBOX"), eq("42"), any())).thenReturn(message);

        servlet.doPost(request, response);

        verify(dispatcher).forward(request, response);
        verify(imap, never()).removeMessage(anyString(), anyString(), anyString());
    }

}
