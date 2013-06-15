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
	<ul>
<%
	List<Map<String, String>> foldersList = (List<Map<String, String>>)session.getAttribute("folders");
	for (Map<String, String> folderMap : foldersList) {
 %>
		<li><a href="folder/<%= folderMap.get("folder") %>"><%= folderMap.get("title") %> (<%= folderMap.get("nunread") %>/<%= folderMap.get("ntotal") %>)</a></li>			
<%		
	}
%>
	</ul>
</body>
</html>