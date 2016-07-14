package de.jalin.webmail;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MailboxServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
    public MailboxServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			final WebmailHttpSession imapySession = new WebmailHttpSession(request, response);
			request.getSession().setAttribute("folders", imapySession.getSession().getFolders());
			imapySession.dispatchTo("/WEB-INF/jsp/mailbox.jsp");
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

}
