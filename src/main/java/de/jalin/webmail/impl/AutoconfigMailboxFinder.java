package de.jalin.webmail.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.jalin.imap.IMAPyException;

public class AutoconfigMailboxFinder extends AbstractMailboxFinder {

	@Override
	public void setLogin(String login, String password) throws IMAPyException {
		this.password = password;
		try {
			final String emailDomain = login.split("@")[1];
			URL url = new URL("http://autoconfig."  + emailDomain + "/mail/config-v1.1.xml?emailaddress=" + login);		
			InputStream autoconfigStream = null;
			try  {
				try {
					autoconfigStream = url.openConnection().getInputStream();
				} catch (UnknownHostException | FileNotFoundException e) {
					url = new URL("http://" + emailDomain + "/.well-known/autoconfig/mail/config-v1.1.xml?emailaddress=" + login);
					autoconfigStream = url.openConnection().getInputStream();
				}
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
			} catch (IOException | ParserConfigurationException | SAXException e) {
				throw new IMAPyException(e);
			} finally {
				if (autoconfigStream != null) { 
					try {
						autoconfigStream.close();
					} catch (IOException e) {
						// do not care
					} 
				}
			}
		} catch (DOMException | MalformedURLException e) {
			throw new IMAPyException(e);
		}
	}

}
