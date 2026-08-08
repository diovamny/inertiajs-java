package com.quarkus.inertia.protocol;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.api.Inertia;

@Path("/back-redirect-test")
public class InertiaRedirectTestResource {

    @Inject
    Inertia inertia;

    @GET
    public Uni<Object> page() {
        return inertia.render("Form", java.util.Map.of());
    }

    @POST
    @Path("flash")
    public Uni<Object> flash() {
        return inertia.back().with("success", "Registro actualizado correctamente.");
    }

    @POST
    @Path("errors")
    public Uni<Object> errors() {
        return inertia.back().withErrors(java.util.Map.of("email", "El correo electrónico no es válido."));
    }

    @POST
    @Path("chain")
    public Uni<Object> chain() {
        return inertia.back().with("success", "ok")
            .withErrors(java.util.Map.of("email", "invalid"));
    }

    @POST
    @Path("redirect-flash")
    public Uni<Object> redirectFlash() {
        return inertia.redirect("/back-redirect-test").with("success", "Contact created.");
    }
}
