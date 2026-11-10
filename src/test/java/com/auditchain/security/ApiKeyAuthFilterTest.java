package com.auditchain.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiKeyAuthFilterTest {

    private final ApiKeyAuthFilter filter =
            new ApiKeyAuthFilter("", "off-key", "rev-key", "aud-key", "adm-key", true, "test-secret");

    private int status(String method, String uri, String key) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest(method, uri);
        if (key != null) req.addHeader("X-API-Key", key);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, new MockFilterChain());
        return res.getStatus();
    }

    @Test
    void publicReadsNeedNoKey() throws Exception {
        assertEquals(200, status("GET", "/api/chain", null));
        assertEquals(200, status("GET", "/api/checkpoints/verify", null));
    }

    @Test
    void reviewQueueNeedsReviewerOrAuditor() throws Exception {
        assertEquals(401, status("GET", "/api/review/pending", null));
        assertEquals(403, status("GET", "/api/review/pending", "off-key"));
        assertEquals(200, status("GET", "/api/review/pending", "rev-key"));
    }

    @Test
    void evidenceSubmissionIsOfficerOrAuditor() throws Exception {
        assertEquals(200, status("POST", "/api/evidence", "off-key"));
        assertEquals(403, status("POST", "/api/evidence", "rev-key"));
    }

    @Test
    void checkpointCreationIsAuditorOnly() throws Exception {
        assertEquals(200, status("POST", "/api/checkpoints", "aud-key"));
        assertEquals(403, status("POST", "/api/checkpoints", "off-key"));
    }

    @Test
    void destructiveDemoEndpointsAreAdminOnly() throws Exception {
        for (String path : new String[]{"/api/reset", "/api/tamper", "/api/generate"}) {
            assertEquals(403, status("POST", path, "off-key"), path);
            assertEquals(403, status("POST", path, "rev-key"), path);
            assertEquals(403, status("POST", path, "aud-key"), path);
            assertEquals(401, status("POST", path, null), path);
            assertEquals(200, status("POST", path, "adm-key"), path);
        }
    }
}
