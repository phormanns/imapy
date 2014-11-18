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
		final List<?> messagesList = (List<?>) messagesListObj;
		for (final Object messageMapObj : messagesList) {
			if (messageMapObj instanceof Map<?, ?>) {
				final Map<?, ?> messageMap = (Map<?, ?>) messageMapObj;
				if ("true".equals(session.getAttribute("mobile"))) {
 %>
		<li class="message<%= messageMap.get("status") %>">
			<a target="_self"
				href="<%= request.getContextPath() %>/message/<%= messageMap.get("folder") %>/<%= messageMap.get("idx") %>">
				<%= messageMap.get("title") %> [von: <%= messageMap.get("author") %>]
			</a>
		</li>			
<%
				} else {
 %>
		<li class="message<%= messageMap.get("status") %>">
			<a target="messagesframe"
				href="<%= request.getContextPath() %>/message/<%= messageMap.get("folder") %>/<%= messageMap.get("idx") %>">
				<%= messageMap.get("title") %> [von: <%= messageMap.get("author") %>]
			</a>
		</li>			
<%
				}
			}
		}
	}
 %>
	</ul>
</body>
</html>