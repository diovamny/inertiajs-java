package io.github.diovamny.spring.inertia.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.diovamny.spring.inertia.config.InertiaProperties;

class InertiaSecurityModesTest {

    @Test
    void defaultsToAuto() {
        var mode = InertiaSecurityModes.configuredMode(new InertiaProperties());
        assertEquals(InertiaSecurityModes.Mode.AUTO, mode);
    }

    @Test
    void explicitModeWinsOverLegacyAlias() {
        var properties = new InertiaProperties();
        properties.getSecurity().setMode("adapter");
        properties.setCsrfEnabled(false);
        assertEquals(InertiaSecurityModes.Mode.ADAPTER,
            InertiaSecurityModes.configuredMode(properties));
    }

    @Test
    void legacyTrueMapsToAdapterAndLegacyFalseToDisabled() {
        var enabled = new InertiaProperties();
        enabled.setCsrfEnabled(true);
        assertEquals(InertiaSecurityModes.Mode.ADAPTER,
            InertiaSecurityModes.configuredMode(enabled));

        var disabled = new InertiaProperties();
        disabled.setCsrfEnabled(false);
        assertEquals(InertiaSecurityModes.Mode.DISABLED,
            InertiaSecurityModes.configuredMode(disabled));
    }

    @Test
    void invalidModeFailsFast() {
        var properties = new InertiaProperties();
        properties.getSecurity().setMode("paranoid");
        assertThrows(IllegalArgumentException.class,
            () -> InertiaSecurityModes.configuredMode(properties));
    }

    @Test
    void sameOriginRefererAccepted() {
        assertTrue(InertiaSecurityModes.isSameOriginReferer(
            "http://localhost:8080/dashboard", "http", "localhost", 8080));
        assertTrue(InertiaSecurityModes.isSameOriginReferer(
            "https://app.example/orders", "https", "app.example", 443));
    }

    @Test
    void crossOriginOrInvalidRefererRejected() {
        assertFalse(InertiaSecurityModes.isSameOriginReferer(
            "https://evil.example/phish", "http", "localhost", 8080));
        assertFalse(InertiaSecurityModes.isSameOriginReferer(
            "http://localhost:9090/other", "http", "localhost", 8080));
        assertFalse(InertiaSecurityModes.isSameOriginReferer("/relative", "http", "localhost", 8080));
        assertFalse(InertiaSecurityModes.isSameOriginReferer(null, "http", "localhost", 8080));
        assertFalse(InertiaSecurityModes.isSameOriginReferer("not a uri%%%", "http", "localhost", 8080));
    }

    @Test
    void safeFailureTargetFallsBackOnExternalReferer() {
        assertEquals("http://localhost:8080/dashboard",
            InertiaSecurityModes.safeFailureTarget("http://localhost:8080/dashboard",
                "http", "localhost", 8080, "/"));
        assertEquals("/",
            InertiaSecurityModes.safeFailureTarget("https://evil.example/x",
                "http", "localhost", 8080, "/"));
        assertEquals("/",
            InertiaSecurityModes.safeFailureTarget(null, "http", "localhost", 8080, "/"));
    }

    @Test
    void productionProfileDetection() {
        var prod = new MockEnvironment().withProperty("spring.profiles.active", "prod");
        prod.setActiveProfiles("prod");
        assertTrue(InertiaSecurityModes.isProductionProfile(prod));
        assertFalse(InertiaSecurityModes.isProductionProfile(new MockEnvironment()));
        assertFalse(InertiaSecurityModes.isProductionProfile(null));
    }
}
