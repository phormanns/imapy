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
			if (folderMapObj instanceof Map<?, ?>) {
				final Map<?, ?> folderMap = (Map<?, ?>) folderMapObj;
 %>
				<li class="<%= folderMap.get("cssclass") %>"><a href="folder/<%= folderMap.get("folder") %>"><%= folderMap.get("title") %> (<%= folderMap.get("nunread") %>/<%= folderMap.get("ntotal") %>)</a></li>			
<%		
			}
		}
	}
 %>
	</ul>
</body>
</html>