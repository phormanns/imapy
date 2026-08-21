package de.jalin.webmail.impl;

import de.jalin.imap.IMAPyException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.IDN;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

    private static boolean isUnsafeAddress(InetAddress address) {
        return address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress();
    }

    private static String normalizeAndValidateDomain(String domain) throws IMAPyException {
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
                final URI uriAutoconfigSubdomain = new URI("https://autoconfig." + emailDomain + "/mail/config-v1.1.xml?emailaddress=" + login);
                URL url = uriAutoconfigSubdomain.toURL();
                try {
                    final URLConnection urlConnection = url.openConnection();
                    autoconfigStream = urlConnection.getInputStream();
                } catch (UnknownHostException | FileNotFoundException e) {
                    final URI uriAutoconfigWellknown = new URI("https://" + emailDomain + "/.well-known/autoconfig/mail/config-v1.1.xml?emailaddress=" + login);
                    url = uriAutoconfigWellknown.toURL();
                    final URLConnection urlConnection = url.openConnection();
                    autoconfigStream = urlConnection.getInputStream();
                }
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
                                this.setHost(child.getTextContent());
                            }
                            if ("username".equals(child.getNodeName())) {
                                this.setUser(child.getTextContent());
                            }
                        }
                    }
                }
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

}
