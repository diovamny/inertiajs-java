package io.github.dg.quarkus.inertia.api;

import jakarta.ws.rs.core.Response;
import io.github.dg.quarkus.inertia.model.PageObject;

/**
 * A wrapper for an Inertia HTTP response, providing type-safe access to the
 * underlying JAX-RS {@link Response} and the rendered {@link PageObject}.
 */
public class InertiaResponse {

    private final Response response;
    private final PageObject page;

    public InertiaResponse(Response response, PageObject page) {
        this.response = response;
        this.page = page;
    }

    public InertiaResponse(Response response) {
        this(response, null);
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
}