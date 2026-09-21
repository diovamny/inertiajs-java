package io.github.diovamny.inertia.core.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class VaryHeadersTest {

    @Test
    void mergesWithEmptyExisting() {
        assertThat(VaryHeaders.merge(null, "X-Inertia")).isEqualTo("X-Inertia");
        assertThat(VaryHeaders.merge("", "X-Inertia")).isEqualTo("X-Inertia");
    }

    @Test
    void preservesExistingTokens() {
        assertThat(VaryHeaders.merge("Accept-Encoding", "X-Inertia"))
            .isEqualTo("Accept-Encoding, X-Inertia");
    }

    @Test
    void deduplicatesCaseInsensitively() {
        assertThat(VaryHeaders.merge("x-inertia", "X-Inertia")).isEqualTo("x-inertia");
    }

    @Test
    void mergesMultipleTokensPreservingOrder() {
        assertThat(VaryHeaders.merge("Accept-Encoding", "X-Inertia", "Precognition"))
            .isEqualTo("Accept-Encoding, X-Inertia, Precognition");
    }

    @Test
    void ignoresBlankTokens() {
        assertThat(VaryHeaders.merge("Accept-Encoding", null, "  ", "X-Inertia"))
            .isEqualTo("Accept-Encoding, X-Inertia");
    }

    @Test
    void mergeAllJoinsValueLists() {
        assertThat(VaryHeaders.mergeAll(List.of("Accept-Encoding", "User-Agent"), "X-Inertia"))
            .isEqualTo("Accept-Encoding, User-Agent, X-Inertia");
        assertThat(VaryHeaders.mergeAll(null, "X-Inertia")).isEqualTo("X-Inertia");
        assertThat(VaryHeaders.mergeAll(List.of(), "X-Inertia")).isEqualTo("X-Inertia");
    }
}
