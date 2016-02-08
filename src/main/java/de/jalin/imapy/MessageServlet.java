package de.jalin.imapy;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.jalin.imap.IMAP;

public class MessageServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
    public MessageServlet() {
        super();
    }

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
		try {
			final IMAPySession imapySession = new IMAPySession(request, response);
			final IMAP imap = imapySession.getSession();
			final HttpSession session = request.getSession();
			@SuppressWarnings("unchecked")
			final Map<String, String > mesgHash = (Map<String, String>) session.getAttribute("message");
			final String messageId = mesgHash.get("message-id");
			final String pathInfo = request.getPathInfo().substring(1);
			int slashIndex = pathInfo.indexOf('/');
			final String folder = pathInfo.substring(0, slashIndex);
			final String msgIndex = pathInfo.substring(slashIndex + 1);
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
			session.setAttribute("message", imap.getMessage(folder, msgIndex));
			imapySession.dispatchTo("/WEB-INF/jsp/message.jsp");
		} catch (IOException e) {
			throw new ServletException(e);
		} catch (IMAPyException e) {
			throw new ServletException(e);
		}
	}

}
