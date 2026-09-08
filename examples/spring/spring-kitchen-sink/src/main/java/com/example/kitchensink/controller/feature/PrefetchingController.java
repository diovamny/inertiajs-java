package com.example.kitchensink.controller.feature;

import org.springframework.web.bind.annotation.RestController;

import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class PrefetchingController {

    private final Inertia inertia;

    public PrefetchingController(Inertia inertia) {
        this.inertia = inertia;
    }

    @org.springframework.web.bind.annotation.GetMapping("/features/prefetching/link-prefetch")
    public Object linkPrefetch() {
        return inertia.render("Features/Prefetching/LinkPrefetch");
    }

    @org.springframework.web.bind.annotation.GetMapping("/features/prefetching/stale-while-revalidate")
    public Object staleWhileRevalidate() {
        return inertia.render("Features/Prefetching/StaleWhileRevalidate");
    }

    @org.springframework.web.bind.annotation.GetMapping("/features/prefetching/manual-prefetch")
    public Object manualPrefetch() {
        return inertia.render("Features/Prefetching/ManualPrefetch");
    }

    @org.springframework.web.bind.annotation.GetMapping("/features/prefetching/cache-management")
    public Object cacheManagement() {
        return inertia.render("Features/Prefetching/CacheManagement");
    }
}