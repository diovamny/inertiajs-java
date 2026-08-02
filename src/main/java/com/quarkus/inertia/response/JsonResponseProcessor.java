package com.quarkus.inertia.response;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.qute.QuteSerializer;

@RequestScoped
public class JsonResponseProcessor {

    private final QuteSerializer serializer;

    @Inject
    public JsonResponseProcessor(QuteSerializer serializer) {
        this.serializer = serializer;
    }

    public Uni<Response> write(PageObject page) {
        return serializer.serialize(page)
            .map(json -> Response.ok(json, MediaType.APPLICATION_JSON)
                .header("X-Inertia", "true")
                .header("X-Inertia-Component", page.component())
                .header("Vary", "X-Inertia")
                .build());
    }
}
