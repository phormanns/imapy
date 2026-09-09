package de.jalin.webmail;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    public void rejectsNullEmail() throws Exception {
        when(request.getParameter("email")).thenReturn(null);
        new LoginServlet().doPost(request, response);
        verify(response).sendRedirect("login.jsp?error=invalid");
    }

    @Test
    public void rejectsNullPassword() throws Exception {
        when(request.getParameter("email")).thenReturn("paul@example.org");
        when(request.getParameter("password")).thenReturn(null);
        new LoginServlet().doPost(request, response);
        verify(response).sendRedirect("login.jsp?error=invalid");
    }

    @Test
    public void rejectsBlankPassword() throws Exception {
        when(request.getParameter("email")).thenReturn("paul@example.org");
        when(request.getParameter("password")).thenReturn("  ");
        new LoginServlet().doPost(request, response);
        verify(response).sendRedirect("login.jsp?error=invalid");
    }

    @Test
    public void invalidEmailRedirectsBeforeCheckingPassword() throws Exception {
        when(request.getParameter("email")).thenReturn("ab");
        when(request.getParameter("password")).thenReturn(null);
        new LoginServlet().doPost(request, response);
        verify(response).sendRedirect("login.jsp?error=invalid");
        verify(request, never()).getParameter("password");
    }

    @Test
    public void invalidatesSessionOnLogin() throws Exception {
        when(request.getParameter("email")).thenReturn("ab");
        new LoginServlet().doPost(request, response);
        verify(httpSession).invalidate();
    }

}
