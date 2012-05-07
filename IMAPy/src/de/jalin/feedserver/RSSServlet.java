package de.jalin.feedserver;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class RSSServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject feed = new JSONObject();
		feed.put("title", "My 1st Testfeed");
		JSONArray items = new JSONArray();
		JSONObject item = new JSONObject();
		item.put("title", "My 1st Story");
		item.put("content", "Lorem ipsum dolor sit amet, consectetur adipisici " +
				"elit, sed eiusmod tempor incidunt ut labore et dolore magna " +
				"aliqua. Ut enim ad minim veniam, quis nostrud exercitation " +
				"ullamco laboris nisi ut aliquid ex ea commodi consequat.\n" +
				"Quis aute iure reprehenderit in voluptate velit esse cillum " +
				"dolore eu fugiat nulla pariatur.\n\n" +
				"Excepteur sint obcaecat cupiditat non proident, sunt in culpa " +
				"qui officia deserunt mollit anim id est laborum.");
		items.add(item);
		feed.put("items", items);
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		writer.append(feed.toJSONString());
		writer.close();
	}

}
