<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="de.jalin.imap.*"%>
<%@ page import="java.util.*"%>
		
<%
	final Object messagesListObj = session.getAttribute("messages");
	if (messagesListObj instanceof List<?>) {
		String maxListLengthAttr = (String)session.getAttribute("max_list_length");
		int maxListLength = Integer.parseInt(maxListLengthAttr); 
		String targetAttrib = "messagesframe";
		if ("true".equals(session.getAttribute("mobile"))) { 
			targetAttrib = "_self";
		}
		final List<?> messagesList = (List<?>) messagesListObj;
		int loopCount = 0;
		for (final Object messageMapObj : messagesList) {
			loopCount++;
			if (loopCount > maxListLength) {
				break;
			}
			if (messageMapObj instanceof IMAPyMessage) {
				final IMAPyMessage yMessage = (IMAPyMessage) messageMapObj;
 %>
		 	<div class="email-item email-item-<%= yMessage.getStatus() %> pure-g">
		 		<div class="pure-u-1">
		 			<h5 class="email-name"><%= yMessage.getAuthor() %></h5>
		 			<h4 class="email-subject"><%= yMessage.getTitle() %></h4>
		 			<p class="email-desc">Preview</p>
		 			<!-- 
					<a target="<%= targetAttrib %>"
						href="<%= request.getContextPath() %>/message/<%= yMessage.getFolder() %>/<%= yMessage.getIndex() %>">
						<%= yMessage.getTitle() %> [von: <%= yMessage.getAuthor() %>]
					</a>  -->
				</div>	
			</div>	
				
<%
			}

		}
	}
 %>
		
