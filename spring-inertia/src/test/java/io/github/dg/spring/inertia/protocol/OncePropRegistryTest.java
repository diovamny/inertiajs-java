package io.github.dg.spring.inertia.protocol;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OncePropRegistryTest {

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
    }

    private void extractHeaders() {
        // Clear the extracted flag to allow re-extraction in tests
        request.removeAttribute(InertiaHeaderExtractor.CONTEXT_HEADERS_EXTRACTED);
        new InertiaHeaderExtractor().extract(request);
    }

    @Test
    void remembersMetadata() {
        var registry = new OncePropRegistry();
        registry.remember("notice", null);
        registry.remember("token", Duration.ofMinutes(5));
        var metadata = registry.metadata();
        assertTrue(metadata.containsKey("notice"));
        assertTrue(metadata.containsKey("token"));
        assertTrue(metadata.get("token").expiresAt() > System.currentTimeMillis());
    }

    @Test
    void unknownKeysAreNotShown() {
        var registry = new OncePropRegistry();
        extractHeaders();
        assertFalse(registry.alreadyShown("notice"));
    }

    @Test
    void alreadyShownUsesExceptOncePropsHeader() {
        var registry = new OncePropRegistry();
        registry.remember("notice", null);
        // Without header, not shown
        extractHeaders();
        assertFalse(registry.alreadyShown("notice"));
        // With header, shown
        request.addHeader("X-Inertia-Except-Once-Props", "notice");
        extractHeaders();
        assertTrue(registry.alreadyShown("notice"));
    }

    @Test
    void markShownIsNoOp() {
        var registry = new OncePropRegistry();
        registry.remember("notice", null);
        registry.markShown("notice");
        // Still not shown without header
        extractHeaders();
        assertFalse(registry.alreadyShown("notice"));
    }

    @Test
    void metadataRoundTrips() {
        var registry = new OncePropRegistry();
        registry.remember("notice", null);
        var once = registry.metadata().get("notice");
        assertTrue(once.prop().equals("notice"));
    }

    @Test
    void exceptOnceKeysParsesCommaSeparated() {
        var registry = new OncePropRegistry();
        request.addHeader("X-Inertia-Except-Once-Props", "notice,token,other");
        extractHeaders();
        var exceptKeys = registry.exceptOnceKeys();
        assertTrue(exceptKeys.contains("notice"));
        assertTrue(exceptKeys.contains("token"));
        assertTrue(exceptKeys.contains("other"));
    }
}
