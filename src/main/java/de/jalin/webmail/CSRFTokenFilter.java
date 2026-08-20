package de.jalin.webmail;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CSRFTokenFilter implements Filter {

    public static final String CSRF_TOKEN_ID = "csrf_token";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;
        final HttpSession httpSession = httpRequest.getSession(true);
        String sessionToken = (String) httpSession.getAttribute(CSRF_TOKEN_ID);
        if ("GET".equalsIgnoreCase(httpRequest.getMethod())) {
            if (sessionToken == null) {
                httpSession.setAttribute(CSRF_TOKEN_ID, UUID.randomUUID().toString());
            }
        }
        if ("POST".equalsIgnoreCase(httpRequest.getMethod())) {
            String requestToken = httpRequest.getParameter(CSRF_TOKEN_ID);
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token invalid");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
