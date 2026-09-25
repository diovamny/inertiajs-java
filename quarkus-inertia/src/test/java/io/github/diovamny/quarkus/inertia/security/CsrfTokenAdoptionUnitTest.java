package io.github.diovamny.quarkus.inertia.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Single-token rule for framework mode: {@code quarkus-rest-csrf} and the
 * reactive filter share the {@code XSRF-TOKEN} cookie name, so the presented
 * cookie is adopted as the session token instead of letting two independent
 * issuers diverge (which 303-fails the other transport with "page expired").
 */
class CsrfTokenAdoptionUnitTest {

    @Test
    void adoptsPresentedCookieWhenSessionIsEmpty() {
        assertThat(InertiaCsrfService.effectiveToken(null, "rest-token", true))
            .isEqualTo("rest-token");
    }

    @Test
    void adoptsPresentedCookieOverStaleSessionToken() {
        // Browse-reactive (token A), login via JAX-RS (token R), POST reactive:
        // the session must follow the jar, or the reactive POST 303-fails.
        assertThat(InertiaCsrfService.effectiveToken("uuid-A", "rest-token-R", true))
            .isEqualTo("rest-token-R");
    }

    @Test
    void keepsSessionTokenWhenNoCookiePresented() {
        assertThat(InertiaCsrfService.effectiveToken("uuid-A", null, true))
            .isEqualTo("uuid-A");
        assertThat(InertiaCsrfService.effectiveToken("uuid-A", "  ", true))
            .isEqualTo("uuid-A");
    }

    @Test
    void fallsBackToUuidGenerationWhenNeitherSideHasToken() {
        assertThat(InertiaCsrfService.effectiveToken(null, null, true)).isNull();
    }

    @Test
    void adapterModeNeverAdopts() {
        assertThat(InertiaCsrfService.effectiveToken(null, "rest-token", false)).isNull();
        assertThat(InertiaCsrfService.effectiveToken("uuid-A", "rest-token", false))
            .isEqualTo("uuid-A");
    }
}
