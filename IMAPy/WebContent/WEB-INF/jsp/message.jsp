<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="de.jalin.imap.*"%>
<%@ page import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>IMAPy Mailbox <%= session.getAttribute("email") %></title>
</head>
<body>
	<dl>
<%
	Map<String, String> message = (Map<String, String>)session.getAttribute("message");
%>
		<dt>from</dt>
		<dd><%= message.get("from") %></dd>

		<dt>to</dt>
		<dd><%= message.get("to") %></dd>

		<dt>subject</dt>
		<dd><%= message.get("subject") %></dd>
	</dl>
</body>
</html>