<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    String email = request.getParameter("email");
    if (email == null) {
        email = "";
    }
    String error = request.getParameter("error");
    String ctx = request.getContextPath();
    pageContext.setAttribute("ctx", ctx);
%>
<!doctype html>
<html lang="de">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>IMAPy – Anmeldung</title>
        <link rel="icon" type="image/x-icon" href="<c:out value="${ctx}"/>/favicon.ico">
        <link rel="stylesheet" href="<c:out value="${ctx}"/>/style.css">
    </head>
    <body>
        <div class="login-shell">
            <div class="login-card" role="main">
                <div class="login-brand">
                    <div class="login-brand-mark">iM</div>
                    <div class="login-brand-name">IMAPy Webmail</div>
                </div>

                <h1 class="login-title">Willkommen zurück</h1>
                <p class="login-subtitle">Melde dich mit deinem Postfach an, um E-Mails zu lesen.</p>

                <% if (error != null && !error.isEmpty()) { %>
                <div class="login-error">
                    <%
                        if ("invalid".equals(error)) {
                            out.print("Anmeldung fehlgeschlagen. Bitte überprüfe Benutzer und Passwort.");
                        } else {
                            out.print("Es ist ein Fehler aufgetreten. Bitte versuche es erneut.");
                        }
                    %>
                </div>
                <% }%>

                <form action="login" method="post" autocomplete="on">
                    <div class="field">
                        <label class="field-label" for="email">Postfach / Benutzer</label>
                        <input id="email" class="field-input" type="text" name="email"
                               value="<c:out value="${param.email}"/>" size="48" maxlength="72"
                               autocomplete="username" required autofocus>
                    </div>
                    <div class="field">
                        <label class="field-label" for="password">Passwort</label>
                        <input id="password" class="field-input" type="password" name="password"
                               size="48" maxlength="48" autocomplete="current-password" required>
                    </div>
                    <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}"> 
                    <button type="submit" class="btn btn-primary btn-block">Anmelden</button>
                </form>
            </div>
        </div>

        <script type="text/javascript">
            if (top != self) {
                top.location.replace(self.location.href);
            }
        </script>
    </body>
</html>
