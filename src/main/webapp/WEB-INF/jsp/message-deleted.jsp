<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String ctx = request.getContextPath();
    String deletedFolder = (String) session.getAttribute("deletedFolder");
    if (deletedFolder == null) {
        deletedFolder = "INBOX";
    }
    String deletedSubject = (String) session.getAttribute("deletedMessageSubject");
    session.removeAttribute("deletedFolder");
    session.removeAttribute("deletedMessageSubject");
    session.removeAttribute("message");
%>
<div class="email-content-empty">
    <svg width="42" height="42" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2"/></svg>
    <p class="empty-state-title">Nachricht gelöscht</p>
<%
    if (deletedSubject != null && !deletedSubject.isEmpty()) {
%>
    <p>„<%= deletedSubject%>" wurde in den Papierkorb verschoben.</p>
<%
    } else {
%>
    <p>Die Nachricht wurde aus „<%= deletedFolder%>" entfernt.</p>
<%
    }
%>
    <p style="margin-top: 1rem;">
        <button type="button" class="btn btn-ghost back-to-list">Zurück zur Liste</button>
    </p>
</div>
