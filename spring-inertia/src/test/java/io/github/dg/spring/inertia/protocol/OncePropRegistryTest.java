package io.github.dg.spring.inertia.protocol;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OncePropRegistryTest {

    private final OncePropRegistry registry = new OncePropRegistry();
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
    void remembersMetadata() {
        registry.remember("notice", null);
        registry.remember("token", Duration.ofMinutes(5));
        var metadata = registry.metadata();
        assertTrue(metadata.containsKey("notice"));
        assertTrue(metadata.containsKey("token"));
        assertTrue(metadata.get("token").expiresAt() > System.currentTimeMillis());
    }

    @Test
    void unknownKeysAreNotShown() {
        assertFalse(registry.alreadyShown("notice"));
    }

    @Test
    void markShownSurvivesIntoSession() {
        registry.markShown("notice");
        assertTrue(registry.alreadyShown("notice"));
    }

    @Test
    void metadataRoundTrips() {
        registry.remember("notice", null);
        var once = registry.metadata().get("notice");
        assertTrue(once.prop().equals("notice"));
    }

    @Test
    void shownStatePersistsAcrossRegistries() {
        registry.markShown("notice");
        var other = new OncePropRegistry();
        assertTrue(other.alreadyShown("notice"));
    }
}