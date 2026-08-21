package de.jalin.webmail;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import de.jalin.imap.IMAPySession;
import de.jalin.imap.mime.AttachmentsCollector;
import de.jalin.imap.IMAPyException;
import de.jalin.imap.IMAPyMessage;

public class MessageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public MessageServlet() {
        super();
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        try {
            final WebmailHttpSession imapySession = new WebmailHttpSession(request, response);
            final IMAPySession imap = imapySession.getSession();
            if (imap == null) {
                return;
            }
            final HttpSession session = request.getSession();
            final String pathInfo = request.getPathInfo().substring(1);
            final String[] pathSplit = pathInfo.split("/");
            if (pathSplit.length != 2) {
                throw new ServletException("servlet path error");
            }
            final String folder = pathSplit[0];
            final String msgIndex = pathSplit[1];
            session.setAttribute("folder", folder);
            final AttachmentsCollector collector = new AttachmentsCollector();
            final IMAPyMessage yMessage = imap.getMessage(folder, msgIndex, collector);
            session.setAttribute("message", yMessage);
            yMessage.addAttachments(collector.getAttachmentsList());
            response.setHeader("HX-Trigger", "messages-changed");
            imapySession.dispatchTo("/WEB-INF/jsp/message.jsp");
        } catch (IOException | IMAPyException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        try {
            final WebmailHttpSession imapySession = new WebmailHttpSession(request, response);
            final IMAPySession imap = imapySession.getSession();
            if (imap == null) {
                return;
            }
            final HttpSession session = request.getSession();
            String messageId = null;
            final IMAPyMessage yMsg = (IMAPyMessage) session.getAttribute("message");
            if (yMsg != null) {
                messageId = yMsg.getMessageId();
            }
            final String pathInfo = request.getPathInfo().substring(1);
            final String[] pathSplit = pathInfo.split("/");
            if (pathSplit.length < 2) {
                throw new ServletException("servlet path error");
            }
            final String folder = pathSplit[0];
            final String msgIndex = pathSplit[1];
            if (pathSplit.length == 4 && pathSplit[2].equals("moveto")) {
                imap.moveMessageToFolder(folder, msgIndex, pathSplit[3]);
                return;
            }
            if ("confirmdel".equals(request.getParameter("msgop"))) {
                imap.removeMessage(folder, msgIndex, messageId);
                session.setAttribute("folder", folder);
                session.setAttribute("deletedFolder", folder);
                session.setAttribute("deletedMessageSubject",
                        yMsg != null ? yMsg.getSubject() : null);
                response.setHeader("HX-Trigger", "messages-changed");
                response.setContentType("text/html;charset=UTF-8");
                request.getRequestDispatcher("/WEB-INF/jsp/message-deleted.jsp")
                        .forward(request, response);
                return;
            }
            session.setAttribute("folder", folder);
            final AttachmentsCollector collector = new AttachmentsCollector();
            final IMAPyMessage yMessage = imap.getMessage(folder, msgIndex, collector);
            session.setAttribute("message", yMessage);
            yMessage.addAttachments(collector.getAttachmentsList());
            response.setHeader("HX-Trigger", "messages-changed");
            imapySession.dispatchTo("/WEB-INF/jsp/message.jsp");
        } catch (IOException | IMAPyException e) {
            throw new ServletException(e);
        }
    }

}
