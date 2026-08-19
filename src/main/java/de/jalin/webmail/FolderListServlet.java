package de.jalin.webmail;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.jalin.imap.IMAPySession;

public class FolderListServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public FolderListServlet() {
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
            request.getSession().setAttribute("folders", imap.getFolders());
            final String activeFolder = request.getParameter("folder");
            if (activeFolder != null && !activeFolder.isEmpty()) {
                request.setAttribute("activeFolder", activeFolder);
            }
            imapySession.dispatchTo("/WEB-INF/jsp/folderlist.jsp");
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

}
