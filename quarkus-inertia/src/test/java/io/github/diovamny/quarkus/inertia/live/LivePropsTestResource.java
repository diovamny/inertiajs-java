package io.github.diovamny.quarkus.inertia.live;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import io.smallrye.mutiny.Uni;

import io.github.diovamny.inertia.core.model.LiveChannel;
import io.github.diovamny.inertia.core.model.LivePropListener;
import io.github.diovamny.quarkus.inertia.api.Inertia;

/**
 * Endpoints backing {@link LivePropsQuarkusTest}: each route exercises one
 * dimension of the live-prop contract (resolver invocation, partial reloads,
 * defer composition and throttle metadata).
 */
@Path("/live-props")
public class LivePropsTestResource {

    static final AtomicInteger ORDER_RESOLVED = new AtomicInteger();
    static final AtomicInteger DEFERRED_RESOLVED = new AtomicInteger();

    @Inject
    Inertia inertia;

    @GET
    @Path("/order")
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

    @GET
    @Path("/order-defer")
    public Uni<Object> orderDefer() {
        return inertia.render("Live/Show", Map.of("other", "o"))
            .defer("default", "stats", LivePropsTestResource::resolveDeferred)
            .live("stats", LivePropsTestResource::resolveDeferred,
                new LivePropListener(LiveChannel.publicChannel("stats"), "StatsUpdated"));
    }

    @GET
    @Path("/throttle")
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
