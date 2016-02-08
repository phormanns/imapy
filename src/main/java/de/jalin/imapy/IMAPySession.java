package de.jalin.imapy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.jalin.imap.IMAP;

public class IMAPySession {

	final HttpServletRequest request;
	final HttpServletResponse response;
	final IMAP imapSession;
	
	public IMAPySession(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		this.request = request;
		this.response = response;
		final HttpSession httpSession = request.getSession();
		imapSession = (IMAP) httpSession.getAttribute("imap");
		if (imapSession == null) {
			response.sendRedirect(request.getContextPath() + "/login.jsp");
		}
	}
	
	public IMAP getSession() {
		return imapSession;
	}
	
	public void dispatchTo(final String jsp) throws ServletException, IOException {
		request.getRequestDispatcher(jsp).forward(request, response);
	}
	
}
