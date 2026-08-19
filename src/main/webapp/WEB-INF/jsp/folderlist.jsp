<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="de.jalin.imap.*"%>
<%@ page import="java.util.*"%>
<%
    String ctx = request.getContextPath();
    String activeFolder = (String) request.getAttribute("activeFolder");
    if (activeFolder == null) {
        activeFolder = request.getParameter("folder");
    }
    if (activeFolder == null) {
        activeFolder = "INBOX";
    }
%>
<div class="nav-section" id="folderlist-root">
    <h3 class="nav-section-title">Ordner</h3>
    <ul class="nav-list">
        <%
            final Object foldersListObj = session.getAttribute("folders");
            if (foldersListObj instanceof List<?>) {
                final List<?> foldersList = (List<?>) foldersListObj;
                for (final Object folderMapObj : foldersList) {
                    if (folderMapObj instanceof IMAPyFolder) {
                        final IMAPyFolder yFolder = (IMAPyFolder) folderMapObj;
                        String folderName = yFolder.getName();
                        String folderTitle = yFolder.getTitle();
                        int unread = yFolder.getUnreadMessageCount();
                        int total = yFolder.getTotalMessageCount();
                        String itemClass = "nav-item";
                        if (unread > 0) {
                            itemClass += " is-unread";
                        }
                        if (folderName.equals(activeFolder)) {
                            itemClass += " is-active";
                        }
        %>
        <li class="<%= itemClass%>"
            data-folder="<%= folderName%>"
            hx-get="<%= ctx%>/folder/<%= folderName%>"
            hx-target="#list"
            hx-trigger="click"
            hx-swap="innerHTML"
            onclick="selectFolder(this, '<%= folderName%>')">
            <span class="nav-item-left">
                <svg class="nav-item-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
                <span class="nav-item-label"><%= folderTitle%></span>
            </span>
            <span class="nav-count"><%= unread%>/<%= total%></span>
        </li>
        <%
                    }
                }
            }
        %>
    </ul>
</div>
