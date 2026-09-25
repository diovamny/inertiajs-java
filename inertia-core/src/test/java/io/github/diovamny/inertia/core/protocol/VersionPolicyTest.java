package io.github.diovamny.inertia.core.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VersionPolicyTest {

    @Test
    void staleGetMismatch() {
        assertThat(VersionPolicy.isStale("GET", "old", "new", false)).isTrue();
    }

    @Test
    void matchingVersionsAreFresh() {
        assertThat(VersionPolicy.isStale("GET", "abc", "abc", false)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"})
    void nonGetNeverStale(String method) {
        assertThat(VersionPolicy.isStale(method, "old", "new", false)).isFalse();
    }

    @Test
    void methodIsCaseInsensitive() {
        assertThat(VersionPolicy.isStale("get", "old", "new", false)).isTrue();
    }

    @Test
    void prefetchNeverStale() {
        assertThat(VersionPolicy.isStale("GET", "old", "new", true)).isFalse();
    }

    @Test
    void missingVersionsAreUntrackedNeverStale() {
        assertThat(VersionPolicy.isStale("GET", null, "new", false)).isFalse();
        assertThat(VersionPolicy.isStale("GET", "old", null, false)).isFalse();
        assertThat(VersionPolicy.isStale("GET", null, null, false)).isFalse();
        assertThat(VersionPolicy.isStale(null, "old", "new", false)).isFalse();
    }

    @Test
    void mismatchCarriesLocationAndVersion() {
        var mismatch = VersionPolicy.mismatch("/events", "v2");
        assertThat(mismatch.location()).isEqualTo("/events");
        assertThat(mismatch.serverVersion()).isEqualTo("v2");
    }
}
