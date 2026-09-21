package io.github.diovamny.quarkus.inertia.head;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import io.smallrye.mutiny.Uni;

import io.github.diovamny.quarkus.inertia.api.Inertia;

@Path("/head-test")
public class HeadTestResource {

    @Inject
    Inertia inertia;

    @GET
    public Uni<Object> headed() {
        inertia.head().title("Hi").meta("description", "d").canonical("https://example.com/h");
        return inertia.render("HeadPage", Map.of());
    }
}
