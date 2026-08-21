package de.jalin.webmail;

import java.io.IOException;

import jakarta.mail.AuthenticationFailedException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import de.jalin.imap.IMAPyException;
import de.jalin.imap.IMAPySession;
import de.jalin.imap.text.EmailValidator;
import de.jalin.webmail.impl.AutoconfigMailboxFinder;
import de.jalin.webmail.impl.HostsharingMailboxFinder;

public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public LoginServlet() {
        super();
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final HttpSession session = request.getSession();
        final String emailAddr = request.getParameter("email");
        if (emailAddr == null || emailAddr.length() < 5) {
            response.sendRedirect("login.jsp?error=invalid");
            return;
        }
        final String password = request.getParameter("password");
        if (password == null || password.length() < 3) {
            response.sendRedirect("login.jsp?error=invalid");
            return;
        }
        MailboxFinder mbxFinder;
        if (emailAddr.contains("@")) {
            if (!EmailValidator.isValidEmailAddress(emailAddr)) {
                response.sendRedirect("login.jsp?error=invalid");
                return;
            }
            mbxFinder = new AutoconfigMailboxFinder();
        } else {
            mbxFinder = new HostsharingMailboxFinder();
        }
        try {
            mbxFinder.setLogin(emailAddr);
            final String host = mbxFinder.getHost();
            final String user = mbxFinder.getUser();
            session.setAttribute("email", emailAddr);
            session.setAttribute("max_list_length", "300");
            session.setAttribute("imap", new IMAPySession(host, user, password));
            response.sendRedirect("mailbox");
        } catch (IMAPyException e) {
            response.sendRedirect("login.jsp?error=invalid");
        }

    }

}
