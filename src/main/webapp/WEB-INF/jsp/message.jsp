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
	final Object attrObj = session.getAttribute("message");
	if (attrObj instanceof IMAPyMessage) {
		final IMAPyMessage message = (IMAPyMessage) session.getAttribute("message");
		if ("requestdel".equals(request.getParameter("msgop"))) {
 %>
		<li class="menuitem"><a href="<%= request.getContextPath() %>/message/<%= message.getFolder() %>/<%= message.getIndex() %>?msgop=confirmdel">Löschen Ok</a></li>			
<%
		} else {
 %>
		<li class="menuitem"><a href="<%= request.getContextPath() %>/message/<%= message.getFolder() %>/<%= message.getIndex() %>?msgop=requestdel">Löschen</a></li>			
<%
		}
 %>
	</ul>
	<div class="emailheader">
		<h1><%= message.getSubject() %></h1>
		<div class="emailfrom"><div class="label">Absender</div><div class="text"><%= message.getFrom() %></div></div>
		<div class="emailto"><div class="label">Empfänger</div><div class="text"><%= message.getTo() %></div></div>
		<div class="emaildate"><div class="label">Datum</div><div class="text"><%= message.getDate() %></div></div>
<%
		final List<String> attachments = message.getAttachments(); 
		final int numAttached = attachments.size();
		if (numAttached > 0) {
 %>
		<div class="emailattached"><div class="label">Anhänge</div><div class="text">
<%
			for (int idx=0; idx<numAttached; idx++) {
 %>
 				<a href="<%= request.getContextPath() %>/attachment/<%= message.getFolder() %>/<%= message.getIndex() %>/<%= attachments.get(idx) %>" target="_new"><%= attachments.get(idx) %></a> &nbsp;
<%
			}
 %>
		</div></div>
<%
		}
 %>
	</div>
	<div class="emailcontent">
		<%= message.getContent() %>
	</div>
<%
		if (!"true".equals(session.getAttribute("mobile")) && "new".equals(message.getStatus())) {
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