<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String ctx = request.getContextPath();
%>
<!doctype html>
<html lang="de">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>IMAPy – Fehler</title>
        <link rel="icon" type="image/x-icon" href="<%= ctx%>/favicon.ico">
        <link rel="stylesheet" href="<%= ctx%>/style.css">
    </head>
    <body>
        <div class="login-shell">
            <div class="login-card" role="main">
                <div class="login-brand">
                    <div class="login-brand-mark">iM</div>
                    <div class="login-brand-name">IMAPy Webmail</div>
                </div>

                <h1 class="login-title">Keine Berechtigung</h1>
                <p class="login-subtitle">Dir fehlen die Rechte, die letzte Aktion auszuführen.</p>

                <div class="login-error">
                    out.print("Es ist ein Fehler aufgetreten. Bitte versuche es erneut.");
                </div>

            </div>
        </div>

        <script type="text/javascript">
            if (top != self) {
                top.location.replace(self.location.href);
            }
        </script>
    </body>
</html>
