package io.github.diovamny.inertia.tck;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import io.github.diovamny.inertia.core.model.PageObject;
import io.github.diovamny.inertia.core.protocol.VersionPolicy;

/**
 * {@code PROTO-Version-History-01}: the asset version travels identically in
 * the JSON response and in the HTML {@code data-page} bootstrap (both
 * transports serialize the same page object), and a stale {@code GET} yields
 * the {@code 409} control payload that drives the full reload behind the
 * v3.8.0 history-restoration fix.
 */
class VersionPolicyTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void protoVersionHistory01VersionTravelsInJsonAndBootstrap() throws Exception {
        var page = new PageObject("Events/Index", Map.of("total", 3),
            "/events?page=1", "v3.8.0-assets");

        var json = JSON.writeValueAsString(page);
        var bootstrap = "<div id=\"app\" data-page='" + json + "'></div>";

        assertThat(JSON.readTree(json).path("version").asText())
            .isEqualTo("v3.8.0-assets");
        assertThat(JSON.readTree(json).path("url").asText())
            .isEqualTo("/events?page=1");
        assertThat(bootstrap).contains("v3.8.0-assets").contains("/events?page=1");
    }

    @Test
    void protoVersionHistory01StaleGetYields409Payload() {
        assertThat(VersionPolicy.isStale("GET", "old-assets", "v3.8.0-assets", false))
            .isTrue();

        var mismatch = VersionPolicy.mismatch("/events", "v3.8.0-assets");
        assertThat(mismatch.location()).isEqualTo("/events");
        assertThat(mismatch.serverVersion()).isEqualTo("v3.8.0-assets");
    }

    @Test
    void protoVersionHistory01MatchingOrUntrackedVersionsNeverStale() {
        assertThat(VersionPolicy.isStale("GET", "same", "same", false)).isFalse();
        assertThat(VersionPolicy.isStale("GET", null, "server", false)).isFalse();
        assertThat(VersionPolicy.isStale("GET", "client", null, false)).isFalse();
        assertThat(VersionPolicy.isStale("GET", "old", "new", true)).isFalse();
        assertThat(VersionPolicy.isStale("POST", "old", "new", false)).isFalse();
    }
}
