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
import java.util.*;

@Component
@Order(1)
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final Map<String, Role> keyToRole = new HashMap<>();
    private final boolean enforce;

    private static final Set<String> PUBLIC_GET = Set.of(
            "/health", "/api/chain", "/actuator", "/", "/index.html", "/favicon.ico",
            "/api/checkpoints/public-key"
    );

    public ApiKeyAuthFilter(
            @Value("${auditchain.api-key:}") String legacyKey,
            @Value("${auditchain.api-key.officer:}") String officerKey,
            @Value("${auditchain.api-key.reviewer:}") String reviewerKey,
            @Value("${auditchain.api-key.auditor:}") String auditorKey,
            @Value("${auditchain.api-key.admin:}") String adminKey,
            @Value("${auditchain.auth.enforce:true}") boolean enforceFlag) {
        if (legacyKey != null && !legacyKey.isBlank()) keyToRole.put(legacyKey.trim(), Role.ADMIN);
        if (adminKey != null && !adminKey.isBlank()) keyToRole.put(adminKey.trim(), Role.ADMIN);
        if (officerKey != null && !officerKey.isBlank()) keyToRole.put(officerKey.trim(), Role.OFFICER);
        if (reviewerKey != null && !reviewerKey.isBlank()) keyToRole.put(reviewerKey.trim(), Role.REVIEWER);
        if (auditorKey != null && !auditorKey.isBlank()) keyToRole.put(auditorKey.trim(), Role.AUDITOR);
        this.enforce = enforceFlag;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!enforce || isPublic(request)) {
            chain.doFilter(request, response);
            return;
        }
        String provided = request.getHeader("X-API-Key");
        Role role = provided == null ? null : keyToRole.get(provided.trim());
        if (role == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"missing or invalid X-API-Key\"}");
            return;
        }
        if (!authorized(request, role)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"insufficient role\",\"role\":\"" + role + "\"}");
            return;
        }
        request.setAttribute("auditchain.role", role);
        chain.doFilter(request, response);
    }

    private boolean isPublic(HttpServletRequest req) {
        if (!"GET".equalsIgnoreCase(req.getMethod())) return false;
        String path = req.getRequestURI();
        for (String p : PUBLIC_GET) {
            if (path.equals(p) || path.startsWith(p + "/")) return true;
        }
        return false;
    }

    private boolean authorized(HttpServletRequest req, Role role) {
        if (role == Role.ADMIN) return true;
        String path = req.getRequestURI();
        String method = req.getMethod();
        if (path.startsWith("/api/evidence") && "POST".equalsIgnoreCase(method)) {
            return role == Role.OFFICER || role == Role.AUDITOR;
        }
        if (path.startsWith("/api/review") && "POST".equalsIgnoreCase(method)) {
            return role == Role.REVIEWER || role == Role.AUDITOR;
        }
        if (path.startsWith("/api/checkpoints") && "POST".equalsIgnoreCase(method)) {
            return role == Role.AUDITOR;
        }
        return true;
    }
}
