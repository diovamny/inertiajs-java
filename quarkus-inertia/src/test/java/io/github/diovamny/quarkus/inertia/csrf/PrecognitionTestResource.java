package io.github.diovamny.quarkus.inertia.csrf;

import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import io.smallrye.mutiny.Uni;

import io.github.diovamny.quarkus.inertia.api.Inertia;

@Path("/precog-test")
public class PrecognitionTestResource {

    @Inject
    Inertia inertia;

    @GET
    public Uni<Object> get(@QueryParam("withErrors") boolean withErrors) {
        if (withErrors) {
            return inertia.render("Home", Map.of("errors", Map.of("name", "required")));
        }
        return inertia.render("Home", Map.of());
    }
}
