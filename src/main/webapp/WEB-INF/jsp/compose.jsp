<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    String ctx = request.getContextPath();
    pageContext.setAttribute("ctx", ctx);
%>
<div class="email-content compose-content">
    <div class="email-content-toolbar">
        <div class="email-content-toolbar-left">
            <button type="button" class="icon-button back-to-list" aria-label="Zurück zur Liste">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
            </button>
        </div>
        <div class="email-content-toolbar-right">
            <button type="button" id="compose-send" class="btn btn-primary"
                    onclick="sendCompose(this)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
                Senden
            </button>
        </div>
    </div>

    <header class="email-content-header compose-header">
        <div class="compose-field">
            <label for="compose-to">An</label>
            <input id="compose-to" type="text" autocomplete="off" spellcheck="false"
                   placeholder="empfaenger@example.org">
        </div>
        <div class="compose-field">
            <label for="compose-subject">Betreff</label>
            <input id="compose-subject" type="text" autocomplete="off">
        </div>
    </header>

    <div class="compose-toolbar" role="toolbar" aria-label="Textformatierung">
        <button type="button" class="compose-tool" title="Fett"
                onmousedown="event.preventDefault()"
                onclick="execComposeCommand('bold')"><strong>B</strong></button>
        <button type="button" class="compose-tool compose-tool-italic" title="Kursiv"
                onmousedown="event.preventDefault()"
                onclick="execComposeCommand('italic')"><em>I</em></button>
        <span class="compose-tool-divider"></span>
        <button type="button" class="compose-tool" title="Aufzählungsliste"
                onmousedown="event.preventDefault()"
                onclick="execComposeCommand('insertUnorderedList')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
        </button>
        <span class="compose-tool-divider"></span>
        <button type="button" class="compose-tool" title="Anlage anhängen"
                onclick="composePickFiles()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg>
        </button>
        <input type="file" id="compose-file" class="compose-file-input" multiple
               onchange="composeFilesChanged(this)">
    </div>

    <ul class="compose-attachments" id="compose-attachments" hidden></ul>

    <div id="compose-editor" class="compose-editor" contenteditable="true"
         data-placeholder="Nachricht schreiben …"></div>

    <input type="hidden" id="compose-in-reply-to" value="">
    <input type="hidden" id="compose-references" value="">
</div>
