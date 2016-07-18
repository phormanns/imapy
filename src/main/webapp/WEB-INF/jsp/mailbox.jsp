<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="de.jalin.imap.*"%>
<%@ page import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>IMAPy Mailbox <%= session.getAttribute("email") %></title>
<link href="<%= request.getContextPath() %>/style.css" media="all" rel="stylesheet" type="text/css" />
</head>
<body>
	<ul class="folders">
<%
	final Object foldersListObj = session.getAttribute("folders");
	if (foldersListObj instanceof List<?>) {
		final List<?> foldersList = (List<?>)foldersListObj;
		for (final Object folderMapObj : foldersList) {
			if (folderMapObj instanceof IMAPyFolder) {
				final IMAPyFolder yFolder = (IMAPyFolder) folderMapObj;
				if ("true".equals(session.getAttribute("mobile"))) {

 %>
		<li class="<%= ( yFolder.getUnreadMessageCount() > 0 ) ? "foldernewmsgs" : "folderread" %>">
			<a href="folder/<%= yFolder.getName() %>" target="_self">
				<%= yFolder.getTitle() %> (<%= yFolder.getUnreadMessageCount() %>/<%= yFolder.getTotalMessageCount() %>)
			</a>
		</li>			
<%
				} else {
					 %>
		<li id="<%= yFolder.getName() %>" class="<%= ( yFolder.getUnreadMessageCount() > 0 ) ? "foldernewmsgs" : "folderread" %>" 
			ondragover="event.preventDefault();" 
			ondrop="event.preventDefault(); var req = new XMLHttpRequest(); req.open('GET', event.dataTransfer.getData('text/plain') + '/moveto/' + event.currentTarget.id, false); req.send(null); reloadOnDrag();">
			<a href="folder/<%= yFolder.getName() %>" target="foldersframe">
				<%= yFolder.getTitle() %> (<%= yFolder.getUnreadMessageCount() %>/<%= yFolder.getTotalMessageCount() %>)
			</a>
		</li>			
				<%
				}
			}
		}
	}
 %>
	</ul>
	<script type="text/javascript">
		var reloadOnDrag = function() { 
			parent.foldersframe.location.reload();
			parent.mailboxframe.location.reload();
		}
	</script>
</body>
</html>