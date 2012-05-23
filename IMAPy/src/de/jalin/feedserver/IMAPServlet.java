package de.jalin.feedserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import de.jalin.imap.IMAP;
import de.jalin.imapy.IMAPyException;

public class IMAPServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		String host = (String) session.getAttribute("host");
		String user = (String) session.getAttribute("user");
		String password = (String) session.getAttribute("password");
		String email = (String) session.getAttribute("email");
		IMAP imap = (IMAP) session.getAttribute("imap");
		String folder = request.getParameter("fd");
		String msg = request.getParameter("msg");
		try {
			if (imap == null) {
				imap = new IMAP(host, user, password);
				session.setAttribute("imap", imap);
			}
			JSONObject jsonObject = new JSONObject();
			if (folder == null) {
				List<Map<String,String>> list = imap.getFolders();
				jsonObject.put("title", email);
				JSONArray jsonArray = new JSONArray();
				jsonObject.put("items", jsonArray);
				for (Map<String, String> item : list) {
					JSONObject jsonItem = new JSONObject();
					String title = item.get("folder");
					if (title.startsWith("INBOX.")) {
						title = title.substring(6);
					}
					jsonItem.put("title", title);
					jsonItem.put("folder", item.get("folder"));
					jsonItem.put("ntotal", item.get("ntotal"));
					jsonItem.put("nunread", item.get("nunread"));
					jsonItem.put("nunseen", item.get("nunseen"));
					jsonArray.add(jsonItem);
				}
			} else {
				if (msg == null) {
					List<Map<String,String>> list = imap.getMessages(folder);
					jsonObject.put("title", email);
					JSONArray jsonArray = new JSONArray();
					jsonObject.put("items", jsonArray);
					for (Map<String, String> item : list) {
						JSONObject jsonItem = new JSONObject();
						jsonItem.put("author", item.get("author"));
						jsonItem.put("title", item.get("title"));
						jsonItem.put("folder", item.get("folder"));
						jsonItem.put("idx", item.get("idx"));
						jsonItem.put("status", item.get("status"));
						jsonArray.add(jsonItem);
					}
				} else {
					Map<String,String> msgItem = imap.getMessage(folder, msg);
					jsonObject.put("title", msgItem.get("title"));
					jsonObject.put("folder", msgItem.get("folder"));
					jsonObject.put("content", msgItem.get("content"));
					jsonObject.put("author", msgItem.get("author"));
				}
			}
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter writer = response.getWriter();
			writer.append(jsonObject.toJSONString());
			writer.close();
		} catch (IMAPyException e) {
			System.out.println(e.getMessage());
		}
	}

}
