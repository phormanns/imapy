package de.jalin.webmail;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.jalin.imap.IMAPyException;
import de.jalin.imap.IMAPySession;

public class MailboxServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public MailboxServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            final WebmailHttpSession imapySession = new WebmailHttpSession(request, response);
            final IMAPySession imapSession = imapySession.getSession();
            if (imapSession == null) {
                return;
            }
            final HttpSession httpSession = request.getSession();
            httpSession.setAttribute("messages", imapSession.getMessages("INBOX"));
            imapySession.dispatchTo("/WEB-INF/jsp/mailbox.jsp");
        } catch (IOException | IMAPyException e) {
            throw new ServletException(e);
        }
    }

}
