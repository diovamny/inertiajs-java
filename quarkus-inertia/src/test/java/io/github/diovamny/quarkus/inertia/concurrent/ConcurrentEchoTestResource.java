package io.github.diovamny.quarkus.inertia.concurrent;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import io.smallrye.mutiny.Uni;

import io.github.diovamny.quarkus.inertia.api.Inertia;

@Path("/concurrent-echo")
public class ConcurrentEchoTestResource {

    @Inject
    Inertia inertia;

    @GET
    public Uni<Object> echo(@HeaderParam("X-Echo") String echo) {
        var tag = echo != null ? echo : "";
        return inertia.render("Echo", Map.of("a", "A-" + tag, "b", "B-" + tag));
    }
}
