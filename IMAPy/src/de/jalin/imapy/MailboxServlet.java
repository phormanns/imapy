package de.jalin.imapy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.jalin.imap.IMAP;

public class MailboxServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
    public MailboxServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			final HttpSession session = request.getSession();
			final IMAP imap = (IMAP) session.getAttribute("imap");
			if (imap == null) {
				response.sendRedirect(request.getContextPath() + "/login.html");
			} else {
				session.setAttribute("folders", imap.getFolders());
				request.getRequestDispatcher("/WEB-INF/jsp/mailbox.jsp").forward(request, response);
			}
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

}
