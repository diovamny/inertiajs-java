package io.github.dg.spring.inertia.mvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import io.github.dg.spring.inertia.config.InertiaProperties;
import io.github.dg.spring.inertia.security.InertiaCsrfService;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression: the {@code XSRF-TOKEN} cookie must be attached to the
 * response <em>before</em> the filter chain runs. Adding it afterwards
 * works under MockMvc (responses are never committed) but is silently
 * dropped by real servlet containers, where the response is already
 * committed once MVC has written the body.
 */
class InertiaCsrfFilterTest {

    private InertiaCsrfFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        var properties = new InertiaProperties();
        properties.setCsrfEnabled(true);
        filter = new InertiaCsrfFilter(properties, new InertiaCsrfService());
        request = new MockHttpServletRequest("GET", "/dashboard");
        request.addHeader("X-Inertia", "true");
        response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void cookieIsSetBeforeTheChainRuns() throws Exception {
        filter.doFilter(request, response, new FilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res) {
                var cookie = response.getCookie("XSRF-TOKEN");
                assertNotNull(cookie, "XSRF-TOKEN cookie must be set before the chain runs");
                assertTrue(!cookie.getValue().isBlank(), "cookie value must be non-blank");
            }
        });
    }
}
