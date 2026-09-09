package io.github.diovamny.quarkus.inertia.reactive;

import java.util.Map;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

import io.github.diovamny.quarkus.inertia.api.Inertia;

@RouteBase(path = "/reactive")
public class ReactiveRouteTestResource {

    @Inject
    Inertia inertia;

    @Route(path = "hello")
    public Uni<Object> hello() {
        return inertia.render("Reactive/Hello", Map.of("name", "World"));
    }

    @Blocking
    @Route(path = "redirect")
    public Uni<Object> redirect() {
        return inertia.redirect("/reactive/hello");
    }

    @Blocking
    @Route(path = "back")
    public Uni<Object> back(RoutingContext rc) {
        if ("POST".equals(rc.request().method().name())) {
            inertia.flash("success", "backed");
        }
        return inertia.back();
    }

    @Route(path = "hash-redirect")
    public Uni<Object> hashRedirect() {
        return inertia.redirect("/reactive/hello#section");
    }

    @Route(path = "external")
    public Uni<Object> external() {
        return inertia.location("https://example.com");
    }

    @Route(path = "error")
    public Uni<Object> error() {
        throw new ValidationException("boom");
    }
}
