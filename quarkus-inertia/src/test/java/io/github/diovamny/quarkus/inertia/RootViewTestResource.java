package io.github.diovamny.quarkus.inertia;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import io.smallrye.mutiny.Uni;

import io.github.diovamny.quarkus.inertia.api.Inertia;

@Path("/root-view")
public class RootViewTestResource {

    @Inject
    Inertia inertia;

    @GET
    public Uni<Object> overridden() {
        inertia.setRootView("alt");
        return inertia.render("Home", java.util.Map.of());
    }

    @Path("/default")
    @GET
    public Uni<Object> defaulted() {
        return inertia.render("Home", java.util.Map.of());
    }
}
