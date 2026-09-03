package de.jalin.webmail;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import de.jalin.imap.IMAPyException;
import de.jalin.imap.SMTPySession;

public class ComposeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public ComposeServlet() {
        super();
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        try {
            final WebmailHttpSession imapySession = new WebmailHttpSession(request, response);
            if (imapySession.getSession() == null) {
                return;
            }
            imapySession.dispatchTo("/WEB-INF/jsp/compose.jsp");
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        try {
            final WebmailHttpSession imapySession = new WebmailHttpSession(request, response);
            if (imapySession.getSession() == null) {
                return;
            }
            final HttpSession httpSession = request.getSession();
            final SMTPySession smtp = (SMTPySession) httpSession.getAttribute("smtp");
            if (smtp == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Kein SMTP-Zugang eingerichtet");
                return;
            }
            final String from = (String) httpSession.getAttribute("from");
            final String to = request.getParameter("to");
            final String subject = request.getParameter("subject");
            final String body = request.getParameter("body");
            final String inReplyTo = request.getParameter("inReplyTo");
            final String references = request.getParameter("references");
            if (to == null || to.isBlank()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empfaenger fehlt");
                return;
            }
            try {
                smtp.sendMail(from, to, subject, body, inReplyTo, references);
            } catch (IMAPyException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Versand fehlgeschlagen");
                return;
            }
            response.setHeader("HX-Trigger", "messages-changed");
            imapySession.dispatchTo("/WEB-INF/jsp/compose-sent.jsp");
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

}
