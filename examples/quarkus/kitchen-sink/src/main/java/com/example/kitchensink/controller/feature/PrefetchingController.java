package com.example.kitchensink.controller.feature;

import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.github.diovamny.quarkus.inertia.api.Inertia;

@Path("/features/prefetching")
public class PrefetchingController {

    @Inject
    Inertia inertia;

    @GET
    @Path("link-prefetch")
    @Blocking
    public Uni<Object> linkPrefetch() {
        return inertia.render("Features/Prefetching/LinkPrefetch");
    }

    @GET
    @Path("stale-while-revalidate")
    @Blocking
    public Uni<Object> staleWhileRevalidate() {
        return inertia.render("Features/Prefetching/StaleWhileRevalidate");
    }

    @GET
    @Path("manual-prefetch")
    @Blocking
    public Uni<Object> manualPrefetch() {
        return inertia.render("Features/Prefetching/ManualPrefetch");
    }

    @GET
    @Path("cache-management")
    @Blocking
    public Uni<Object> cacheManagement() {
        return inertia.render("Features/Prefetching/CacheManagement");
    }
}
