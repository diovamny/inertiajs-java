package io.github.diovamny.spring.inertia.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CsrfRefreshPolicyUnitTest {

    @ParameterizedTest
    @ValueSource(strings = {"GET", "HEAD", "POST", "PUT", "PATCH", "DELETE"})
    void alwaysPolicyAlwaysEmits(String method) {
        assertThat(InertiaCsrfService.shouldEmitCookie(false, method, "token", "token")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE"})
    void lazyPolicyEmitsOnStateChangingEvenWithValidCookie(String method) {
        assertThat(InertiaCsrfService.shouldEmitCookie(true, method, "token", "token")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "HEAD", "get", "head"})
    void lazyPolicySkipsIdempotentWithValidCookie(String method) {
        assertThat(InertiaCsrfService.shouldEmitCookie(true, method, "token", "token")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "HEAD"})
    void lazyPolicyEmitsIdempotentWithoutCookie(String method) {
        assertThat(InertiaCsrfService.shouldEmitCookie(true, method, null, "token")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "HEAD"})
    void lazyPolicyEmitsIdempotentWithBlankCookie(String method) {
        assertThat(InertiaCsrfService.shouldEmitCookie(true, method, "  ", "token")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "HEAD"})
    void lazyPolicyEmitsIdempotentWithMismatchedCookie(String method) {
        assertThat(InertiaCsrfService.shouldEmitCookie(true, method, "stale", "token")).isTrue();
    }

    @Test
    void lazyPolicyEmitsIdempotentWithoutSessionToken() {
        assertThat(InertiaCsrfService.shouldEmitCookie(true, "GET", "token", null)).isTrue();
    }
}
