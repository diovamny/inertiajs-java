package io.github.diovamny.quarkus.inertia.render;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import io.smallrye.mutiny.Uni;

import io.github.diovamny.quarkus.inertia.api.Inertia;

@Path("/render-flash-test")
public class RenderFlashTestResource {

    @Inject
    Inertia inertia;

    @GET
    @Path("/render")
    public Uni<Object> render() {
        return inertia.render("FlashPage", Map.of()).flash("message", "Chained");
    }

    @GET
    @Path("/show")
    public Uni<Object> show() {
        return inertia.render("FlashPage", Map.of());
    }
}
