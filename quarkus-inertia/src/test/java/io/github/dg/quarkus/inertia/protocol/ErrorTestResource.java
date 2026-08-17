package io.github.dg.quarkus.inertia.protocol;

import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import io.smallrye.mutiny.Uni;

import io.github.dg.quarkus.inertia.api.Inertia;

@Path("/error-test")
public class ErrorTestResource {

    @Inject
    Inertia inertia;

    @GET
    @Path("/render-403")
    public Uni<Object> render403() {
        return inertia.render("ErrorPage", Map.of("status", 403, "message", "Forbidden"), 403);
    }

    @GET
    @Path("/render-404")
    public Uni<Object> render404() {
        return inertia.render("ErrorPage", Map.of("status", 404, "message", "Not Found"), 404);
    }

    @GET
    @Path("/render-200")
    public Uni<Object> render200() {
        return inertia.render("ErrorPage", Map.of("status", 200), 200);
    }

    @GET
    @Path("/throw-403")
    public Uni<Object> throw403() {
        throw new ForbiddenException("Forbidden");
    }

    @GET
    @Path("/throw-500")
    public Uni<Object> throw500() {
        throw new RuntimeException("boom");
    }

    @GET
    @Path("/throw-400")
    public Uni<Object> throw400() {
        throw new IllegalArgumentException("invalid input");
    }

    @GET
    @Path("/throw-409")
    public Uni<Object> throw409() {
        throw new IllegalStateException("invalid state");
    }

    @GET
    @Path("/throw-422")
    public Uni<Object> throw422() {
        throw new jakarta.validation.ValidationException("business rule");
    }

    @GET
    @Path("/throw-security")
    public Uni<Object> throwSecurity() {
        throw new SecurityException("denied");
    }
}
