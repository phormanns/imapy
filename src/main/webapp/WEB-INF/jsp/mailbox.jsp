<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
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
    pageContext.setAttribute("ctx", ctx);
    pageContext.setAttribute("userEmail", userEmail);
    pageContext.setAttribute("userInitial", userInitial);
    pageContext.setAttribute("activeFolder", activeFolder);
%>
<!doctype html>
<html lang="de">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>IMAPy – <c:out value="${empty userEmail ? 'Mailbox' : userEmail}"/></title>
        <link rel="icon" type="image/x-icon" href="<c:out value="${ctx}"/>/favicon.ico">
        <link rel="stylesheet" href="<c:out value="${ctx}"/>/style.css">
        <script src="<c:out value="${ctx}"/>/webjars/htmx.org/2.0.10/dist/htmx.min.js"></script>
    </head>
    <body>
        <div class="app-shell">
            <header class="app-header">
                <div class="app-header-left">
                    <button type="button" class="icon-button nav-toggle" aria-label="Navigation umschalten" onclick="toggleNav()">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
                    </button>
                    <a href="<c:out value="${ctx}"/>/mailbox" class="app-brand">
                        <span class="app-brand-mark">iM</span>
                        <span>IMAPy</span>
                    </a>
                </div>
                <div class="app-header-right">
                    <c:if test="${not empty userEmail}">
                    <span class="user-chip" title="<c:out value="${userEmail}"/>">
                        <span class="user-avatar"><c:out value="${userInitial}"/></span>
                        <span><c:out value="${userEmail}"/></span>
                    </span>
                    <form class="logout-form" method="post" action="<c:out value="${ctx}"/>/logout">
                        <input type="hidden" name="csrf_token" value="${sessionScope.csrf_token}"> 
                        <button type="submit" class="btn btn-ghost" title="Abmelden">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                            <span>Abmelden</span>
                        </button>
                    </form>
                    </c:if>
                </div>
            </header>

            <div class="app-content">
                <div id="nav-backdrop" class="nav-backdrop" onclick="closeNav()"></div>

                <aside id="nav" class="app-nav"
                       hx-get="<c:out value="${ctx}"/>/folderlist"
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
                         hx-get="<c:out value="${ctx}"/>/folder/<c:out value="${activeFolder}"/>"
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
            var csrfToken = '<c:out value="${sessionScope.csrf_token}"/>';
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

            function selectMessage(el, folderName, messageUid) {
                document.querySelectorAll('.email-item').forEach(function (n) {
                    n.classList.remove('is-active');
                });
                if (el)
                    el.classList.add('is-active');
                hideMessagesList();
            }

            function dragMessageStart(evt, el) {
                var folder = el.getAttribute('data-folder');
                var uid = el.getAttribute('data-message-uid');
                if (!folder || !uid) {
                    evt.preventDefault();
                    return;
                }
                el.classList.add('is-dragging');
                evt.dataTransfer.effectAllowed = 'move';
                evt.dataTransfer.setData('text/plain', JSON.stringify({folder: folder, uid: uid}));
            }

            function messageDragEnd(evt, el) {
                el.classList.remove('is-dragging');
                document.querySelectorAll('.nav-item.drag-over').forEach(function (n) {
                    n.classList.remove('drag-over');
                });
            }

            function folderDragOver(evt, el) {
                evt.preventDefault();
                evt.dataTransfer.dropEffect = 'move';
                el.classList.add('drag-over');
            }

            function folderDragLeave(evt, el) {
                if (!el.contains(evt.relatedTarget)) {
                    el.classList.remove('drag-over');
                }
            }

            function folderDrop(evt, el) {
                evt.preventDefault();
                el.classList.remove('drag-over');
                var payload = null;
                try {
                    payload = JSON.parse(evt.dataTransfer.getData('text/plain'));
                } catch (e) {
                    payload = null;
                }
                if (!payload || !payload.folder || !payload.uid) {
                    return false;
                }
                var targetFolder = el.getAttribute('data-folder');
                if (!targetFolder || payload.folder === targetFolder) {
                    return false;
                }
                moveMessageToFolder(payload.folder, payload.uid, targetFolder);
                return false;
            }

            function moveMessageToFolder(srcFolder, uid, targetFolder) {
                var url = '<c:out value="${ctx}"/>/message/' + encodeURIComponent(srcFolder)
                        + '/' + encodeURIComponent(uid) + '/moveto/' + encodeURIComponent(targetFolder);
                fetch(url, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                    body: 'csrf_token=' + encodeURIComponent(csrfToken)
                }).then(function (res) {
                    if (res.ok) {
                        refreshMailbox();
                    } else {
                        alert('Nachricht konnte nicht verschoben werden (HTTP ' + res.status + ')');
                    }
                }).catch(function () {
                    alert('Nachricht konnte nicht verschoben werden');
                });
            }

            function execComposeCommand(cmd) {
                var editor = document.getElementById('compose-editor');
                if (!editor) {
                    return;
                }
                editor.focus();
                document.execCommand(cmd, false, null);
            }

            function sendCompose(btn) {
                var to = document.getElementById('compose-to').value.trim();
                var subject = document.getElementById('compose-subject').value.trim();
                var editor = document.getElementById('compose-editor');
                var html = editor ? editor.innerHTML : '';
                if (!to) {
                    alert('Bitte einen Empfänger angeben.');
                    return;
                }
                if (!subject && !(editor && editor.innerText.trim())) {
                    alert('Bitte einen Betreff oder eine Nachricht eingeben.');
                    return;
                }
                if (btn) {
                    btn.disabled = true;
                }
                var inReplyToEl = document.getElementById('compose-in-reply-to');
                var referencesEl = document.getElementById('compose-references');
                var body = 'csrf_token=' + encodeURIComponent(csrfToken)
                        + '&to=' + encodeURIComponent(to)
                        + '&subject=' + encodeURIComponent(subject)
                        + '&body=' + encodeURIComponent(html)
                        + '&inReplyTo=' + encodeURIComponent(inReplyToEl ? inReplyToEl.value : '')
                        + '&references=' + encodeURIComponent(referencesEl ? referencesEl.value : '');
                fetch('<c:out value="${ctx}"/>/compose', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                    body: body
                }).then(function (res) {
                    if (!res.ok) {
                        throw new Error('HTTP ' + res.status);
                    }
                    return res.text();
                }).then(function (fragment) {
                    var main = document.getElementById('main');
                    if (main) {
                        main.innerHTML = fragment;
                    }
                    hideMessagesList();
                    document.body.dispatchEvent(new Event('messages-changed'));
                }).catch(function (err) {
                    alert('Die Nachricht konnte nicht gesendet werden (' + err.message + ').');
                }).finally(function () {
                    if (btn) {
                        btn.disabled = false;
                    }
                });
            }

            function quoteMessage(text) {
                return text.split('\n').map(function (line) {
                    return line.length ? '> ' + line : '>';
                }).join('\n');
            }

            function openReply(btn) {
                var to = btn.getAttribute('data-reply-to') || '';
                var subject = (btn.getAttribute('data-reply-subject') || '').trim();
                if (subject && !/^re:/i.test(subject)) {
                    subject = 'Re: ' + subject;
                }
                var replyDate = btn.getAttribute('data-reply-date') || '';
                var messageId = btn.getAttribute('data-reply-message-id') || '';
                var origReferences = btn.getAttribute('data-reply-references') || '';
                var bodyEl = document.querySelector('.email-content-body');
                var quoted = '';
                if (bodyEl && bodyEl.innerText.trim()) {
                    var original = bodyEl.innerText.replace(/\r\n/g, '\n').trim();
                    quoted = 'Am ' + replyDate + ' schrieb ' + to + ':\n' + quoteMessage(original);
                }
                fetch('<c:out value="${ctx}"/>/compose', {method: 'GET'})
                    .then(function (res) {
                        if (!res.ok) {
                            throw new Error('HTTP ' + res.status);
                        }
                        return res.text();
                    }).then(function (fragment) {
                        var main = document.getElementById('main');
                        if (!main) {
                            return;
                        }
                        main.innerHTML = fragment;
                        hideMessagesList();
                        window.scrollTo(0, 0);
                        var toEl = document.getElementById('compose-to');
                        var subjEl = document.getElementById('compose-subject');
                        var editor = document.getElementById('compose-editor');
                        if (toEl) {
                            toEl.value = to;
                        }
                        if (subjEl) {
                            subjEl.value = subject;
                        }
                        var inReplyToEl = document.getElementById('compose-in-reply-to');
                        var referencesEl = document.getElementById('compose-references');
                        if (inReplyToEl) {
                            inReplyToEl.value = messageId;
                        }
                        if (referencesEl) {
                            referencesEl.value = origReferences;
                        }
                        if (editor) {
                            editor.innerText = quoted ? quoted + '\n\n' : '';
                            editor.focus();
                        }
                    }).catch(function (err) {
                        alert('Antwort kann nicht erstellt werden (' + err.message + ').');
                    });
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
                var listUrl = '<c:out value="${ctx}"/>/folder/' + folder;
                var folderListUrl = '<c:out value="${ctx}"/>/folderlist?folder=' + encodeURIComponent(folder);
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
                var listUrl = '<c:out value="${ctx}"/>/folder/' + folder;
                var folderListUrl = '<c:out value="${ctx}"/>/folderlist?folder=' + encodeURIComponent(folder);
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
