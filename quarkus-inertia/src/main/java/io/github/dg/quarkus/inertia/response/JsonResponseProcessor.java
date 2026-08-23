package io.github.dg.quarkus.inertia.response;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;

import io.github.dg.quarkus.inertia.model.PageObject;
import io.github.dg.quarkus.inertia.qute.QuteSerializer;

/**
 * Serializes a page object into the JSON response of an Inertia visit,
 * decorating it with the {@code X-Inertia} headers and the partial-reload
 * component header.
 */
@ApplicationScoped
public class JsonResponseProcessor {

    private final QuteSerializer serializer;

    @Inject
    public JsonResponseProcessor(QuteSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * Serialize a page object to its JSON string (reactive).
     *
     * @param page the page object
     * @return the JSON document as a Uni
     */
    public Uni<String> serialize(PageObject page) {
        return serializer.serialize(page);
    }

    /**
     * Serialize a page object to its JSON string (synchronous).
     *
     * @param page the page object
     * @return the JSON document
     */
    public String serializeSync(PageObject page) {
        return serializer.serialize(page).await().atMost(java.time.Duration.ofSeconds(5));
    }

    /**
     * Build the decorated JSON response for an Inertia visit (reactive).
     *
     * @param page   the page object
     * @param status the HTTP status (200, 303, etc.)
     * @return the HTTP response as a Uni
     */
    public Uni<Response> write(PageObject page, int status) {
        return serialize(page)
            .map(json -> {
                var builder = (status != 200)
                    ? Response.status(status).entity(json).type(MediaType.APPLICATION_JSON)
                    : Response.ok(json, MediaType.APPLICATION_JSON);
                builder.header("X-Inertia", "true")
                    .header("X-Inertia-Component", page.component())
                    .header("Vary", "X-Inertia, X-Inertia-Version, X-Inertia-Partial-Component, X-Inertia-Partial-Data, X-Inertia-Partial-Except");
                var partialComponent = partialComponent();
                if (partialComponent != null) {
                    builder.header("X-Inertia-Partial-Component", partialComponent);
                }
                if (page.version() != null) {
                    builder.header("X-Inertia-Version", page.version());
                }
                return builder.build();
            });
    }

    /**
     * Build the decorated JSON response for an Inertia visit (synchronous).
     *
     * @param page   the page object
     * @param status the HTTP status (200, 303, etc.)
     * @return the HTTP response
     */
    public Response writeSync(PageObject page, int status) {
        String json = serializeSync(page);
        var builder = (status != 200)
            ? Response.status(status).entity(json).type(MediaType.APPLICATION_JSON)
            : Response.ok(json, MediaType.APPLICATION_JSON);
        builder.header("X-Inertia", "true")
            .header("X-Inertia-Component", page.component())
            .header("Vary", "X-Inertia, X-Inertia-Version, X-Inertia-Partial-Component, X-Inertia-Partial-Data, X-Inertia-Partial-Except");
        var partialComponent = partialComponent();
        if (partialComponent != null) {
            builder.header("X-Inertia-Partial-Component", partialComponent);
        }
        if (page.version() != null) {
            builder.header("X-Inertia-Version", page.version());
        }
        return builder.build();
    }

    /**
     * Legacy method without explicit status - defaults to 200.
     */
    public Uni<Response> write(PageObject page) {
        return write(page, 200);
    }

    /**
     * Legacy method without explicit status - defaults to 200.
     */
    public Response writeSync(PageObject page) {
        return writeSync(page, 200);
    }

    private String partialComponent() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var component = ctx.getLocal("inertia-partial-component");
            if (component != null) return (String) component;
        }
        return null;
    }
}
