package com.quarkus.inertia.response;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.qute.QuteSerializer;

/**
 * Serializes a page object into the JSON response of an Inertia visit,
 * decorating it with the {@code X-Inertia} headers and the partial-reload
 * component header.
 */
@RequestScoped
public class JsonResponseProcessor {

    private final QuteSerializer serializer;

    @Inject
    public JsonResponseProcessor(QuteSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * Serialize a page object to its JSON string.
     *
     * @param page the page object
     * @return the JSON document as a Uni
     */
    public Uni<String> serialize(PageObject page) {
        return serializer.serialize(page);
    }

    /**
     * Build the decorated JSON response for an Inertia visit.
     *
     * @param page the page object
     * @return the HTTP response as a Uni
     */
    public Uni<Response> write(PageObject page) {
        return serialize(page)
            .map(json -> {
                var builder = Response.ok(json, MediaType.APPLICATION_JSON)
                    .header("X-Inertia", "true")
                    .header("X-Inertia-Component", page.component())
                    .header("Vary", "X-Inertia");
                var partialComponent = partialComponent();
                if (partialComponent != null) {
                    builder.header("X-Inertia-Partial-Component", partialComponent);
                }
                return builder.build();
            });
    }

    private String partialComponent() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            var component = ctx.getLocal("inertia-partial-component");
            if (component != null) return (String) component;
        }
        return null;
    }
}
