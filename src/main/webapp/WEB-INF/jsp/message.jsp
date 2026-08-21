<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ page import="de.jalin.imap.*"%>
<%@ page import="java.util.*"%>

<%
    String ctx = request.getContextPath();
    String csrfToken = (String) session.getAttribute("csrf_token");
    pageContext.setAttribute("ctx", ctx);
    pageContext.setAttribute("csrfToken", csrfToken);
    pageContext.setAttribute("deleteParams", "{ \"msgop\": \"confirmdel\", \"csrf_token\": \"" + csrfToken + "\" }");
%>

<%
    final Object attrObj = session.getAttribute("message");
    if (attrObj instanceof IMAPyMessage) {
        final IMAPyMessage message = (IMAPyMessage) attrObj;
        String from = message.getFrom();
        String subject = message.getSubject();
        String date = message.getDate();
        String folder = message.getFolder();
        int index = message.getIndex();
        String content = message.getContent();
        List<String> attachments = message.getAttachments();

        String initial = "?";
        if (from != null && !from.isEmpty()) {
            from = from.trim();
            int lt = from.indexOf('<');
            String display = lt > 0 ? from.substring(0, lt).trim() : from;
            if (display.isEmpty()) display = from;
            initial = display.substring(0, 1).toUpperCase();
        }
        pageContext.setAttribute("from", from);
        pageContext.setAttribute("subject", subject);
        pageContext.setAttribute("date", date);
        pageContext.setAttribute("folder", folder);
        pageContext.setAttribute("index", index);
        pageContext.setAttribute("content", content);
        pageContext.setAttribute("initial", initial);
        pageContext.setAttribute("attachments", attachments);
%>
        <div class="email-content"
            hx-get="<c:out value="${ctx}"/>/folder/<c:out value="${folder}"/>"
            hx-trigger="load"
            hx-target="#list"
            hx-swap="innerHTML">
            <div class="email-content-toolbar">
                <div class="email-content-toolbar-left">
                                     <button type="button" class="icon-button back-to-list" aria-label="Zurück zur Liste">
                                         <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
                                     </button>
                </div>
                <div class="email-content-toolbar-right">
                    <button type="button" class="btn btn-danger"
                       hx-post="<c:out value="${ctx}"/>/message/<c:out value="${folder}"/>/<c:out value="${index}"/>"
                       hx-vals="<c:out value="${deleteParams}"/>"
                       hx-confirm="Diese Nachricht wirklich löschen?"
                       hx-target="#main"
                       hx-swap="innerHTML">Löschen</button>
                </div>
            </div>

            <header class="email-content-header">
                <h1 class="email-content-title"><c:out value="${subject}"/></h1>
                <div class="email-content-meta">
                    <span class="email-avatar"><c:out value="${initial}"/></span>
                    <strong><c:out value="${from}"/></strong>
                    <span class="email-content-meta-divider"></span>
                    <span><c:out value="${date}"/></span>
<%
                    if (attachments != null && !attachments.isEmpty()) {
                        int attCount = attachments.size();
                        pageContext.setAttribute("attCount", attCount);
%>
                    <span class="email-content-meta-divider"></span>
                    <a class="email-content-attachments-hint" href="#attachments-<c:out value="${folder}"/>-<c:out value="${index}"/>" title="Zu den Anlagen springen">
                        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg>
                        <span><c:out value="${attCount}"/> Anlage<c:out value="${attCount == 1 ? '' : 'n'}"/></span>
                    </a>
<%
                    }
%>
                </div>
            </header>

            <div class="email-content-body">
                <c:out value="${content}" escapeXml="false"/>
            </div>

<%
            if (attachments != null && !attachments.isEmpty()) {
                pageContext.setAttribute("attCount", attachments.size());
%>
            <section class="email-attachments" id="attachments-<c:out value="${folder}"/>-<c:out value="${index}"/>" aria-label="Anlagen">
                <h3 class="email-attachments-title">
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg>
                    <span><c:out value="${attCount}"/> Anlage<c:out value="${attCount == 1 ? '' : 'n'}"/></span>
                </h3>
                <ul class="email-attachments-list">
<%
                for (String name : attachments) {
                    if (name == null || name.isEmpty()) continue;
                    String safeName = name;
                    String mimeGuess = getServletContext().getMimeType(safeName);
                    String iconKey = "file";
                    if (mimeGuess != null) {
                        if (mimeGuess.startsWith("image/")) iconKey = "image";
                        else if (mimeGuess.startsWith("audio/")) iconKey = "audio";
                        else if (mimeGuess.startsWith("video/")) iconKey = "video";
                        else if (mimeGuess.equals("application/pdf")) iconKey = "pdf";
                        else if (mimeGuess.startsWith("text/")) iconKey = "text";
                        else if (mimeGuess.contains("zip") || mimeGuess.contains("compressed")) iconKey = "archive";
                    }
                    pageContext.setAttribute("safeName", safeName);
                    pageContext.setAttribute("mimeGuess", mimeGuess);
                    pageContext.setAttribute("iconKey", iconKey);
%>
                    <li>
                        <a class="email-attachment"
                            href="<c:out value="${ctx}"/>/attachment/<c:out value="${folder}"/>/<c:out value="${index}"/>/<c:out value="${safeName}"/>"
                            target="_blank"
                            rel="noopener"
                            download="<c:out value="${safeName}"/>"
                            title="<c:out value="${empty mimeGuess ? 'Unbekannter Typ' : mimeGuess}"/>">
                            <span class="email-attachment-icon email-attachment-icon--<c:out value="${iconKey}"/>">
<%
                                if ("image".equals(iconKey)) {
%>
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
<%
                                } else if ("pdf".equals(iconKey)) {
%>
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
<%
                                } else if ("archive".equals(iconKey)) {
%>
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="21 8 21 21 3 21 3 8"/><rect x="1" y="3" width="22" height="5"/><line x1="10" y1="12" x2="14" y2="12"/></svg>
<%
                                } else if ("audio".equals(iconKey) || "video".equals(iconKey)) {
%>
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/></svg>
<%
                                } else if ("text".equals(iconKey)) {
%>
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="17" y1="10" x2="3" y2="10"/><line x1="21" y1="6" x2="3" y2="6"/><line x1="21" y1="14" x2="3" y2="14"/><line x1="17" y1="18" x2="3" y2="18"/></svg>
<%
                                } else {
%>
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
<%
                                }
%>
                            </span>
                            <span class="email-attachment-name"><c:out value="${safeName}"/></span>
                        </a>
                    </li>
<%
                }
%>
                </ul>
            </section>
<%
            }
%>
        </div>
<%
    } else {
%>
        <div class="email-content-empty">
            <p>Nachricht konnte nicht geladen werden.</p>
        </div>
<%
    }
%>
