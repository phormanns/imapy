package de.jalin.webmail;

import de.jalin.imap.IMAPyException;
import de.jalin.imap.IMAPyMessage;
import de.jalin.imap.IMAPySession;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FolderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public FolderServlet() {
        super();
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        try {
            final WebmailHttpSession imapySession = new WebmailHttpSession(request, response);
            final IMAPySession imap = imapySession.getSession();
            if (imap == null) {
                return;
            }
            final String pathInfo = request.getPathInfo();
            final String folderName = pathInfo.substring(1);
            final List<IMAPyMessage> messages = imap.getMessages(folderName);
            request.getSession().setAttribute("messages", messages);
            request.setAttribute("folderName", folderName);
            imapySession.dispatchTo("/WEB-INF/jsp/folder.jsp");
        } catch (IOException | IMAPyException e) {
            throw new ServletException(e);
        }
    }

}
