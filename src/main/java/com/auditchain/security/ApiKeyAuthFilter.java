package com.auditchain.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Simple API-key gate for write endpoints. Key comes from env AUDITCHAIN_API_KEY.
 * Health, chain read, and static remain public. No hardcoded secrets.
 */
@Component
@Order(1)
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final String expectedKey;
    private final boolean enforce;

    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/health", "/api/chain", "/actuator", "/", "/index.html", "/favicon.ico"
    );

    public ApiKeyAuthFilter(
            @Value("${auditchain.api-key:}") String apiKey,
            @Value("${auditchain.auth.enforce:true}") boolean enforce) {
        this.expectedKey = apiKey == null ? "" : apiKey.trim();
        this.enforce = enforce && !this.expectedKey.isEmpty();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!enforce || isPublic(request)) {
            chain.doFilter(request, response);
            return;
        }
        String provided = request.getHeader("X-API-Key");
        if (provided == null || provided.isBlank() || !expectedKey.equals(provided.trim())) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"missing or invalid X-API-Key\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isPublic(HttpServletRequest req) {
        String path = req.getRequestURI();
        if ("GET".equalsIgnoreCase(req.getMethod())) {
            for (String p : PUBLIC_PREFIXES) {
                if (path.equals(p) || path.startsWith(p + "/")) return true;
            }
        }
        return false;
    }
}
