package io.github.diovamny.quarkus.inertia.api;

import java.util.Map;
import jakarta.ws.rs.core.Response;
import io.github.diovamny.inertia.core.model.PageObject;
import io.github.diovamny.inertia.core.spi.FlashStore;

/**
 * A wrapper for an Inertia HTTP response, providing type-safe access to the
 * underlying JAX-RS {@link Response} and the rendered {@link PageObject}.
 */
public class InertiaResponse {

    private final Response response;
    private final PageObject page;
    private final FlashStore flashStore;

    public InertiaResponse(Response response, PageObject page) {
        this(response, page, null);
    }

    public InertiaResponse(Response response) {
        this(response, null, null);
    }

    public InertiaResponse(Response response, PageObject page, FlashStore flashStore) {
        this.response = response;
        this.page = page;
        this.flashStore = flashStore;
    }

    /**
     * Returns the underlying JAX-RS response.
     */
    public Response toResponse() {
        return response;
    }

    /**
     * Returns the page object that was rendered, or {@code null} if not available.
     */
    public PageObject getPage() {
        return page;
    }

    /**
     * Implicitly converts to {@link Response} for JAX-RS return types.
     */
    public Response unwrap() {
        return response;
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
