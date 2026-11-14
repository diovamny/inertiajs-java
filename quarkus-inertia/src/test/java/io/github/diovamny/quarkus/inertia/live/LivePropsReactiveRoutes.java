package io.github.diovamny.quarkus.inertia.live;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

import io.github.diovamny.inertia.core.model.LiveChannel;
import io.github.diovamny.inertia.core.model.LivePropListener;
import io.github.diovamny.quarkus.inertia.api.Inertia;

/**
 * Reactive-routes counterparts of {@link LivePropsTestResource}, proving the
 * live-prop contract off the JAX-RS stack with request-scoped resolvers.
 */
@RouteBase(path = "/live-props-reactive")
public class LivePropsReactiveRoutes {

    static final AtomicInteger ORDER_RESOLVED = new AtomicInteger();
    static final AtomicInteger DEFERRED_RESOLVED = new AtomicInteger();

    @Inject
    Inertia inertia;

    @Blocking
    @Route(path = "order")
    public Uni<Object> order() {
        inertia.live("order", () -> {
            ORDER_RESOLVED.incrementAndGet();
            return Uni.createFrom().item((Object) Map.of(
                "id", 1,
                "status", "shipped",
                "revision", ORDER_RESOLVED.get()));
        }, new LivePropListener(LiveChannel.privateChannel("orders.1"), "OrderUpdated"));
        return inertia.render("Live/Show", Map.of("other", "o"));
    }

    @Blocking
    @Route(path = "order-defer")
    public Uni<Object> orderDefer() {
        return inertia.render("Live/Show", Map.of("other", "o"))
            .defer("default", "stats", LivePropsReactiveRoutes::resolveDeferred)
            .live("stats", LivePropsReactiveRoutes::resolveDeferred,
                new LivePropListener(LiveChannel.publicChannel("stats"), "StatsUpdated"));
    }

    @Blocking
    @Route(path = "throttle")
    public Uni<Object> throttle() {
        inertia.live("poll", () -> Uni.createFrom().item((Object) "p"), 2000,
            new LivePropListener(LiveChannel.publicChannel("poll"), "PollUpdated"));
        return inertia.render("Live/Show", Map.of());
    }

    private static Uni<Object> resolveDeferred() {
        DEFERRED_RESOLVED.incrementAndGet();
        return Uni.createFrom().item((Object) Map.of("total", 42));
    }
}
