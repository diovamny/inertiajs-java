package com.example.pingcrm.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

import io.github.diovamny.quarkus.inertia.api.Inertia;

/**
 * E2E feature probe (Reactive Routes twin of {@link ProbeResource}): identical
 * observable contract so {@code e2e/feature-matrix.spec.ts} certifies the
 * React/Svelte x Quarkus-Reactive cells via
 * {@code E2E_PROBE_BASE=/e2e-probe-rx}. Public route (see the
 * {@code /e2e-probe-rx} permit entries in {@code application.properties});
 * blocking worker semantics mirror the JAX-RS probe.
 */
public class ProbeReactiveRoutes {

    private static final List<Map<String, Object>> ENTRY_POOL = List.of(
        Map.of("id", 1, "name", "Alpha"),
        Map.of("id", 2, "name", "Beta"),
        Map.of("id", 3, "name", "Gamma"),
        Map.of("id", 4, "name", "Delta"),
        Map.of("id", 5, "name", "Epsilon"));

    @Inject
    Inertia inertia;

    @Blocking
    @Route(path = "/e2e-probe-rx", methods = HttpMethod.GET)
    public Uni<Object> index(RoutingContext rc) {
        inertia.deferred("probe", "slow", () -> Uni.createFrom().<Object>item("slow-value"));
        var entries = entries(rc);
        inertia.merge("entries", entries, false, "id");
        inertia.once("notice", "Probe notice");
        // The once prop is auto-included from the registration above (same
        // shape as ProbeResource): never duplicate it in the map, or the
        // eager value would bypass once tracking.
        return inertia.render("Probe/Index", Map.of(
            "greeting", "Probe source page.",
            "entries", entries));
    }

    @Blocking
    @Route(path = "/e2e-probe-rx/target", methods = HttpMethod.GET)
    public Uni<Object> target(RoutingContext rc) {
        int delay = parseDelay(rc);
        sleepSeconds(delay);
        return inertia.render("Probe/Target", Map.of("greeting", "Hello from the server!"));
    }

    @Blocking
    @Route(path = "/e2e-probe-rx/validate", methods = HttpMethod.POST)
    public Uni<Object> submitValidation(RoutingContext rc) {
        var body = jsonBody(rc);
        var errors = new LinkedHashMap<String, String>();
        if (isBlank(body.getString("name"))) {
            errors.put("name", "Please enter your full name.");
        }
        if (isBlank(body.getString("email"))) {
            errors.put("email", "We need your email address.");
        }
        if (!errors.isEmpty()) {
            return inertia.back().withErrors(errors);
        }
        inertia.flash("success", "Primary probe form submitted!");
        return inertia.back();
    }

    @Blocking
    @Route(path = "/e2e-probe-rx/validate-secondary", methods = HttpMethod.POST)
    public Uni<Object> submitValidationSecondary(RoutingContext rc) {
        var body = jsonBody(rc);
        var errors = new LinkedHashMap<String, String>();
        if (isBlank(body.getString("title"))) {
            errors.put("title", "Please enter a title.");
        }
        if (isBlank(body.getString("body"))) {
            errors.put("body", "Please enter a body.");
        }
        if (!errors.isEmpty()) {
            return inertia.back().withErrors(errors);
        }
        inertia.flash("success", "Secondary probe form submitted!");
        return inertia.back();
    }

    @Blocking
    @Route(path = "/e2e-probe-rx/upload", methods = HttpMethod.POST)
    public Uni<Object> submitUpload(RoutingContext rc) {
        if (rc.fileUploads() == null || rc.fileUploads().isEmpty()) {
            return inertia.back().withErrors(Map.of("photo", "Please choose a photo."));
        }
        inertia.flash("success", "Uploaded 1 file(s) successfully!");
        return inertia.back();
    }

    @Blocking
    @Route(path = "/e2e-probe-rx/redirect-back", methods = HttpMethod.POST)
    public Uni<Object> redirectBack() {
        inertia.flash("success", "Redirected back via probe");
        return inertia.back();
    }

    private List<Object> entries(RoutingContext rc) {
        var resetHeader = rc.request().getHeader("X-Inertia-Reset");
        var resetProps = resetHeader != null && !resetHeader.isBlank()
            ? List.of(resetHeader.split(",")) : List.<String>of();
        int count;
        if (resetProps.contains("entries")) {
            count = 1;
        } else if (isEntriesReload(rc.request().getHeader("X-Inertia-Partial-Data"))) {
            var session = rc.session();
            var previous = session != null ? session.get("probeEntryCount") : null;
            var prev = previous instanceof Number n ? n.intValue() : 1;
            count = Math.min(prev + 1, ENTRY_POOL.size());
        } else {
            count = 1;
        }
        if (rc.session() != null) {
            rc.session().put("probeEntryCount", count);
        }
        // Partials return only the fresh entry: the official client appends
        // it (merge + matchOn id). A replacing client would lose Alpha, so
        // the "2 entries" assertion below only passes with real merging.
        var slice = new ArrayList<Object>();
        slice.add(new LinkedHashMap<>(ENTRY_POOL.get(count - 1)));
        return slice;
    }

    /**
     * Only partial reloads that actually request {@code entries} advance the
     * pool: the official client auto-fetches deferred props after mount, and
     * that unrelated partial must not consume merge entries.
     */
    private static boolean isEntriesReload(String partialData) {
        if (partialData == null) {
            return false;
        }
        for (var part : partialData.split(",")) {
            if ("entries".equals(part.trim())) {
                return true;
            }
        }
        return false;
    }

    private static int parseDelay(RoutingContext rc) {
        var params = rc.queryParam("delay");
        int delay = 2;
        if (!params.isEmpty()) {
            try {
                delay = Integer.parseInt(params.get(0));
            } catch (NumberFormatException ignored) {
                // falls through to the default below
            }
        }
        return Math.min(Math.max(delay, 0), 5);
    }

    private static io.vertx.core.json.JsonObject jsonBody(RoutingContext rc) {
        try {
            var body = rc.body().asJsonObject();
            return body != null ? body : new io.vertx.core.json.JsonObject();
        } catch (Exception ignored) {
            return new io.vertx.core.json.JsonObject();
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static void sleepSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
