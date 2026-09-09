package de.jalin.webmail.impl;

import de.jalin.imap.IMAPyException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.IDN;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.UnknownHostException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AutoconfigMailboxFinder extends AbstractMailboxFinder {

    @Override
    public void setLogin(String login) throws IMAPyException {
        try {
            final String[] loginParts = login.split("@", 2);
            if (loginParts.length != 2) {
                throw new IMAPyException("Invalid login");
            }
            final String emailDomain = normalizeAndValidateDomain(loginParts[1]);
            InputStream autoconfigStream = null;
            try {
                autoconfigStream = openAutoconfigStream(emailDomain, login);
                parseAutoconfig(autoconfigStream, login);
            } catch (IOException | URISyntaxException | ParserConfigurationException | SAXException e) {
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
        } catch (DOMException e) {
            throw new IMAPyException(e);
        }
    }

    InputStream openAutoconfigStream(final String emailDomain, final String login) throws IOException, URISyntaxException {
        final URI uriAutoconfigSubdomain = new URI("https://autoconfig." + emailDomain + "/mail/config-v1.1.xml?emailaddress=" + login);
        try {
            return openStream(uriAutoconfigSubdomain);
        } catch (UnknownHostException | FileNotFoundException e) {
            final URI uriAutoconfigWellknown = new URI("https://" + emailDomain + "/.well-known/autoconfig/mail/config-v1.1.xml?emailaddress=" + login);
            return openStream(uriAutoconfigWellknown);
        }
    }

    InputStream openStream(final URI uri) throws IOException {
        final URLConnection urlConnection = uri.toURL().openConnection();
        return urlConnection.getInputStream();
    }

    void parseAutoconfig(final InputStream configStream, final String emailAddress) throws ParserConfigurationException, SAXException, IOException {
        final Document document = newSecureDocumentBuilder().parse(configStream);
        parseIncomingServer(document, emailAddress);
    }

    private void parseIncomingServer(final Document document, final String emailAddress) throws DOMException {
        final NodeList inServersNodes = document.getElementsByTagName("incomingServer");
        final int listLength = inServersNodes.getLength();
        for (int idx = 0; idx < listLength; idx++) {
            final Node node = inServersNodes.item(idx);
            evalServerElement(node, emailAddress, "imap");
        }
    }

    private void parseOutgoingServer(final Document document, final String emailAddress) throws DOMException {
        final NodeList inServersNodes = document.getElementsByTagName("outgoingServer");
        final int listLength = inServersNodes.getLength();
        for (int idx = 0; idx < listLength; idx++) {
            final Node node = inServersNodes.item(idx);
            evalServerElement(node, emailAddress, "smtp");
        }
    }

    private void evalServerElement(final Node node, final String emailAddress, final String protocol) throws DOMException {
        final Node item = node.getAttributes().getNamedItem("type");
        if (item != null && protocol.equalsIgnoreCase(item.getNodeValue())) {
            // Placeholders:
            // %EMAILADDRESS% (full email address of the user, usually entered by the user)
            // %EMAILLOCALPART% (email address, part before @)
            // %EMAILDOMAIN% (email address, part after @)
            final NodeList childNodes = node.getChildNodes();
            final int childsListLength = childNodes.getLength();
            for (int childsIdx = 0; childsIdx < childsListLength; childsIdx++) {
                final Node child = childNodes.item(childsIdx);
                final String nodeName = child.getNodeName();
                if ("hostname".equals(nodeName)) {
                    final String textContent = child.getTextContent();
                    String hostName = textContent;
                    if (textContent.contains("%EMAILDOMAIN%")) {
                        hostName = textContent.replace("%EMAILDOMAIN%", emailAddress.split("@")[1]);
                    }
                    this.setHost(protocol, hostName);
                }
                if ("username".equals(nodeName)) {
                    final String textContent = child.getTextContent();
                    String loginUser = textContent;
                    if ("%EMAILADDRESS%".equalsIgnoreCase(textContent)) {
                        loginUser = emailAddress;
                    }
                    if ("%EMAILLOCALPART%".equalsIgnoreCase(textContent)) {
                        loginUser = emailAddress.split("@")[0];
                    }
                    this.setUser(protocol, loginUser);
                }
            }
        }
    }
    
    private void setHost(String protocol, String host) {
        if ("imap".equalsIgnoreCase(protocol)) {
            setImapHost(host);
        }
        if ("smtp".equalsIgnoreCase(protocol)) {
            setSmtpHost(host);
        }
    }

    private void setUser(String protocol, String login) {
        if ("imap".equalsIgnoreCase(protocol)) {
            setImapUser(login);
        }
        if ("smtp".equalsIgnoreCase(protocol)) {
            setSmtpUser(login);
        }
    }

    private static DocumentBuilder newSecureDocumentBuilder() throws ParserConfigurationException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return documentBuilderFactory.newDocumentBuilder();
    }

    static boolean isUnsafeAddress(InetAddress address) {
        return address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress();
    }

    static String normalizeAndValidateDomain(String domain) throws IMAPyException {
        if (domain == null || domain.isBlank()) {
            throw new IMAPyException("Invalid email domain");
        }
        final String asciiDomain;
        try {
            asciiDomain = IDN.toASCII(domain.trim(), IDN.USE_STD3_ASCII_RULES).toLowerCase();
        } catch (IllegalArgumentException e) {
            throw new IMAPyException(e);
        }
        if (asciiDomain.length() > 253 || !asciiDomain.matches("^[a-z0-9](?:[a-z0-9-\\.]*[a-z0-9])?$") || !asciiDomain.contains(".")) {
            throw new IMAPyException("Invalid email domain");
        }
        if ("localhost".equals(asciiDomain) || asciiDomain.endsWith(".localhost") || asciiDomain.endsWith(".local")) {
            throw new IMAPyException("Unsafe email domain");
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(asciiDomain)) {
                if (isUnsafeAddress(address)) {
                    throw new IMAPyException("Unsafe email domain");
                }
            }
        } catch (UnknownHostException e) {
            // Keep previous behavior: unknown domains are handled later by connection attempts.
        }
        return asciiDomain;
    }

}
