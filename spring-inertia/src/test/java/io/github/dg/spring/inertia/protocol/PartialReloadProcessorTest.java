package io.github.dg.spring.inertia.protocol;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartialReloadProcessorTest {

    private final PartialReloadProcessor processor = new PartialReloadProcessor();
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private void partial(String component, String data, String except, String reset) {
        if (component != null) request.setAttribute(InertiaHeaderExtractor.CONTEXT_PARTIAL_COMPONENT, component);
        if (data != null) request.setAttribute(InertiaHeaderExtractor.CONTEXT_PARTIAL_DATA, data);
        if (except != null) request.setAttribute(InertiaHeaderExtractor.CONTEXT_PARTIAL_EXCEPT, except);
        if (reset != null) request.setAttribute(InertiaHeaderExtractor.CONTEXT_PARTIAL_RESET, reset);
    }

    @Test
    void fullPageWhenNoHeaders() {
        Map<String, Object> props = Map.of("a", 1, "b", 2);
        assertEquals(props, processor.filterProps(props, Map.of()));
        assertFalse(processor.isPartialReload("Users"));
        assertFalse(processor.isPartialReset());
    }

    @Test
    void partialDataSelectsProps() {
        partial("Users", "list,count", null, null);
        assertTrue(processor.isPartialReload("Users"));
        assertFalse(processor.isPartialReload("Other"));
        var filtered = processor.filterProps(Map.of("list", List.of(1), "count", 2, "sidebar", "x"), Map.of());
        assertEquals(Map.of("list", List.of(1), "count", 2), filtered);
    }

    @Test
    void partialDataWithAlwaysProps() {
        partial("Users", "count", null, null);
        var filtered = processor.filterProps(Map.of("count", 2), Map.of("auth", "user"));
        assertEquals(Map.<String, Object>of("count", 2, "auth", "user"), filtered);
    }

    @Test
    void partialExceptRemovesProps() {
        partial("Users", null, "sidebar,chat", null);
        var filtered = processor.filterProps(Map.of("list", 1, "sidebar", "x", "chat", "y"), Map.of());
        assertEquals(Map.of("list", 1), filtered);
    }

    @Test
    void partialResetFlag() {
        partial("Users", "count", null, "true");
        assertTrue(processor.isPartialReset());
    }

    @Test
    void partialDataKeepsExplicitNullProps() {
        // Null is a real prop value, not an omission signal: an explicitly
        // requested null prop must be present in the partial response.
        partial("Users", "status,count", null, null);
        var props = new java.util.LinkedHashMap<String, Object>();
        props.put("status", null);
        props.put("count", 2);
        props.put("sidebar", "x");
        var filtered = processor.filterProps(props, Map.of());
        assertTrue(filtered.containsKey("status"));
        assertEquals(null, filtered.get("status"));
        assertEquals(2, filtered.get("count"));
        assertFalse(filtered.containsKey("sidebar"));
    }

    @Test
    void exceptNestedLeafOnly() {
        partial("Users", null, "auth.user.email", null);
        var props = Map.<String, Object>of(
            "auth", Map.of("user", Map.of("id", 1, "email", "john@test.com"), "role", "admin"),
            "other", "value"
        );
        var filtered = processor.filterProps(props, Map.of());
        assertTrue(filtered.containsKey("auth"));
        assertTrue(filtered.containsKey("other"));
        @SuppressWarnings("unchecked")
        var auth = (Map<String, Object>) filtered.get("auth");
        assertTrue(auth.containsKey("user"));
        assertEquals("admin", auth.get("role"));
        @SuppressWarnings("unchecked")
        var user = (Map<String, Object>) auth.get("user");
        assertEquals(1, user.get("id"));
        assertFalse(user.containsKey("email"));
    }
}