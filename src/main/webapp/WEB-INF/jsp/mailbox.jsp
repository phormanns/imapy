<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%
    String ctx = request.getContextPath();
    String userEmail = (String) session.getAttribute("email");
    String userInitial = "?";
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
        <title>IMAPy – <%= userEmail != null ? userEmail : "Mailbox"%></title>
        <link rel="icon" type="image/x-icon" href="<%= ctx%>/favicon.ico">
        <link rel="stylesheet" href="<%= ctx%>/style.css">
        <script src="<%= ctx%>/webjars/htmx.org/2.0.10/dist/htmx.min.js"></script>
    </head>
    <body>
        <div class="app-shell">
            <header class="app-header">
                <div class="app-header-left">
                    <button type="button" class="icon-button nav-toggle" aria-label="Navigation umschalten" onclick="toggleNav()">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
                    </button>
                    <a href="<%= ctx%>/mailbox" class="app-brand">
                        <span class="app-brand-mark">iM</span>
                        <span>IMAPy</span>
                    </a>
                </div>
                <div class="app-header-right">
                    <% if (userEmail != null) {%>
                    <span class="user-chip" title="<%= userEmail%>">
                        <span class="user-avatar"><%= userInitial%></span>
                        <span><%= userEmail%></span>
                    </span>
                    <form class="logout-form" method="post" action="<%= ctx%>/logout">
                        <button type="submit" class="btn btn-ghost" title="Abmelden">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                            <span>Abmelden</span>
                        </button>
                    </form>
                    <% } %>
                </div>
            </header>

            <div class="app-content">
                <div id="nav-backdrop" class="nav-backdrop" onclick="closeNav()"></div>

                <aside id="nav" class="app-nav"
                       hx-get="<%= ctx%>/folderlist"
                       hx-trigger="load"
                       hx-swap="innerHTML">
                    <div class="nav-section">
                        <h3 class="nav-section-title">Ordner</h3>
                        <div class="empty-state">
                            <p>Lade Ordner…</p>
                        </div>
                    </div>
                </aside>

                <section id="list" class="app-list"
                         hx-get="<%= ctx%>/folder/<%= activeFolder%>"
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
                if (el)
                    el.classList.add('is-active');
                closeNav();
            }

            function selectMessage(el, folderName, messageIndex) {
                document.querySelectorAll('.email-item').forEach(function (n) {
                    n.classList.remove('is-active');
                });
                if (el)
                    el.classList.add('is-active');
                hideMessagesList();
            }

            function toggleNav() {
                var nav = document.getElementById('nav');
                var backdrop = document.getElementById('nav-backdrop');
                var isOpen = nav.classList.toggle('is-open');
                if (backdrop)
                    backdrop.classList.toggle('is-open', isOpen);
            }
            function closeNav() {
                var nav = document.getElementById('nav');
                var backdrop = document.getElementById('nav-backdrop');
                if (nav)
                    nav.classList.remove('is-open');
                if (backdrop)
                    backdrop.classList.remove('is-open');
            }

            function activeFolderName() {
                var active = document.querySelector('.nav-item.is-active');
                if (active) {
                    return active.getAttribute('data-folder');
                }
                var params = new URLSearchParams(window.location.search);
                return params.get('folder') || 'INBOX';
            }

            function refreshMailbox() {
                var folder = activeFolderName();
                var listUrl = '<%= ctx%>/folder/' + folder;
                var folderListUrl = '<%= ctx%>/folderlist?folder=' + encodeURIComponent(folder);
                if (window.htmx) {
                    htmx.ajax('GET', folderListUrl, { target: '#nav', swap: 'innerHTML' });
                    htmx.ajax('GET', listUrl, { target: '#list', swap: 'innerHTML' });
                } else {
                    window.location.href = listUrl;
                }
            }

            document.body.addEventListener('htmx:afterSwap', function (evt) {
                if (evt.target.id === 'main') {
                    var toolbar = evt.target.querySelector('.email-content-toolbar .back-to-list');
                    if (toolbar)
                        toolbar.style.display = '';
                    hideMessagesList();
                    window.scrollTo(0, 0);
                }
            });

            document.body.addEventListener('messages-changed', function () {
                var folder = activeFolderName();
                var listUrl = '<%= ctx%>/folder/' + folder;
                var folderListUrl = '<%= ctx%>/folderlist?folder=' + encodeURIComponent(folder);
                if (window.htmx) {
                    htmx.ajax('GET', folderListUrl, { target: '#nav', swap: 'innerHTML' });
                    htmx.ajax('GET', listUrl, { target: '#list', swap: 'innerHTML' });
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
