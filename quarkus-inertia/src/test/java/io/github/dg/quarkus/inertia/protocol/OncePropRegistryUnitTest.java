package io.github.dg.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class OncePropRegistryUnitTest {

    private final OncePropRegistry registry = new OncePropRegistry();

    @Test
    void hasNoPropsInitially() {
        assertThat(registry.hasProps()).isFalse();
    }

    @Test
    void drainReturnsValuesAndClears() {
        registry.set("notifications", java.util.List.of(1, 2, 3));
        assertThat(registry.hasProps()).isTrue();

        var drained = registry.drain();
        assertThat(drained).isEqualTo(Map.of("notifications", java.util.List.of(1, 2, 3)));
        assertThat(registry.hasProps()).isFalse();
        assertThat(registry.drain()).isEmpty();
    }

    @Test
    void metadataUsesCustomKey() {
        registry.set("auth_user", Map.of("id", 1), "user", null);

        var metadata = registry.metadata();
        assertThat(metadata).containsOnlyKeys("user");
        assertThat(metadata.get("user").prop()).isEqualTo("auth_user");
        assertThat(metadata.get("user").expiresAt()).isNull();
    }

    @Test
    void metadataExposesEpochMillisWhenExpiring() {
        var future = Instant.now().plusSeconds(3600);
        registry.set("session_info", "x", null, future);

        var metadata = registry.metadata();
        assertThat(metadata.get("session_info").expiresAt()).isEqualTo(future.toEpochMilli());
    }

    @Test
    void drainSkipsKeysPresentOnClientButKeepsMetadata() {
        registry.set("notifications", java.util.List.of(1));
        registry.set("user", "jane");

        var metadataBefore = registry.metadata();
        var drained = registry.drain(Set.of("notifications"));

        assertThat(drained).isEqualTo(Map.of("user", "jane"));
        assertThat(metadataBefore).containsOnlyKeys("notifications", "user");
    }

    @Test
    void expiredPropsArePurged() {
        registry.set("stale", "value", null, Instant.now().minusSeconds(1));
        assertThat(registry.hasProps()).isFalse();
        assertThat(registry.drain()).isEmpty();
        assertThat(registry.metadata()).isEmpty();
    }

    @Test
    void freshFlagCanBeMarked() {
        registry.set("user", "jane");
        assertThat(registry.isFresh("user")).isFalse();
        registry.markFresh("user");
        assertThat(registry.isFresh("user")).isTrue();
    }

    @Test
    void resolvedKeysUsesCustomKeys() {
        registry.set("auth_user", Map.of(), "user", null);
        registry.set("plain", 1);

        assertThat(registry.resolvedKeys()).containsExactlyInAnyOrder("user", "plain");
    }
}
