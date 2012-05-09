package de.jalin.feedserver;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import de.jalin.javamail.EMailReader;
import de.jalin.jspwiki.plugin.imap.IMAPProxy;
import de.jalin.jspwiki.plugin.imap.MessageBox;
import de.jalin.message.MessageData;
import de.jalin.message.MessageList;

public class IMAPServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String host = request.getParameter("hst");
		String user = request.getParameter("usr");
		String password = request.getParameter("pwd");
		IMAPProxy imapProxy = IMAPProxy.getInstance();
		MessageBox box = imapProxy.getMessageBox(host, user, password);
		List<MessageList> folders = box.getFolders();
		JSONObject feed = new JSONObject();
		feed.put("title", "no folders");
		JSONArray items = new JSONArray();
		JSONObject item = null;
		if (folders.size() > 0) {
			int folderIdx = 0;
			MessageList messageList = folders.get(folderIdx);
			while (!messageList.getDisplayName().startsWith("Postei")) {
				folderIdx++;
				messageList = folders.get(folderIdx);
				if (!messageList.isUpToDate()) {
//					box.update(folders.);
				}
			}
			feed.put("title", messageList.getDisplayName());
			List<MessageData> messagesList = messageList.getMessagesList();
			for (int idx = 0; idx < messagesList.size() && idx < 20; ++idx) {
				MessageData messageData = messagesList.get(idx);
				item = new JSONObject();
				item.put("title", messageData.getSubject());
				item.put("author", messageData.getFrom());
				item.put("content", messageData.getFormattedText());
				items.add(item);
			}
		}
	}

}
