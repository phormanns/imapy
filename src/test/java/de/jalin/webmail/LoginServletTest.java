package de.jalin.webmail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginServletTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession httpSession;

    @BeforeEach
    public void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        httpSession = mock(HttpSession.class);
        when(request.getSession()).thenReturn(httpSession);
        when(request.getSession(anyBoolean())).thenReturn(httpSession);
    }

    @Test
    public void computesSmtpHostFromImapHost() {
        assertEquals("smtp.example.org", LoginServlet.smtpHost(null, "imap.example.org"));
    }

    @Test
    public void prefersConfiguredSmtpHost() {
        assertEquals("mail.example.net", LoginServlet.smtpHost("mail.example.net", "imap.example.org"));
    }

    @Test
    public void ignoresBlankConfiguredSmtpHost() {
        assertEquals("smtp.example.org", LoginServlet.smtpHost("  ", "imap.example.org"));
    }

    @Test
    public void keepsImapHostWithoutImapPrefix() {
        assertEquals("example.org", LoginServlet.smtpHost(null, "example.org"));
    }

    @Test
    public void defaultsToStandardSmtpPort() {
        assertEquals(587, LoginServlet.smtpPort(null));
        assertEquals(587, LoginServlet.smtpPort(""));
        assertEquals(587, LoginServlet.smtpPort("  "));
        assertEquals(587, LoginServlet.smtpPort("abc"));
    }

    @Test
    public void parsesConfiguredSmtpPort() {
        assertEquals(465, LoginServlet.smtpPort("465"));
    }

    @Test
    public void rejectsTooShortEmail() throws Exception {
        when(request.getParameter("email")).thenReturn("ab");
        new LoginServlet().doPost(request, response);
        verify(response).sendRedirect("login.jsp?error=invalid");
    }

    @Test
    public void rejectsTooShortPassword() throws Exception {
        when(request.getParameter("email")).thenReturn("paul@example.org");
        when(request.getParameter("password")).thenReturn("xy");
        new LoginServlet().doPost(request, response);
        verify(response).sendRedirect("login.jsp?error=invalid");
    }

}
