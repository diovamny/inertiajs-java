package io.github.dg.spring.inertia.protocol;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RedirectProcessorTest {

    private final RedirectProcessor processor = new RedirectProcessor(new TestFlashStore());
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

    @Test
    void redirectGetIs302() {
        request.setMethod("GET");
        var redirect = processor.redirect("/dashboard", false);
        assertEquals(302, redirect.getStatusCode().value());
        assertEquals("/dashboard", redirect.getHeaders().getFirst("Location"));
    }

    @Test
    void redirectPostIs303() {
        request.setMethod("POST");
        var redirect = processor.redirect("/dashboard", false);
        assertEquals(303, redirect.getStatusCode().value());
    }

    @Test
    void fullPageRedirectOnInertiaVisitIs409() {
        request.setMethod("GET");
        request.addHeader("X-Inertia", "true");
        var redirect = processor.redirect("/external", true);
        assertEquals(409, redirect.getStatusCode().value());
        assertEquals("/external", redirect.getHeaders().getFirst("X-Inertia-Location"));
        assertNull(redirect.getHeaders().getFirst("Location"));
    }

    @Test
    void fragmentRedirectOnInertiaVisitIs409RedirectHeader() {
        request.setMethod("GET");
        request.addHeader("X-Inertia", "true");
        var redirect = processor.redirect("/section#top", false);
        assertEquals(409, redirect.getStatusCode().value());
        assertEquals("/section#top", redirect.getHeaders().getFirst("X-Inertia-Redirect"));
    }

    @Test
    void locationOnInertiaVisitIs409() {
        request.addHeader("X-Inertia", "true");
        var redirect = processor.location("/download");
        assertEquals(409, redirect.getStatusCode().value());
        assertEquals("/download", redirect.getHeaders().getFirst("X-Inertia-Location"));
    }

    @Test
    void backUsesReferer() {
        request.setMethod("GET");
        request.addHeader("Referer", "/previous");
        var redirect = processor.back();
        assertEquals("/previous", redirect.getHeaders().getFirst("Location"));
    }

    @Test
    void backFallsBackToRoot() {
        request.setMethod("GET");
        var redirect = processor.back();
        assertEquals("/", redirect.getHeaders().getFirst("Location"));
    }

    @Test
    void customStatusAndHeaders() {
        request.setMethod("POST");
        var redirect = processor.back(302, Map.of("X-Custom", "yes"));
        assertEquals(302, redirect.getStatusCode().value());
        assertEquals("yes", redirect.getHeaders().getFirst("X-Custom"));
    }

    @Test
    void flashStoreReceivesWith() {
        var store = new TestFlashStore();
        var processor2 = new RedirectProcessor(store);
        request.setMethod("GET");
        var redirect = processor2.redirect("/x", false).with("message", "hi");
        assertEquals("hi", store.stored.get("message"));
        assertEquals("/x", redirect.getHeaders().getFirst("Location"));
    }

    private static final class TestFlashStore implements io.github.dg.spring.inertia.spi.FlashStore {
        final Map<String, Object> stored = new java.util.LinkedHashMap<>();

        @Override public void put(String key, Object value) { stored.put(key, value); }
        @Override public void putAll(Map<String, Object> values) { stored.putAll(values); }
        @Override public Object get(String key, Object defaultValue) { return stored.getOrDefault(key, defaultValue); }
        @Override public Object pull(String key, Object defaultValue) { return stored.remove(key); }
        @Override public Map<String, Object> drain() { return Map.of(); }
        @Override public boolean hasData() { return !stored.isEmpty(); }
    }
}