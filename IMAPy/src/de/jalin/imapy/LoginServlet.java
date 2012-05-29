package de.jalin.imapy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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

public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
    public LoginServlet() {
        super();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String emailAddr = request.getParameter("email");
		if (emailAddr == null || !emailAddr.contains("@")) {
			throw new ServletException("no valid email address given");
		}
		String password = request.getParameter("password");
		if (password == null || password.length() < 3) {
			throw new ServletException("no valid password given");
		}
		URL url = new URL("http://autoconfig." + emailAddr.split("@")[1] 
				+ "/mail/config-v1.1.xml?emailaddress=" + emailAddr);
		InputStream autoconfigStream = url.openConnection().getInputStream();
		String host = null;
		String user = null;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(autoconfigStream);
			NodeList inServersNodes = document.getElementsByTagName("incomingServer");
			int listLength = inServersNodes.getLength();
			for (int idx = 0; idx < listLength; idx++) {
				Node node = inServersNodes.item(idx);
				Node item = node.getAttributes().getNamedItem("type");
				if (item != null && "imap".equals(item.getNodeValue())) {
					NodeList childNodes = node.getChildNodes();
					int childsListLength = childNodes.getLength();
					for (int childsIdx = 0; childsIdx < childsListLength; childsIdx++) {
						Node child = childNodes.item(childsIdx);
						if ("hostname".equals(child.getNodeName())) {
							host = child.getTextContent();
						}
						if ("username".equals(child.getNodeName())) {
							user = child.getTextContent();
						}
					}
				}
			}
			HttpSession session = request.getSession();
			session.setAttribute("host", host);
			session.setAttribute("user", user);
			session.setAttribute("password", password);
			session.setAttribute("email", emailAddr);
			response.sendRedirect("webmail.html");
		} catch (ParserConfigurationException e) {
			throw new ServletException(e);
		} catch (SAXException e) {
			throw new ServletException(e);
		} finally {
			autoconfigStream.close();
		}
		
	}

}
