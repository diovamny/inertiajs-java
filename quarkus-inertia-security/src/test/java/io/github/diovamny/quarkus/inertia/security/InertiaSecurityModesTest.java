package io.github.diovamny.quarkus.inertia.security;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;

class InertiaSecurityModesTest {

    @Test
    void frameworkAvailableWhenBothMarkersPresent() {
        assertTrue(InertiaSecurityModes.isFrameworkAvailable());
    }

    @Test
    void explicitModeWins() {
        var config = mock(InertiaConfig.class);
        when(config.securityMode()).thenReturn(Optional.of("adapter"));
        assertEquals(InertiaSecurityModes.Mode.ADAPTER, InertiaSecurityModes.configuredMode(config));
    }

    @Test
    void invalidModeFailsFast() {
        var config = mock(InertiaConfig.class);
        when(config.securityMode()).thenReturn(Optional.of("paranoid"));
        assertThrows(IllegalArgumentException.class, () -> InertiaSecurityModes.configuredMode(config));
    }

    @Test
    void sameOriginRefererAccepted() {
        assertTrue(InertiaSecurityModes.isSameOriginReferer(
            "http://localhost:8081/sec/secure", "http", "localhost:8081"));
        assertTrue(InertiaSecurityModes.isSameOriginReferer(
            "https://app.example/orders", "https", "app.example"));
    }

    @Test
    void crossOriginOrInvalidRefererRejected() {
        assertFalse(InertiaSecurityModes.isSameOriginReferer(
            "https://evil.example/phish", "http", "localhost:8081"));
        assertFalse(InertiaSecurityModes.isSameOriginReferer(
            "http://localhost:9090/other", "http", "localhost:8081"));
        assertFalse(InertiaSecurityModes.isSameOriginReferer("/relative", "http", "localhost:8081"));
        assertFalse(InertiaSecurityModes.isSameOriginReferer(null, "http", "localhost:8081"));
    }

    @Test
    void safeFailureTargetFallsBackOnExternalReferer() {
        assertEquals("http://localhost:8081/secure",
            InertiaSecurityModes.safeFailureTarget("http://localhost:8081/secure",
                "http", "localhost:8081", "/"));
        assertEquals("/",
            InertiaSecurityModes.safeFailureTarget("https://evil.example/x",
                "http", "localhost:8081", "/"));
    }

    @Test
    void loginUrlValidation() {
        assertEquals("/login", InertiaChallengeUrls.validatedLoginUrl("/login"));
        assertEquals("https://idp.example/authorize",
            InertiaChallengeUrls.validatedLoginUrl("https://idp.example/authorize"));
        assertEquals("/login", InertiaChallengeUrls.validatedLoginUrl("http://evil.example/"));
        assertEquals("/login", InertiaChallengeUrls.validatedLoginUrl(null));
    }
}
