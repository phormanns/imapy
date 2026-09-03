package de.jalin.webmail;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import de.jalin.imap.IMAPySession;

public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public LogoutServlet() {
        super();
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            final Object imapSession = session.getAttribute("imap");
            if (imapSession instanceof IMAPySession) {
                ((IMAPySession) imapSession).disconnect();
            }
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

}
