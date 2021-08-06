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
	<ul class="menu">
<%
	if ("true".equals(session.getAttribute("mobile"))) {
%>
		<li class="menuitem"><a href="<%= request.getContextPath() %>/mailbox">Mailbox</a></li>
<%
	}
%>
	</ul>
	<ul class="messages">
<%
	final Object messagesListObj = session.getAttribute("messages");
	if (messagesListObj instanceof List<?>) {
		String maxListLengthAttr = (String)session.getAttribute("max_list_length");
		int maxListLength = Integer.parseInt(maxListLengthAttr); 
		String targetAttrib = "messagesframe";
		if ("true".equals(session.getAttribute("mobile"))) { 
			targetAttrib = "_self";
		}
		final List<?> messagesList = (List<?>) messagesListObj;
		int loopCount = 0;
		for (final Object messageMapObj : messagesList) {
			loopCount++;
			if (loopCount > maxListLength) {
				break;
			}
			if (messageMapObj instanceof IMAPyMessage) {
				final IMAPyMessage yMessage = (IMAPyMessage) messageMapObj;
 %>
		<li class="message<%= yMessage.getStatus() %>">
			<a target="<%= targetAttrib %>"
				href="<%= request.getContextPath() %>/message/<%= yMessage.getFolder() %>/<%= yMessage.getIndex() %>">
				<%= yMessage.getTitle() %> [von: <%= yMessage.getAuthor() %>]
			</a>
		</li>			
<%
			}

		}
	}
 %>
	</ul>
</body>
</html>