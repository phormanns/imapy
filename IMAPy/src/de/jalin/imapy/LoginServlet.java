package de.jalin.imapy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.mail.AuthenticationFailedException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.jalin.imap.IMAP;

public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
    public LoginServlet() {
        super();
    }

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final String emailAddr = request.getParameter("email");
		if (emailAddr == null || !emailAddr.contains("@")) {
			throw new ServletException("no valid email address given");
		}
		final String password = request.getParameter("password");
		if (password == null || password.length() < 3) {
			throw new ServletException("no valid password given");
		}
		final URL url = new URL("http://autoconfig." + emailAddr.split("@")[1] 
				+ "/mail/config-v1.1.xml?emailaddress=" + emailAddr);
		final InputStream autoconfigStream = url.openConnection().getInputStream();
		String host = null;
		String user = null;
		try {
			final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			final Document document = documentBuilder.parse(autoconfigStream);
			final NodeList inServersNodes = document.getElementsByTagName("incomingServer");
			final int listLength = inServersNodes.getLength();
			for (int idx = 0; idx < listLength; idx++) {
				final Node node = inServersNodes.item(idx);
				final Node item = node.getAttributes().getNamedItem("type");
				if (item != null && "imap".equals(item.getNodeValue())) {
					final NodeList childNodes = node.getChildNodes();
					final int childsListLength = childNodes.getLength();
					for (int childsIdx = 0; childsIdx < childsListLength; childsIdx++) {
						final Node child = childNodes.item(childsIdx);
						if ("hostname".equals(child.getNodeName())) {
							host = child.getTextContent();
						}
						if ("username".equals(child.getNodeName())) {
							user = child.getTextContent();
						}
					}
				}
			}
			final HttpSession session = request.getSession();
			session.setAttribute("email", emailAddr);
			session.setAttribute("imap", new IMAP(host, user, password));
			response.sendRedirect("mailbox");
		} catch (ParserConfigurationException e) {
			throw new ServletException(e);
		} catch (SAXException e) {
			throw new ServletException(e);
		} catch (IMAPyException e) {
			if (e.getCause() instanceof AuthenticationFailedException) {
				response.sendRedirect("login.jsp");
			} else {
				throw new ServletException(e);
			}
		} finally {
			autoconfigStream.close();
		}
		
	}

}
