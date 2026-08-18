<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="de.jalin.imap.*"%>
<%@ page import="java.util.*"%>
<%
	String ctx = request.getContextPath();
	String userEmail = (String) session.getAttribute("email");
	String userInitial = "";
	if (userEmail != null && !userEmail.isEmpty()) {
		int at = userEmail.indexOf("@");
		String local = at > 0 ? userEmail.substring(0, at) : userEmail;
		userInitial = local.substring(0, 1).toUpperCase();
	}
	String activeFolder = request.getParameter("folder");
	if (activeFolder == null) {
		activeFolder = "INBOX";
	}
%>
<!doctype html>
<html lang="de">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>IMAPy – <%= userEmail != null ? userEmail : "Mailbox" %></title>
	<link rel="stylesheet" href="<%= ctx %>/style.css">
	<script src="<%= ctx %>/webjars/htmx.org/2.0.8/dist/htmx.min.js"></script>
</head>
<body>
	<div class="app-shell">
		<header class="app-header">
			<div class="app-header-left">
				<button type="button" class="icon-button nav-toggle" aria-label="Navigation umschalten" onclick="toggleNav()">
					<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
				</button>
				<a href="<%= ctx %>/mailbox" class="app-brand">
					<span class="app-brand-mark">iM</span>
					<span>IMAPy</span>
				</a>
			</div>
			<div class="app-header-right">
				<% if (userEmail != null) { %>
					<span class="user-chip" title="<%= userEmail %>">
						<span class="user-avatar"><%= userInitial %></span>
						<span><%= userEmail %></span>
					</span>
				<% } %>
			</div>
		</header>

		<div class="app-content">
			<div id="nav-backdrop" class="nav-backdrop" onclick="closeNav()"></div>

			<aside id="nav" class="app-nav">
				<div class="nav-section">
					<h3 class="nav-section-title">Ordner</h3>
					<ul class="nav-list">
<%
	final Object foldersListObj = session.getAttribute("folders");
	if (foldersListObj instanceof List<?>) {
		final List<?> foldersList = (List<?>) foldersListObj;
		for (final Object folderMapObj : foldersList) {
			if (folderMapObj instanceof IMAPyFolder) {
				final IMAPyFolder yFolder = (IMAPyFolder) folderMapObj;
				String folderName = yFolder.getName();
				String folderTitle = yFolder.getTitle();
				int unread = yFolder.getUnreadMessageCount();
				int total = yFolder.getTotalMessageCount();
				String itemClass = "nav-item";
				if (unread > 0) itemClass += " is-unread";
				if (folderName.equals(activeFolder)) itemClass += " is-active";
%>
						<li class="<%= itemClass %>"
							data-folder="<%= folderName %>"
							hx-get="<%= ctx %>/folder/<%= folderName %>"
							hx-target="#list"
							hx-trigger="click"
							hx-swap="innerHTML"
							onclick="selectFolder(this, '<%= folderName %>')">
							<span class="nav-item-left">
								<svg class="nav-item-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
								<span class="nav-item-label"><%= folderTitle %></span>
							</span>
							<span class="nav-count"><%= unread %>/<%= total %></span>
						</li>
<%
			}
		}
	}
%>
					</ul>
				</div>
			</aside>

			<section id="list" class="app-list"
				hx-get="<%= ctx %>/folder/<%= activeFolder %>"
				hx-trigger="load"
				hx-swap="innerHTML">
				<div class="empty-state">
					<p>Lade Nachrichten…</p>
				</div>
			</section>

			<section id="main" class="app-main">
				<div class="email-content-empty">
					<svg width="42" height="42" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
					<p class="empty-state-title">Wähle eine Nachricht</p>
					<p>Tippe links auf eine E-Mail, um sie hier zu lesen.</p>
				</div>
			</section>
		</div>
	</div>

	<script type="text/javascript">
		var mediaQueryList = window.matchMedia("(min-width: 900px)");

		function showMessagesList() {
			if (!mediaQueryList.matches) {
				document.getElementById("main").style.display = 'none';
				document.getElementById("list").style.display = 'flex';
			}
		}
		function hideMessagesList() {
			if (!mediaQueryList.matches) {
				document.getElementById("list").style.display = 'none';
				document.getElementById("main").style.display = 'block';
			}
		}
		function hideCurrentMessage() {
			showMessagesList();
			document.getElementById("main").innerHTML = '';
		}

		function selectFolder(el, folderName) {
			document.querySelectorAll('.nav-item').forEach(function (n) {
				n.classList.remove('is-active');
			});
			if (el) el.classList.add('is-active');
			closeNav();
		}

		function toggleNav() {
			var nav = document.getElementById('nav');
			var backdrop = document.getElementById('nav-backdrop');
			var isOpen = nav.classList.toggle('is-open');
			if (backdrop) backdrop.classList.toggle('is-open', isOpen);
		}
		function closeNav() {
			var nav = document.getElementById('nav');
			var backdrop = document.getElementById('nav-backdrop');
			if (nav) nav.classList.remove('is-open');
			if (backdrop) backdrop.classList.remove('is-open');
		}

		document.body.addEventListener('htmx:afterSwap', function (evt) {
			if (evt.target.id === 'main') {
				var toolbar = evt.target.querySelector('.email-content-toolbar .back-to-list');
				if (toolbar) toolbar.style.display = '';
				hideMessagesList();
				window.scrollTo(0, 0);
			}
		});

		document.body.addEventListener('click', function (e) {
			var t = e.target.closest('.back-to-list');
			if (t) {
				e.preventDefault();
				hideCurrentMessage();
			}
		});
	</script>
</body>
</html>
