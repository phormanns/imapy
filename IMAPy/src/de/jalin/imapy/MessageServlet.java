package de.jalin.imapy;

import java.io.IOException;

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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			HttpSession session = request.getSession();
			IMAP imap = (IMAP) session.getAttribute("imap");
			if (imap == null) {
				response.sendRedirect(request.getContextPath() + "/login.html");
			} else {
				String pathInfo = request.getPathInfo().substring(1);
				int slashIndex = pathInfo.indexOf('/');
				String folder = pathInfo.substring(0, slashIndex);
				String msgIndex = pathInfo.substring(slashIndex + 1);
				session.setAttribute("message", imap.getMessage(folder, msgIndex));
				request.getRequestDispatcher("/WEB-INF/jsp/message.jsp").forward(request, response);
			}
		} catch (IOException e) {
			throw new ServletException(e);
		} catch (IMAPyException e) {
			throw new ServletException(e);
		}
	}

}
