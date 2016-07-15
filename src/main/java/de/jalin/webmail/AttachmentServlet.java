package de.jalin.webmail;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.jalin.imap.IMAPySession;
import de.jalin.imap.IMAPyException;

public class AttachmentServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
    public AttachmentServlet() {
        super();
    }

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
		try {
			final WebmailHttpSession imapySession = new WebmailHttpSession(request, response);
			final IMAPySession imap = imapySession.getSession();
			final String pathInfo = request.getPathInfo();
			final String[] pathItems = pathInfo.substring(1).split("/");
			final String folder = pathItems[0]; 
			final String messageIdx = pathItems[1]; 
			final String attachmentName = pathItems[2];
			response.setContentType("text/plain");
			final PrintWriter writer = response.getWriter();
			writer.println("attached text from:");
			writer.println("Folder: " + folder);
			writer.println("Message: " + messageIdx);
			writer.println("Attachment: " + attachmentName);
			writer.close();
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

}
