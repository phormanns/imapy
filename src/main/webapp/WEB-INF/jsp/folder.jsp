<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ page import="de.jalin.imap.*"%>
<%@ page import="java.util.*"%>

<%
    String ctx = request.getContextPath();
    String folderTitle = (String) request.getAttribute("folderName");
    if (folderTitle == null) {
        folderTitle = request.getParameter("folder");
    }
    if (folderTitle == null) {
        folderTitle = "INBOX";
    }

    if (folderTitle.startsWith("INBOX.")) {
        folderTitle = folderTitle.substring(6);
    }
    folderTitle = folderTitle.replace(".", "/");

    int rendered = 0;
    int maxListLength = Integer.MAX_VALUE;
    String maxListLengthAttr = (String) session.getAttribute("max_list_length");
    if (maxListLengthAttr != null) {
        try {
            maxListLength = Integer.parseInt(maxListLengthAttr);
        } catch (Exception ignored) {
        }
    }

    final Object messagesListObj = session.getAttribute("messages");

    String activeMessageFolder = null;
    int activeMessageIndex = -1;
    final Object activeMsgObj = session.getAttribute("message");
    if (activeMsgObj instanceof IMAPyMessage) {
        final IMAPyMessage activeMsg = (IMAPyMessage) activeMsgObj;
        activeMessageFolder = activeMsg.getFolder();
        activeMessageIndex = activeMsg.getIndex();
    }

    pageContext.setAttribute("ctx", ctx);
    pageContext.setAttribute("folderTitle", folderTitle);
%>

<header class="app-list-header">
    <h2 class="app-list-title"><c:out value="${folderTitle}"/></h2>
    <div class="app-list-tools">
        <button type="button" class="icon-button" aria-label="Aktualisieren"
                onclick="refreshMailbox()">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
        </button>
    </div>
</header>

<%
    if (messagesListObj instanceof List<?>) {
        final List<?> messagesList = (List<?>) messagesListObj;
        for (final Object messageMapObj : messagesList) {
            rendered++;
            if (rendered > maxListLength) {
                break;
            }
            if (messageMapObj instanceof IMAPyMessage) {
                final IMAPyMessage yMessage = (IMAPyMessage) messageMapObj;
                String author = yMessage.getAuthor();
                String title = yMessage.getTitle();
                String date = yMessage.getDate();
                String status = yMessage.getStatus();
                String folder = yMessage.getFolder();
                int index = yMessage.getIndex();

                String cls = "email-item";
                if ("unread".equalsIgnoreCase(status)) {
                    cls += " is-unread";
                }
                if (folder.equals(activeMessageFolder) && index == activeMessageIndex) {
                    cls += " is-active";
                }

                String initial = "?";
                if (author != null && !author.isEmpty()) {
                    author = author.trim();
                    int lt = author.indexOf('<');
                    String display = lt > 0 ? author.substring(0, lt).trim() : author;
                    if (display.isEmpty()) {
                        display = author;
                    }
                    initial = display.substring(0, 1).toUpperCase();
                }
                pageContext.setAttribute("cls", cls);
                pageContext.setAttribute("index", index);
                pageContext.setAttribute("folder", folder);
                pageContext.setAttribute("initial", initial);
                pageContext.setAttribute("author", author);
                pageContext.setAttribute("date", date);
                pageContext.setAttribute("title", title);
%>
    <div class="<c:out value="${cls}"/>"
         data-message-index="<c:out value="${index}"/>"
         hx-get="<c:out value="${ctx}"/>/message/<c:out value="${folder}"/>/<c:out value="${index}"/>"
         hx-target="#main"
         hx-trigger="click"
         hx-swap="innerHTML"
         onclick="selectMessage(this, '<c:out value="${folder}"/>', <c:out value="${index}"/>)">
        <div class="email-avatar"><c:out value="${initial}"/></div>
        <div class="email-body">
            <div class="email-row">
                <p class="email-name"><c:out value="${author}"/></p>
                <span class="email-time"><c:out value="${date}"/></span>
            </div>
            <p class="email-subject"><c:out value="${title}"/></p>
        </div>
    </div>
<%
            }
        }
        if (rendered == 0) {
%>
    <div class="empty-state">
        <p>Keine Nachrichten in diesem Ordner.</p>
    </div>
<%
        }
    } else {
%>
    <div class="empty-state">
        <p>Keine Nachrichten verfügbar.</p>
    </div>
<%
    }
%>
