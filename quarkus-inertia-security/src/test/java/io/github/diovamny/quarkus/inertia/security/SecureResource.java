package io.github.diovamny.quarkus.inertia.security;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;

import io.github.diovamny.quarkus.inertia.api.Inertia;

@Path("/sec")
public class SecureResource {

    static final AtomicInteger SUBMIT_INVOCATIONS = new AtomicInteger();

    @Inject
    Inertia inertia;

    @GET
    @Path("public")
    @PermitAll
    public Uni<Object> publicPage() {
        return inertia.render("Sec/Public", Map.of());
    }

    @GET
    @Path("secure")
    @Authenticated
    public Uni<Object> securePage() {
        return inertia.render("Sec/Secure", Map.of());
    }

    @GET
    @Path("admin")
    @RolesAllowed("admin")
    public Uni<Object> adminPage() {
        return inertia.render("Sec/Admin", Map.of());
    }

    @POST
    @Path("submit")
    @Authenticated
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Object> submit(Map<String, Object> body) {
        SUBMIT_INVOCATIONS.incrementAndGet();
        return inertia.redirect("/sec/secure");
    }
}
