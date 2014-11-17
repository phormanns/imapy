<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>IMAPy Login</title>
<link href="<%= request.getContextPath() %>/style.css" media="all" rel="stylesheet" type="text/css" />
</head>
<%
	final String browserType = request.getHeader("User-Agent");
	String mobile = "true";
	if (browserType == null || !browserType.toLowerCase().contains("mobile")) {
		mobile = "false";
	}
%>
<body>
	<div class="login">
		<p>Anmeldung</p>
		<form action="login" method="post">
			<div>E-Mail Adresse</div>
			<div><input type="text" size="48" maxlength="72" name="email"></div>
			<div>Passwort</div>
			<div><input type="password" size="48" maxlength="48" name="password"></div>
			<div>Handy-Version</div>
<%
	if ("true".equals(mobile)) {
%>			
			<div><select size="1" name="mobile"><option value="true" selected>Ja</option><option value="false">Nein</option></select></div>
<%
	} else {
%>			
			<div><select size="1" name="mobile"><option value="true">Ja</option><option value="false" selected>Nein</option></select></div>
<%
	}
%>			
			<p><input type="submit" value=" Absenden "/></p>
		</form>
	</div>
</body>
</html>