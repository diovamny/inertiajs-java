package io.github.diovamny.spring.inertia.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.diovamny.inertia.core.spi.FlashStore;
import io.github.diovamny.spring.inertia.api.InertiaRedirect;

class ErrorBagsTest {

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

    @AfterEach
    void resetRequestAttributes() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void wrapWithoutRequestPassesErrorsThrough() {
        var errors = Map.of("email", "required");
        assertThat(ErrorBags.wrap(errors)).isSameAs(errors);
    }

    @Test
    void wrapWithExplicitBagName() {
        var errors = Map.of("title", "required");
        assertThat(ErrorBags.wrap(errors, "probeSecondary"))
            .isEqualTo(Map.of("probeSecondary", Map.of("title", "required")));
    }

    @Test
    void wrapWithBlankOrNullBagPassesThrough() {
        var errors = Map.of("title", "required");
        assertThat(ErrorBags.wrap(errors, null)).isSameAs(errors);
        assertThat(ErrorBags.wrap(errors, "  ")).isSameAs(errors);
        assertThat(ErrorBags.wrap(null, "bag")).isNull();
    }

    @Test
    void wrapReadsBagFromRequestContext() {
        var request = new MockHttpServletRequest();
        request.setAttribute("inertia-error-bag", "probeSecondary");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        var errors = Map.of("title", "required");
        assertThat(ErrorBags.wrap(errors))
            .isEqualTo(Map.of("probeSecondary", Map.of("title", "required")));
    }

    @Test
    void redirectWithErrorsHonorsRequestErrorBag() {
        var request = new MockHttpServletRequest();
        request.setAttribute("inertia-error-bag", "probeSecondary");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        var flashStore = new MapFlashStore();
        var redirect = new InertiaRedirect(
            HttpStatusCode.valueOf(303), new HttpHeaders(), null, flashStore);
        redirect.withErrors(Map.of("title", "required"));

        assertThat(flashStore.data)
            .containsEntry("errors", Map.of("probeSecondary", Map.of("title", "required")));
    }

    @Test
    void redirectWithErrorsStaysFlatWithoutBag() {
        var flashStore = new MapFlashStore();
        var redirect = new InertiaRedirect(
            HttpStatusCode.valueOf(303), new HttpHeaders(), null, flashStore);
        redirect.withErrors(Map.of("title", "required"));

        assertThat(flashStore.data)
            .containsEntry("errors", Map.of("title", "required"));
    }
}
