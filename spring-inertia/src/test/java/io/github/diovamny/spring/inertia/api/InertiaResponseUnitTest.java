package io.github.diovamny.spring.inertia.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

import io.github.diovamny.inertia.core.spi.FlashStore;

class InertiaResponseUnitTest {

    private static class MapFlashStore implements FlashStore {
        final Map<String, Object> data = new LinkedHashMap<>();

        @Override
        public void put(String key, Object value) {
            data.put(key, value);
        }

        @Override
        public void putAll(Map<String, Object> values) {
            data.putAll(values);
        }

        @Override
        public Object get(String key, Object defaultValue) {
            return data.getOrDefault(key, defaultValue);
        }

        @Override
        public Object pull(String key, Object defaultValue) {
            return data.containsKey(key) ? data.remove(key) : defaultValue;
        }

        @Override
        public Map<String, Object> drain() {
            var drained = new LinkedHashMap<>(data);
            data.clear();
            return drained;
        }

        @Override
        public boolean hasData() {
            return !data.isEmpty();
        }
    }

    @Test
    void flashWithStorePutsAndReturnsSameInstance() {
        var flashStore = new MapFlashStore();
        var response = new InertiaResponse(HttpStatusCode.valueOf(200), new HttpHeaders(), "{}",
            flashStore);
        assertThat(response.flash("success", "OK")).isSameAs(response);
        assertThat(response.flash(Map.of("a", "b"))).isSameAs(response);
        assertThat(response.withErrors(Map.of("email", "required"))).isSameAs(response);
        assertThat(flashStore.data)
            .containsEntry("success", "OK")
            .containsEntry("a", "b")
            .containsEntry("errors", Map.of("email", "required"));
    }

    @Test
    void flashWithoutStoreFailsFast() {
        var response = new InertiaResponse(HttpStatusCode.valueOf(200), new HttpHeaders(), "{}");
        assertThatThrownBy(() -> response.flash("success", "OK"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("FlashStore");
    }
}
