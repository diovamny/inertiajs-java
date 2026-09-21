package io.github.diovamny.spring.inertia.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.diovamny.spring.inertia.config.InertiaConfigValidator;
import io.github.diovamny.spring.inertia.config.InertiaProperties;

class InertiaSecurityGuardrailsTest {

    @Test
    void frameworkModeWithoutIntegrationFailsFast() {
        var properties = new InertiaProperties();
        properties.getSecurity().setMode("framework");
        // spring-inertia-security is not on this module's test classpath.
        assertThrows(IllegalStateException.class,
            () -> new InertiaConfigValidator(properties, new MockEnvironment()));
    }

    @Test
    void failOnFallbackRefusesAdapterResolution() {
        var properties = new InertiaProperties();
        properties.getSecurity().setFailOnFallback(true);
        assertThrows(IllegalStateException.class,
            () -> new InertiaConfigValidator(properties, new MockEnvironment()));
    }

    @Test
    void disabledModeRefusedInProductionWithoutConfirmation() {
        var properties = new InertiaProperties();
        properties.getSecurity().setMode("disabled");
        var prod = new MockEnvironment();
        prod.setActiveProfiles("prod");
        assertThrows(IllegalStateException.class,
            () -> new InertiaConfigValidator(properties, prod));
    }

    @Test
    void disabledModeAllowedInProductionWithConfirmation() {
        var properties = new InertiaProperties();
        properties.getSecurity().setMode("disabled");
        properties.getSecurity().setAllowDisabledInProduction(true);
        var prod = new MockEnvironment();
        prod.setActiveProfiles("prod");
        assertDoesNotThrow(() -> new InertiaConfigValidator(properties, prod));
    }

    @Test
    void invalidCookieSameSiteFailsFast() {
        var properties = new InertiaProperties();
        properties.getSecurity().setCookieSameSite("Sometimes");
        assertThrows(IllegalArgumentException.class,
            () -> new InertiaConfigValidator(properties, new MockEnvironment()));
    }

    @Test
    void relativeLoginUrlFailsFast() {
        var properties = new InertiaProperties();
        properties.getSecurity().setLoginUrl("login");
        assertThrows(IllegalArgumentException.class,
            () -> new InertiaConfigValidator(properties, new MockEnvironment()));
    }
}
