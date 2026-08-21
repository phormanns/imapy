package de.jalin.webmail;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import de.jalin.imap.IMAPySession;

public class WebmailHttpSession {

    final HttpServletRequest request;
    final HttpServletResponse response;
    final IMAPySession imapSession;

    public WebmailHttpSession(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        this.request = request;
        this.response = response;
        final HttpSession httpSession = request.getSession();
        imapSession = (IMAPySession) httpSession.getAttribute("imap");
        if (imapSession == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }

    public IMAPySession getSession() {
        return imapSession;
    }

    public void dispatchTo(final String jsp) throws ServletException, IOException {
        request.getRequestDispatcher(jsp).forward(request, response);
    }

}
