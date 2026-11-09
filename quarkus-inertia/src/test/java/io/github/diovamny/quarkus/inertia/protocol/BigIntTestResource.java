package io.github.diovamny.quarkus.inertia.protocol;

import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import io.smallrye.mutiny.Uni;

import io.github.diovamny.quarkus.inertia.api.Inertia;

@Path("/bigint-test")
public class BigIntTestResource {

    @Inject
    Inertia inertia;

    @GET
    public Uni<Object> page() {
        return inertia.render("BigIntPage", Map.of("id", Long.MAX_VALUE, "safe", 42L));
    }

    @GET
    @Path("opt-in")
    public Uni<Object> optIn() {
        inertia.preserveBigIntegers(true);
        return inertia.render("BigIntPage", Map.of("id", Long.MAX_VALUE, "safe", 42L));
    }

    @GET
    @Path("opt-out")
    public Uni<Object> optOut() {
        inertia.preserveBigIntegers(false);
        return inertia.render("BigIntPage", Map.of("id", Long.MAX_VALUE, "safe", 42L));
    }
}
