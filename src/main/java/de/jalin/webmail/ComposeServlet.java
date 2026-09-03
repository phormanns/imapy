package de.jalin.webmail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import de.jalin.imap.IMAPyException;
import de.jalin.imap.MailAttachment;
import de.jalin.imap.SMTPySession;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 25L * 1024 * 1024,
        maxRequestSize = 30L * 1024 * 1024
)
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
            final List<MailAttachment> attachments = collectAttachments(request);
            try {
                smtp.sendMail(from, to, subject, body, inReplyTo, references, attachments);
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
    private List<MailAttachment> collectAttachments(final HttpServletRequest request) throws IOException, ServletException {
        final List<MailAttachment> attachments = new ArrayList<>();
        for (final Part part : request.getParts()) {
            if (!"attachment".equals(part.getName())) {
                continue;
            }
            final String submittedName = part.getSubmittedFileName();
            if (submittedName == null || submittedName.isBlank()) {
                continue;
            }
            final byte[] data;
            try (InputStream in = part.getInputStream()) {
                data = in.readAllBytes();
            }
            if (data.length == 0) {
                continue;
            }
            String fileName = submittedName.replace('\\', '/');
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1).trim();
            if (fileName.isEmpty()) {
                fileName = "Anhang";
            }
            attachments.add(new MailAttachment(fileName, part.getContentType(), data));
        }
        return attachments;
    }

}
