<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="de.jalin.imap.*"%>
<%@ page import="java.util.*"%>

<%
	final Object attrObj = session.getAttribute("message");
	if (attrObj instanceof IMAPyMessage) {
		final IMAPyMessage message = (IMAPyMessage) session.getAttribute("message");
 %>
    
        <div class="email-content" hx-get="<%= request.getContextPath() %>/folder/<%= message.getFolder() %>" hx-trigger="load" hx-target="#list">
            <div class="email-content-header pure-g">
                <div class="pure-u-1-2">
                    <h1 class="email-content-title"><%= message.getSubject() %></h1>
                    <p class="email-content-subtitle">
                        From <%= message.getFrom() %> at <span><%= message.getDate() %></span>
                    </p>
                </div>

                <div class="email-content-controls pure-u-1-2">
                    <button class="secondary-button pure-button">Reply</button>
                    <button class="secondary-button pure-button">Forward</button>
                    <button class="secondary-button pure-button">Move to</button>
                </div>
            </div>

            <div class="email-content-body">
            	<%= message.getContent() %>
            </div>
        </div>
<%
	}
 %>
