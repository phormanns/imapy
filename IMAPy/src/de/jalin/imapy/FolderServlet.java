package de.jalin.imapy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.jalin.imap.IMAP;

public class FolderServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
    public FolderServlet() {
        super();
    }

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
		try {
			final IMAPySession imapySession = new IMAPySession(request, response);
			final IMAP imap = imapySession.getSession();
			final String pathInfo = request.getPathInfo();
			request.getSession().setAttribute("messages", imap.getMessages(pathInfo.substring(1)));
			imapySession.dispatchTo("/WEB-INF/jsp/folder.jsp");
		} catch (IOException e) {
			throw new ServletException(e);
		} catch (IMAPyException e) {
			throw new ServletException(e);
		}
	}

}
