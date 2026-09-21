package io.github.diovamny.quarkus.inertia.tck;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import io.smallrye.mutiny.Uni;

import io.github.diovamny.quarkus.inertia.api.Inertia;

/**
 * Fixed endpoints backing the executable TCK suite
 * ({@code protocol-v3/*.yaml} in {@code inertia-tck}).
 */
@Path("/tck")
public class TckTestResource {

    @Inject
    Inertia inertia;

    @GET
    @Path("/page")
    public Uni<Object> page() {
        return inertia.render("Tck/Page", Map.of("a", "A", "b", "B"));
    }

    @GET
    @Path("/deferred")
    public Uni<Object> deferred() {
        inertia.deferred("slow", "data", () -> Uni.createFrom().item("lazy-value"));
        return inertia.render("Tck/Deferred", Map.of("title", "t"));
    }

    @GET
    @Path("/once")
    public Uni<Object> once() {
        inertia.once("notice", "Hello");
        return inertia.render("Tck/Once", Map.of());
    }

    @POST
    @Path("/submit")
    public Uni<Object> submit(Map<String, Object> body) {
        var name = body != null ? body.get("name") : null;
        if (name == null || name.toString().isBlank()) {
            return inertia.back().withErrors(Map.of("name", "required"));
        }
        return inertia.redirect("/tck/page");
    }

    @GET
    @Path("/redirect-me")
    public Uni<Object> redirectMe() {
        return inertia.redirect("/tck/page");
    }

    @PUT
    @Path("/put-me")
    public Uni<Object> putMe() {
        return inertia.redirect("/tck/page");
    }

    @DELETE
    @Path("/delete-me")
    public Uni<Object> deleteMe() {
        return inertia.redirect("/tck/page");
    }

    @GET
    @Path("/external")
    public Uni<Object> external() {
        return inertia.location("https://example.com");
    }

    @POST
    @Path("/back")
    public Uni<Object> back() {
        return inertia.back();
    }

    @GET
    @Path("/versioned")
    public Uni<Object> versioned() {
        return inertia.render("Tck/Versioned", Map.of());
    }
}
