<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>IMAPy Login</title>
<link href="<%= request.getContextPath() %>/style.css" media="all" rel="stylesheet" type="text/css" />
</head>
<body>
	<div class="login">
		<p>Anmeldung</p>
		<form action="login" method="post">
			<div>E-Mail Adresse</div><div><input type="text" size="48" maxlength="72" name="email"></div>
			<div>Passwort</div><div><input type="password" size="48" maxlength="48" name="password"></div>
			<p><input type="submit" value=" Absenden "/></p>
		</form>
	</div>
</body>
</html>