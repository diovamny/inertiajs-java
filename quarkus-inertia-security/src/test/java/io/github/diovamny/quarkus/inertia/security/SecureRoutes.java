package io.github.diovamny.quarkus.inertia.security;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.inject.Inject;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

import io.github.diovamny.quarkus.inertia.api.Inertia;

@RouteBase(path = "/rsec")
public class SecureRoutes {

    static final AtomicInteger SUBMIT_INVOCATIONS = new AtomicInteger();

    @Inject
    Inertia inertia;

    @Route(path = "ping", methods = HttpMethod.GET)
    public Uni<Object> ping() {
        return inertia.render("Rsec/Ping", Map.of());
    }

    @Route(path = "submit", methods = HttpMethod.POST)
    @Blocking
    public Uni<Object> submit() {
        SUBMIT_INVOCATIONS.incrementAndGet();
        return inertia.redirect("/rsec/ping");
    }
}
