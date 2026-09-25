package io.github.diovamny.spring.inertia.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

import io.github.diovamny.inertia.core.model.ValidationErrors;
import io.github.diovamny.inertia.core.spi.FlashStore;

class ValidationErrorsApiUnitTest {

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
    void redirectKeepsLegacySingleMessageShape() {
        var flash = new MapFlashStore();
        var redirect = new InertiaRedirect(HttpStatusCode.valueOf(303), new HttpHeaders(), null, flash);
        redirect.withErrors(Map.of("email", "required"));
        assertThat(flash.data).containsEntry("errors", Map.of("email", "required"));
    }

    @Test
    void redirectEmitsOrderedArraysViaNewApis() {
        var flash = new MapFlashStore();
        var redirect = new InertiaRedirect(HttpStatusCode.valueOf(303), new HttpHeaders(), null, flash);
        redirect.withValidationErrors(
            ValidationErrors.builder().add("email", "required").add("email", "must be valid").build());
        assertThat(flash.data).containsEntry("errors", Map.of("email", List.of("required", "must be valid")));

        var flash2 = new MapFlashStore();
        var redirect2 = new InertiaRedirect(HttpStatusCode.valueOf(303), new HttpHeaders(), null, flash2);
        redirect2.withErrorMessages(Map.of("name", List.of("a", "b")));
        assertThat(flash2.data).containsEntry("errors", Map.of("name", List.of("a", "b")));
    }

    @Test
    void renderAndResponseEmitOrderedArraysViaNewApis() {
        var flash = new MapFlashStore();
        var render = new InertiaRender(HttpStatusCode.valueOf(200), new HttpHeaders(), "{}", flash);
        render.withValidationErrors(ValidationErrors.single("email", "required"));
        assertThat(flash.data).containsEntry("errors", Map.of("email", List.of("required")));

        var flash2 = new MapFlashStore();
        var response = new InertiaResponse(HttpStatusCode.valueOf(200), new HttpHeaders(), "{}", flash2);
        response.withErrorMessages(Map.of("email", List.of("required", "must be valid")));
        assertThat(flash2.data)
            .containsEntry("errors", Map.of("email", List.of("required", "must be valid")));
    }
}
