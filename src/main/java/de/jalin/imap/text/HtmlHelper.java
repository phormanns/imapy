package de.jalin.imap.text;

public class HtmlHelper {

	public static String replaceEntities(String src) {
		src = replaceString(src, "&", "&amp;");
		src = replaceString(src, "<", "&lt;");
		src = replaceString(src, ">", "&gt;");
		src = replaceString(src, "\"", "&quot;");
		return src;
	}

	private static final String replaceString(final String orig, final String src, final String dest) {
		if (orig == null) {
			return null;
		}
		if (src == null || dest == null) {
			throw new NullPointerException();
		}
		if (src.length() == 0) {
			return orig;
		}
		final StringBuffer res = new StringBuffer(orig.length() + 20); 
		int start = 0;
		int end = 0;
		int last = 0;
		while ((start = orig.indexOf(src, end)) != -1) {
			res.append(orig.substring(last, start));
			res.append(dest);
			end = start + src.length();
			last = start + src.length();
		}
		res.append(orig.substring(end));
		return res.toString();
	}

}
