<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html lang="de">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>IMAPy Login</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/webjars/purecss/2.0.3/build/pure-min.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/style.css">
</head>
<%
	String email = null;
	email = request.getParameter("email");
	if (email == null) {
		email = "";
	}
%>
<body>
	<div class="login">
		<p>Anmeldung</p>
		<form action="login" method="post">
			<div>User / Postfach</div>
			<div><input type="text" size="48" maxlength="72" name="email" value="<%=email%>"></div>
			<div>Passwort</div>
			<div><input type="password" size="48" maxlength="48" name="password"></div>
			<p><input type="submit" value=" Absenden "/></p>
		</form>
	</div>
	<script type="text/javascript">
		if (top != self) { top.location.replace(self.location.href); }
	</script>
</body>
</html>