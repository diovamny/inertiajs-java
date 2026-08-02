package com.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SharedDataRegistryUnitTest {

    @Test
    void shouldStoreAndRetrieveValue() {
        var registry = new SharedDataRegistry();
        registry.set("key", "value");
        assertThat(registry.get("key")).isEqualTo("value");
    }

    @Test
    void shouldStoreMultipleValues() {
        var registry = new SharedDataRegistry();
        registry.set("a", 1);
        registry.set("b", 2);
        assertThat(registry.getAll()).hasSize(2);
    }

    @Test
    void shouldSetAllValues() {
        var registry = new SharedDataRegistry();
        registry.setAll(java.util.Map.of("x", "10", "y", "20"));
        assertThat(registry.get("x")).isEqualTo("10");
        assertThat(registry.get("y")).isEqualTo("20");
    }

    @Test
    void shouldReturnImmutableSnapshot() {
        var registry = new SharedDataRegistry();
        registry.set("key", "value");
        assertThatThrownBy(() -> registry.getAll().put("new", "value"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldBeEmptyInitially() {
        var registry = new SharedDataRegistry();
        assertThat(registry.isEmpty()).isTrue();
    }

    @Test
    void shouldClearData() {
        var registry = new SharedDataRegistry();
        registry.set("key", "value");
        registry.clear();
        assertThat(registry.isEmpty()).isTrue();
    }

    @Test
    void shouldReturnNullForMissingKey() {
        var registry = new SharedDataRegistry();
        assertThat(registry.get("nonexistent")).isNull();
    }

    @Test
    void shouldStoreAndDrainFlash() {
        var registry = new SharedDataRegistry();
        registry.setFlash("flash-key", "flash-value");
        assertThat(registry.hasFlash()).isTrue();
        var drained = registry.drainFlash();
        assertThat(drained).containsEntry("flash-key", "flash-value");
        assertThat(registry.hasFlash()).isFalse();
    }

    @Test
    void getAllShouldIncludeFlash() {
        var registry = new SharedDataRegistry();
        registry.set("normal", "value");
        registry.setFlash("flash", "data");
        assertThat(registry.getAll()).containsEntry("flash", "data");
    }

    @Test
    void shouldStorePrependPropKeys() {
        var registry = new SharedDataRegistry();
        registry.addPrependPropKey("items");
        registry.addPrependPropKey("more");
        assertThat(registry.getPrependPropKeys()).containsExactly("items", "more");
    }

    @Test
    void shouldReturnImmutablePrependPropKeys() {
        var registry = new SharedDataRegistry();
        registry.addPrependPropKey("key");
        assertThatThrownBy(() -> registry.getPrependPropKeys().add("new"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldStoreAndRetrieveRescuedProps() {
        var registry = new SharedDataRegistry();
        registry.addRescuedProp("failedKey");
        assertThat(registry.getRescuedProps()).containsExactly("failedKey");
    }

    @Test
    void shouldAddMultipleRescuedProps() {
        var registry = new SharedDataRegistry();
        registry.addRescuedProps(java.util.List.of("a", "b"));
        assertThat(registry.hasRescuedProps()).isTrue();
        assertThat(registry.getRescuedProps()).hasSize(2);
    }

    @Test
    void shouldClearPrependAndRescuedProps() {
        var registry = new SharedDataRegistry();
        registry.addPrependPropKey("items");
        registry.addRescuedProp("key");
        registry.clear();
        assertThat(registry.getPrependPropKeys()).isEmpty();
        assertThat(registry.hasRescuedProps()).isFalse();
    }

    @Test
    void shouldIncludePrependPropsInClear() {
        var registry = new SharedDataRegistry();
        registry.addPrependPropKey("p");
        registry.clear();
        assertThat(registry.getPrependPropKeys()).isEmpty();
    }

    @Test
    void shouldStoreOptionalProps() {
        var registry = new SharedDataRegistry();
        registry.addOptionalProp("key", () -> io.smallrye.mutiny.Uni.createFrom().item("value"));
        assertThat(registry.hasOptionalProps()).isTrue();
        assertThat(registry.getOptionalProps()).containsOnlyKeys("key");
    }

    @Test
    void shouldClearOptionalProps() {
        var registry = new SharedDataRegistry();
        registry.addOptionalProp("key", () -> io.smallrye.mutiny.Uni.createFrom().item("value"));
        registry.clear();
        assertThat(registry.hasOptionalProps()).isFalse();
    }

    @Test
    void getSharedShouldReturnOnlyTrackedKeys() {
        var registry = new SharedDataRegistry();
        registry.set("shared", 1);
        registry.setWithNoTrack("untracked", 2);
        assertThat(registry.getShared()).containsOnlyKeys("shared");
    }

    @Test
    void flushSharedShouldRemoveTrackedKeysOnly() {
        var registry = new SharedDataRegistry();
        registry.set("shared", 1);
        registry.setWithNoTrack("untracked", 2);
        registry.flushShared();
        assertThat(registry.getAll()).containsOnlyKeys("untracked");
    }

    @Test
    void getSharedWithDefaultReturnsValueForTrackedKey() {
        var registry = new SharedDataRegistry();
        registry.set("user", "jane");
        assertThat(registry.getShared("user", "fallback")).isEqualTo("jane");
    }

    @Test
    void getSharedWithDefaultReturnsDefaultForMissingKey() {
        var registry = new SharedDataRegistry();
        assertThat(registry.getShared("missing", "fallback")).isEqualTo("fallback");
    }

    @Test
    void getSharedWithDefaultReturnsDefaultForUntrackedKey() {
        var registry = new SharedDataRegistry();
        registry.setWithNoTrack("untracked", "value");
        assertThat(registry.getShared("untracked", "fallback")).isEqualTo("fallback");
    }

    @Test
    void mergeRegistersKeyValueAndMatchOn() {
        var registry = new SharedDataRegistry();
        registry.merge("items", java.util.List.of(1), false, "id");
        assertThat(registry.getMergePropKeys()).containsExactly("items");
        assertThat(registry.getMatchPropKeys()).containsExactly("items.id");
        assertThat(registry.get("items")).isEqualTo(java.util.List.of(1));
    }

    @Test
    void mergeWithDeepRegistersDeepMergeKey() {
        var registry = new SharedDataRegistry();
        registry.merge("profile", java.util.Map.of("name", "jane"), true, "id");
        assertThat(registry.getDeepMergePropKeys()).containsExactly("profile");
        assertThat(registry.getMatchPropKeys()).containsExactly("profile.id");
    }

    @Test
    void mergeWithoutMatchOnLeavesMatchPropsEmpty() {
        var registry = new SharedDataRegistry();
        registry.merge("items", java.util.List.of(1), false);
        assertThat(registry.getMatchPropKeys()).isEmpty();
        assertThat(registry.getMergePropKeys()).containsExactly("items");
    }

    @Test
    void mergeWithMultipleMatchOnFields() {
        var registry = new SharedDataRegistry();
        registry.merge("items", java.util.List.of(1), false, "id", "sku");
        assertThat(registry.getMatchPropKeys()).containsExactly("items.id", "items.sku");
    }
}
