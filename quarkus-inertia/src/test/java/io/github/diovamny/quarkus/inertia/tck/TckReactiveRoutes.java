package io.github.diovamny.quarkus.inertia.tck;

import java.util.Map;

import jakarta.inject.Inject;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

import io.github.diovamny.quarkus.inertia.api.Inertia;

/**
 * Reactive-routes counterparts of the TCK endpoints, proving the same wire
 * contract off the JAX-RS stack.
 */
@RouteBase(path = "/tck-reactive")
public class TckReactiveRoutes {

    @Inject
    Inertia inertia;

    @Blocking
    @Route(path = "page")
    public Uni<Object> page() {
        return inertia.render("Tck/Page", Map.of("a", "A", "b", "B"));
    }

    @Blocking
    @Route(path = "redirect-me")
    public Uni<Object> redirectMe() {
        return inertia.redirect("/tck/page");
    }

    @Blocking
    @Route(path = "external")
    public Uni<Object> external() {
        return inertia.location("https://example.com");
    }
}
