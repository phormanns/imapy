package de.jalin.webmail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.jalin.imap.IMAPyException;
import de.jalin.imap.IMAPySession;
import de.jalin.imap.SMTPySession;

import jakarta.servlet.ServletException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class ComposeServletTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession httpSession;
    private SMTPySession smtp;
    private RequestDispatcher dispatcher;
    private ComposeServlet servlet;

    @BeforeEach
    public void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        httpSession = mock(HttpSession.class);
        smtp = mock(SMTPySession.class);
        dispatcher = mock(RequestDispatcher.class);
        servlet = new ComposeServlet();
        when(request.getSession()).thenReturn(httpSession);
        when(httpSession.getAttribute("imap")).thenReturn(mock(IMAPySession.class));
        when(httpSession.getAttribute("smtp")).thenReturn(smtp);
        when(httpSession.getAttribute("from")).thenReturn("paul@example.org");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    public void rejectsPostWithoutSmtpSession() throws Exception {
        when(httpSession.getAttribute("smtp")).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Kein SMTP-Zugang eingerichtet");
    }

    @Test
    public void rejectsPostWithoutRecipient() throws Exception {
        when(request.getParameter("to")).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Empfaenger fehlt");
    }

    @Test
    public void rejectsPostWithBlankRecipient() throws Exception {
        when(request.getParameter("to")).thenReturn("   ");
        servlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Empfaenger fehlt");
    }

    @Test
    public void sendsMailAndConfirmsDispatch() throws Exception {
        when(request.getParameter("to")).thenReturn("chef@example.org");
        when(request.getParameter("subject")).thenReturn("Betreff");
        when(request.getParameter("body")).thenReturn("Inhalt");

        servlet.doPost(request, response);

        verify(smtp).sendMail(eq("paul@example.org"), eq("chef@example.org"), eq("Betreff"), eq("Inhalt"),
                isNull(), isNull(), anyList());
        verify(response).setHeader("HX-Trigger", "messages-changed");
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void reportsFailedDelivery() throws Exception {
        when(request.getParameter("to")).thenReturn("chef@example.org");
        doThrow(new IMAPyException("smtp weg")).when(smtp)
                .sendMail(any(), any(), any(), any(), any(), any(), any());

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Versand fehlgeschlagen");
        verify(dispatcher, never()).forward(any(), any());
    }

}
