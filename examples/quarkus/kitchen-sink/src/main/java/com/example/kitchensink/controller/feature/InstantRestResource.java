package com.example.kitchensink.controller.feature;

import java.util.ArrayList;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import com.example.kitchensink.service.Demo;
import io.github.diovamny.quarkus.inertia.api.Inertia;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

/**
 * JAX-RS twin of the reactive instant-visit pages: renders the same Vue
 * components ({@code InstantVisits}/{@code InstantVisitTarget}) through the
 * Quarkus REST transport so the instant-visit contract (PROTO-057B) is proven
 * on every transport. The client page derives its target URL from
 * {@code page.url}, keeping both variants on the same transport.
 */
@Path("/features/rest-instant")
@Blocking
public class InstantRestResource {

    @Inject
    Inertia inertia;

    @GET
    @Path("visits")
    @Blocking
    public Uni<Object> visits() {
        return inertia.render("Features/Navigation/InstantVisits", Map.of(
            "sourceTimestamp", Demo.now(),
            "message", "This is the source page."));
    }

    @GET
    @Path("instant-visit-target")
    @Blocking
    public Uni<Object> target(@QueryParam("delay") @DefaultValue("2") int delay) {
        Demo.sleepSeconds(Math.min(Math.max(delay, 0), 5));
        return inertia.render("Features/Navigation/InstantVisitTarget", Map.of(
            "greeting", "Hello from the server!",
            "serverTimestamp", Demo.now(),
            "items", items(3)));
    }

    private java.util.List<Map<String, Object>> items(int count) {
        var items = new ArrayList<Map<String, Object>>();
        for (int i = 1; i <= count; i++) {
            items.add(Map.of("id", i, "name", "Server Item " + (char) ('A' + i - 1)));
        }
        return items;
    }
}
