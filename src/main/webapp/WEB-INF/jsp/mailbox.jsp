<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="de.jalin.imap.*"%>
<%@ page import="java.util.*"%>
<!doctype html>
<html lang="de">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>IMAPy Mailbox <%= session.getAttribute("email") %></title>
	<link rel="stylesheet" href="<%= request.getContextPath() %>/webjars/purecss/2.0.3/build/pure-min.css">
	<link rel="stylesheet" href="<%= request.getContextPath() %>/style.css">
	<script src="<%= request.getContextPath() %>/webjars/htmx.org/1.5.0/dist/htmx.min.js"></script>
</head>
<body>
	<div id="layout" class="content pure-g">
    	<div id="nav" class="pure-u">
        	<a href="#" id="menuLink" class="nav-menu-button">Menu</a>

        	<div class="nav-inner">
            	<button class="primary-button pure-button">Compose</button>

            	<div class="pure-menu">
                	<ul class="pure-menu-list">
                		
<%
	final Object foldersListObj = session.getAttribute("folders");
	if (foldersListObj instanceof List<?>) {
		final List<?> foldersList = (List<?>)foldersListObj;
		for (final Object folderMapObj : foldersList) {
			if (folderMapObj instanceof IMAPyFolder) {
				final IMAPyFolder yFolder = (IMAPyFolder) folderMapObj;

 %>
						<li class="pure-menu-item" hx-get="<%= request.getContextPath() %>/folder/<%= yFolder.getName() %>" hx-target="#list" hx-trigger="click" hx-swap="innerHTML">
							<a href="#" class="pure-menu-link">
								<%= yFolder.getTitle() %> <span class="email-count"><%= yFolder.getUnreadMessageCount() %>/<%= yFolder.getTotalMessageCount() %></span>
							</a>
						</li>
		<!-- 			
		<li id="<%= yFolder.getName() %>" class="<%= ( yFolder.getUnreadMessageCount() > 0 ) ? "foldernewmsgs" : "folderread" %>" 
			ondragover="event.preventDefault();" 
			ondrop="event.preventDefault(); var req = new XMLHttpRequest(); req.open('GET', event.dataTransfer.getData('text/plain') + '/moveto/' + event.currentTarget.id, false); req.send(null); reloadOnDrag();">
			<a href="folder/<%= yFolder.getName() %>" target="foldersframe">
				<%= yFolder.getTitle() %> (<%= yFolder.getUnreadMessageCount() %>/<%= yFolder.getTotalMessageCount() %>)
			</a>
		</li>  -->			
<%
			}
		}
	}
 %>
         			</ul>
            	</div>
        	</div>
    	</div>

		<div id="list" class="pure-u-1" hx-get="<%= request.getContextPath() %>/folder/INBOX" hx-trigger="load">
		</div>

	</div>
	<script type="text/javascript">
		var reloadOnDrag = function() { 
			parent.foldersframe.location.reload();
			parent.mailboxframe.location.reload();
		}
	</script>
</body>
</html>