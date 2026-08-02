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
        registry.addRescuedProp("failedKey", "error message");
        assertThat(registry.getRescuedProps()).containsEntry("failedKey", "error message");
    }

    @Test
    void shouldAddMultipleRescuedProps() {
        var registry = new SharedDataRegistry();
        registry.addRescuedProps(java.util.Map.of("a", "err1", "b", "err2"));
        assertThat(registry.hasRescuedProps()).isTrue();
        assertThat(registry.getRescuedProps()).hasSize(2);
    }

    @Test
    void shouldClearPrependAndRescuedProps() {
        var registry = new SharedDataRegistry();
        registry.addPrependPropKey("items");
        registry.addRescuedProp("key", "err");
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
}
