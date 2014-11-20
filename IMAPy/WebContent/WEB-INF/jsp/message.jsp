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
		<li class="menuitem"><a href="<%= request.getContextPath() %>/folder/<%= session.getAttribute("folder") %>">Folder</a></li>			
<%
	}
 %>
<%
	final Object attrObj = session.getAttribute("message");
	if (attrObj instanceof Map<?, ?>) {
		final Map<?, ?> message = (Map<?, ?>)session.getAttribute("message");
		if ("requestdel".equals(request.getParameter("msgop"))) {
 %>
		<li class="menuitem"><a href="<%= request.getContextPath() %>/message/<%= message.get("folder") %>/<%= message.get("idx") %>?msgop=confirmdel">Löschen Bestätigen</a></li>			
<%
		} else {
 %>
		<li class="menuitem"><a href="<%= request.getContextPath() %>/message/<%= message.get("folder") %>/<%= message.get("idx") %>?msgop=requestdel">Löschen</a></li>			
<%
		}
 %>
	</ul>
	<div class="emailheader">
		<h1><%= message.get("subject") %></h1>
		<div class="emailfrom"><div class="label">Absender</div><div class="text"><%= message.get("from") %></div></div>
		<div class="emailto"><div class="label">Empfänger</div><div class="text"><%= message.get("to") %></div></div>
		<div class="emaildate"><div class="label">Datum</div><div class="text"><%= message.get("date") %></div></div>
	</div>
	<div class="emailcontent">
		<%= message.get("content") %>
	</div>
<%
		if (!"true".equals(session.getAttribute("mobile")) && "new".equals(message.get("status"))) {
 %>
 	<script type="text/javascript">
		parent.foldersframe.location.reload();
		parent.mailboxframe.location.reload();
	</script>
<%
		}
	}
 %>
</body>
</html>