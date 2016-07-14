package de.jalin.webmail;

import java.io.IOException;

import javax.mail.AuthenticationFailedException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.jalin.imap.IMAPyException;
import de.jalin.imap.IMAPySession;
import de.jalin.webmail.impl.AutoconfigMailboxFinder;
import de.jalin.webmail.impl.HostsharingMailboxFinder;

public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
    public LoginServlet() {
        super();
    }

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final HttpSession session = request.getSession();
		final String emailAddr = request.getParameter("email");
		if (emailAddr == null || emailAddr.length() < 5) {
			throw new ServletException("no valid email address given");
		}
		final String password = request.getParameter("password");
		if (password == null || password.length() < 3) {
			throw new ServletException("no valid password given");
		}
		final String mobile = request.getParameter("mobile");
		session.setAttribute("mobile", mobile);
		String host = null;
		String user = null;
		try {
			MailboxFinder mbxFinder;
			if (emailAddr.contains("@")) {
				mbxFinder = new AutoconfigMailboxFinder();
			} else {
				mbxFinder = new HostsharingMailboxFinder();
			}
			mbxFinder.setLogin(emailAddr, password);
			host = mbxFinder.getHost();
			user = mbxFinder.getUser();
			session.setAttribute("email", emailAddr);
			session.setAttribute("imap", new IMAPySession(host, user, password));
			if ("true".equals(mobile)) {
				response.sendRedirect("mailbox");
			} else {
				response.sendRedirect("desktop.html");
			}
		} catch (IMAPyException e) {
			if (e.getCause() instanceof AuthenticationFailedException) {
				response.sendRedirect("login.jsp");
			} else {
				throw new ServletException(e);
			}
		}
		
	}

}
