package de.jalin.webmail;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.jalin.imap.IMAPySession;
import de.jalin.imap.IMAPyException;

public class FolderServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
    public FolderServlet() {
        super();
    }

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
		try {
			final WebmailHttpSession imapySession = new WebmailHttpSession(request, response);
			final IMAPySession imap = imapySession.getSession();
			if (imap == null) {
				return;
			}
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
