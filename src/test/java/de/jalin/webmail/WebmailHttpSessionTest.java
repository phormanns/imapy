package de.jalin.webmail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.jalin.imap.IMAPySession;

import jakarta.servlet.ServletException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class WebmailHttpSessionTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession httpSession;
    private IMAPySession imap;
    private RequestDispatcher dispatcher;

    @BeforeEach
    public void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        httpSession = mock(HttpSession.class);
        imap = mock(IMAPySession.class);
        dispatcher = mock(RequestDispatcher.class);
        when(request.getSession()).thenReturn(httpSession);
    }

    @Test
    public void returnsImapSessionWhenAttributePresent() throws IOException {
        when(httpSession.getAttribute("imap")).thenReturn(imap);
        assertEquals(imap, new WebmailHttpSession(request, response).getSession());
    }

    @Test
    public void redirectsAndThrowsWhenNoImapSessionStored() throws IOException {
        when(httpSession.getAttribute("imap")).thenReturn(null);
        when(request.getContextPath()).thenReturn("/imapy");
        assertThrows(IllegalStateException.class, () -> new WebmailHttpSession(request, response));
        verify(response).sendRedirect("/imapy/login.jsp");
    }

    @Test
    public void dispatchesToJsp() throws ServletException, IOException {
        when(httpSession.getAttribute("imap")).thenReturn(imap);
        when(request.getRequestDispatcher("/WEB-INF/jsp/folder.jsp")).thenReturn(dispatcher);
        new WebmailHttpSession(request, response).dispatchTo("/WEB-INF/jsp/folder.jsp");
        verify(dispatcher).forward(request, response);
    }

}
