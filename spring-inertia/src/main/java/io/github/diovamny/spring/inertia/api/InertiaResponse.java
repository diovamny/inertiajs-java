package io.github.diovamny.spring.inertia.api;

import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import io.github.diovamny.inertia.core.spi.FlashStore;

/**
 * An Inertia JSON response. Extends {@link ResponseEntity} so Spring MVC
 * serves it natively through {@code HttpEntityMethodProcessor} without
 * custom converters.
 */
public class InertiaResponse extends ResponseEntity<String> {

    private final FlashStore flashStore;

    /**
     * @param status  the HTTP status
     * @param headers the response headers
     * @param body    the page JSON
     */
    public InertiaResponse(HttpStatusCode status, HttpHeaders headers, String body) {
        this(status, headers, body, null);
    }

    /**
     * @param status     the HTTP status
     * @param headers    the response headers
     * @param body       the page JSON
     * @param flashStore backing store for {@link #flash}, may be {@code null}
     */
    public InertiaResponse(HttpStatusCode status, HttpHeaders headers, String body,
            FlashStore flashStore) {
        super(body, headers, status);
        this.flashStore = flashStore;
    }

    /**
     * Factory for a 200 Inertia JSON response.
     *
     * @param body    the page JSON
     * @param headers the response headers
     * @return the response
     */
    public static InertiaResponse ok(String body, HttpHeaders headers) {
        return new InertiaResponse(HttpStatusCode.valueOf(200), headers, body);
    }

    /**
     * Flash a single key/value pair for the next rendered page.
     *
     * @throws IllegalStateException when built without a {@link FlashStore}
     */
    public InertiaResponse flash(String key, Object value) {
        requireFlashStore().put(key, value);
        return this;
    }

    /**
     * Flash several key/value pairs for the next rendered page.
     *
     * @throws IllegalStateException when built without a {@link FlashStore}
     */
    public InertiaResponse flash(Map<String, Object> values) {
        requireFlashStore().putAll(values);
        return this;
    }

    /**
     * Flash validation errors under the {@code errors} key.
     *
     * @throws IllegalStateException when built without a {@link FlashStore}
     */
    public InertiaResponse withErrors(Map<String, String> errors) {
        return flash("errors", errors);
    }

    private FlashStore requireFlashStore() {
        if (flashStore == null) {
            throw new IllegalStateException(
                "InertiaResponse.flash(...) requires a FlashStore: "
                    + "use the constructor accepting one, or call inertia.flash(...) directly");
        }
        return flashStore;
    }
}
