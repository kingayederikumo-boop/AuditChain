package com.auditchain.web;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class I18nScriptInjectionFilterTest {

    private static final String PAGE = "<html><body>hello</body></html>";

    private final FilterChain htmlChain = (req, res) -> {
        res.setContentType("text/html;charset=UTF-8");
        res.getOutputStream().write(PAGE.getBytes(StandardCharsets.UTF_8));
    };

    @Test
    void injectsScriptBeforeClosingBody() throws Exception {
        var response = new MockHttpServletResponse();
        new I18nScriptInjectionFilter().doFilter(new MockHttpServletRequest("GET", "/index.html"), response, htmlChain);
        String out = response.getContentAsString(StandardCharsets.UTF_8);
        assertTrue(out.contains("/i18n.js"));
        assertTrue(out.indexOf("/i18n.js") < out.indexOf("</body>"));
        assertTrue(out.contains("hello"));
    }

    @Test
    void leavesOtherPathsUntouched() throws Exception {
        var response = new MockHttpServletResponse();
        new I18nScriptInjectionFilter().doFilter(new MockHttpServletRequest("GET", "/api/chain"), response, htmlChain);
        String out = response.getContentAsString(StandardCharsets.UTF_8);
        assertEquals(PAGE, out);
        assertFalse(out.contains("/i18n.js"));
    }
}
