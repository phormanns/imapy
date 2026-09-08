package de.jalin.webmail;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class CSRFTokenFilterTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession httpSession;
    private FilterChain chain;
    private CSRFTokenFilter filter;

    @BeforeEach
    public void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        httpSession = mock(HttpSession.class);
        chain = mock(FilterChain.class);
        filter = new CSRFTokenFilter();
        when(request.getSession()).thenReturn(httpSession);
    }

    @Test
    public void createsUuidTokenOnGetRequest() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(httpSession.getAttribute(CSRFTokenFilter.CSRF_TOKEN_ID)).thenReturn(null);

        filter.doFilter(request, response, chain);

        final ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpSession).setAttribute(eq(CSRFTokenFilter.CSRF_TOKEN_ID), tokenCaptor.capture());
        assertNotNull(tokenCaptor.getValue());
        assertDoesNotThrow(() -> UUID.fromString(tokenCaptor.getValue()));
        verify(chain).doFilter(request, response);
    }

    @Test
    public void keepsExistingTokenOnGetRequest() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(httpSession.getAttribute(CSRFTokenFilter.CSRF_TOKEN_ID)).thenReturn("vorhanden");

        filter.doFilter(request, response, chain);

        verify(httpSession, never()).setAttribute(anyString(), any());
        verify(chain).doFilter(request, response);
    }

    @Test
    public void acceptsPostWithMatchingToken() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(httpSession.getAttribute(CSRFTokenFilter.CSRF_TOKEN_ID)).thenReturn("geheim");
        when(request.getParameter(CSRFTokenFilter.CSRF_TOKEN_ID)).thenReturn("geheim");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    public void rejectsPostWithWrongToken() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(httpSession.getAttribute(CSRFTokenFilter.CSRF_TOKEN_ID)).thenReturn("geheim");
        when(request.getParameter(CSRFTokenFilter.CSRF_TOKEN_ID)).thenReturn("anders");

        filter.doFilter(request, response, chain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token invalid");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    public void rejectsPostWithoutSessionToken() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(httpSession.getAttribute(CSRFTokenFilter.CSRF_TOKEN_ID)).thenReturn(null);
        when(request.getParameter(CSRFTokenFilter.CSRF_TOKEN_ID)).thenReturn("egal");

        filter.doFilter(request, response, chain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token invalid");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    public void rejectsPostWithoutRequestToken() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(httpSession.getAttribute(CSRFTokenFilter.CSRF_TOKEN_ID)).thenReturn("geheim");
        when(request.getParameter(CSRFTokenFilter.CSRF_TOKEN_ID)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token invalid");
        verify(chain, never()).doFilter(any(), any());
    }

}
