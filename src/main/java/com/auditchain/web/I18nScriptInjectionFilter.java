package com.auditchain.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Adds the language layer (/i18n.js) to the two public pages without touching their markup.
 * Only GET requests for the console and submit page are affected.
 */
@Component
@Order(2)
public class I18nScriptInjectionFilter extends OncePerRequestFilter {

    static final String SCRIPT_TAG = "<script src=\"/i18n.js\" defer></script>";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        return !(path.equals("/") || path.equals("/index.html") || path.equals("/submit.html"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(request, wrapper);
            String type = wrapper.getContentType();
            byte[] body = wrapper.getContentAsByteArray();
            if (body.length > 0 && type != null && type.toLowerCase().contains("text/html")) {
                Charset charset = StandardCharsets.UTF_8;
                String html = new String(body, charset);
                int at = html.lastIndexOf("</body>");
                if (at >= 0 && !html.contains("/i18n.js")) {
                    String injected = html.substring(0, at) + SCRIPT_TAG + "\n" + html.substring(at);
                    wrapper.resetBuffer();
                    wrapper.getOutputStream().write(injected.getBytes(charset));
                }
            }
        } finally {
            wrapper.copyBodyToResponse();
        }
    }
}
