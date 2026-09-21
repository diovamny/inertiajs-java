package io.github.diovamny.spring.inertia.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

import io.github.diovamny.inertia.core.spi.FlashStore;

class InertiaRenderUnitTest {

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

    private InertiaRender render(MapFlashStore flashStore) {
        return new InertiaRender(
            new InertiaResponse(HttpStatusCode.valueOf(200), new HttpHeaders(), "{}"),
            flashStore);
    }

    @Test
    void flashStoresEntriesAndReturnsSameInstance() {
        var flashStore = new MapFlashStore();
        var result = render(flashStore);
        assertThat(result.flash("success", "Guardado!")).isSameAs(result);
        assertThat(result.flash(Map.of("a", "b"))).isSameAs(result);
        assertThat(flashStore.data)
            .containsEntry("success", "Guardado!")
            .containsEntry("a", "b");
    }

    @Test
    void withAliasesStoreEntries() {
        var flashStore = new MapFlashStore();
        var result = render(flashStore);
        assertThat(result.with("success", "OK")).isSameAs(result);
        assertThat(result.with(Map.of("a", "b"))).isSameAs(result);
        assertThat(result.withErrors(Map.of("email", "required"))).isSameAs(result);
        assertThat(flashStore.data)
            .containsEntry("success", "OK")
            .containsEntry("a", "b")
            .containsEntry("errors", Map.of("email", "required"));
    }

    @Test
    void stillServesAsResponseEntity() {
        var result = render(new MapFlashStore());
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo("{}");
    }

    @Test
    void enableSsrCacheSetsPolicyTtl() {
        var policy = new io.github.diovamny.spring.inertia.renderer.SsrCachePolicy();
        var result = new InertiaRender(
            new InertiaResponse(HttpStatusCode.valueOf(200), new HttpHeaders(), "{}"),
            new MapFlashStore(), policy);
        assertThat(result.enableSsrCache(java.time.Duration.ofMinutes(15))).isSameAs(result);
        assertThat(policy.ttlMillis()).isEqualTo(15 * 60 * 1000L);
    }
}
