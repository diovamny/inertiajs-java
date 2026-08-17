package io.github.dg.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class InertiaResponseFilterUnitTest {

    @Test
    void shouldConvert302To303ForPut() {
        assertThat(InertiaResponseFilter.normalizeRedirectStatus("PUT", 302)).isEqualTo(303);
    }

    @Test
    void shouldConvert302To303ForPatch() {
        assertThat(InertiaResponseFilter.normalizeRedirectStatus("PATCH", 302)).isEqualTo(303);
    }

    @Test
    void shouldConvert302To303ForDelete() {
        assertThat(InertiaResponseFilter.normalizeRedirectStatus("DELETE", 302)).isEqualTo(303);
    }

    @Test
    void shouldKeep303ForPut() {
        assertThat(InertiaResponseFilter.normalizeRedirectStatus("PUT", 303)).isEqualTo(303);
    }

    @Test
    void shouldKeep302ForGet() {
        assertThat(InertiaResponseFilter.normalizeRedirectStatus("GET", 302)).isEqualTo(302);
    }

    @Test
    void shouldKeep302ForPost() {
        assertThat(InertiaResponseFilter.normalizeRedirectStatus("POST", 302)).isEqualTo(302);
    }

    @Test
    void shouldKeep307ForPut() {
        assertThat(InertiaResponseFilter.normalizeRedirectStatus("PUT", 307)).isEqualTo(307);
    }

    @Test
    void shouldKeep301ForPut() {
        assertThat(InertiaResponseFilter.normalizeRedirectStatus("PUT", 301)).isEqualTo(301);
    }
}
