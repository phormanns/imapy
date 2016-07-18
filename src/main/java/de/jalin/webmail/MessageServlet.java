package de.jalin.webmail;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.jalin.imap.IMAPySession;
import de.jalin.imap.mime.AttachmentsCollector;
import de.jalin.imap.IMAPyException;
import de.jalin.imap.IMAPyMessage;

public class MessageServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
    public MessageServlet() {
        super();
    }

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
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
			final String folder = pathSplit[0];
			final String msgIndex = pathSplit[1];
			if (pathSplit.length == 4 && pathSplit[2].equals("moveto")) {
				imap.moveMessageToFolder(folder, msgIndex, pathSplit[3]);
				return;
			}
			if ("confirmdel".equals(request.getParameter("msgop"))) {
				imap.removeMessage(folder, msgIndex, messageId);
				if ("true".equals(session.getAttribute("mobile"))) {
					response.sendRedirect(request.getContextPath() + "/folder/" + folder);
				} else {
					imapySession.dispatchTo("/WEB-INF/jsp/folder-reload.jsp");
				}
				return;
			}
			session.setAttribute("folder", folder);
			final AttachmentsCollector collector = new AttachmentsCollector();
			final IMAPyMessage yMessage = imap.getMessage(folder, msgIndex, collector);
			session.setAttribute("message", yMessage);
			yMessage.addAttachments(collector.getAttachmentsList());
			imapySession.dispatchTo("/WEB-INF/jsp/message.jsp");
		} catch (IOException e) {
			throw new ServletException(e);
		} catch (IMAPyException e) {
			throw new ServletException(e);
		}
	}

}
