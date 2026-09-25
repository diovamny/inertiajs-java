package com.example.pingcrm.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.github.diovamny.quarkus.inertia.api.Inertia;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * E2E feature probe (Fase E, JAX-RS): same contract as the Spring
 * {@code ProbeController} so {@code e2e/feature-matrix.spec.ts} certifies
 * React × Quarkus-REST cells against identical behavior.
 */
@Path("/e2e-probe")
@Blocking
public class ProbeResource {

    private static final List<Map<String, Object>> ENTRY_POOL = List.of(
        Map.of("id", 1, "name", "Alpha"),
        Map.of("id", 2, "name", "Beta"),
        Map.of("id", 3, "name", "Gamma"),
        Map.of("id", 4, "name", "Delta"),
        Map.of("id", 5, "name", "Epsilon"));

    @Inject
    Inertia inertia;

    @Context
    RoutingContext rc;

    @GET
    @Blocking
    public Uni<Object> index(@HeaderParam("X-Inertia-Partial-Data") String partialData,
            @HeaderParam("X-Inertia-Reset") String resetHeader) {
        inertia.deferred("probe", "slow", () -> Uni.createFrom().<Object>item("slow-value"));
        var entries = entries(partialData, resetHeader);
        inertia.merge("entries", entries, false, "id");
        inertia.once("notice", "Probe notice");
        // The once prop is auto-included from the registration above (same
        // shape as the kitchen-sink OnceProps fixture): never duplicate it
        // in the map, or the eager value would bypass once tracking.
        return inertia.render("Probe/Index", Map.of(
            "greeting", "Probe source page.",
            "entries", entries));
    }

    @GET
    @Path("target")
    @Blocking
    public Uni<Object> target(@QueryParam("delay") @DefaultValue("2") int delay) {
        sleepSeconds(Math.min(Math.max(delay, 0), 5));
        return inertia.render("Probe/Target", Map.of("greeting", "Hello from the server!"));
    }

    @POST
    @Path("validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Response submitValidation(ProbeForm form) {
        var errors = new LinkedHashMap<String, String>();
        if (form.name == null || form.name.isBlank()) {
            errors.put("name", "Please enter your full name.");
        }
        if (form.email == null || form.email.isBlank()) {
            errors.put("email", "We need your email address.");
        }
        if (!errors.isEmpty()) {
            return inertia.back().withErrors(errors).toResponse();
        }
        return inertia.back().with("success", "Primary probe form submitted!").toResponse();
    }

    @POST
    @Path("validate-secondary")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Response submitValidationSecondary(ProbeSecondaryForm form) {
        var errors = new LinkedHashMap<String, String>();
        if (form.title == null || form.title.isBlank()) {
            errors.put("title", "Please enter a title.");
        }
        if (form.body == null || form.body.isBlank()) {
            errors.put("body", "Please enter a body.");
        }
        if (!errors.isEmpty()) {
            return inertia.back().withErrors(errors).toResponse();
        }
        return inertia.back().with("success", "Secondary probe form submitted!").toResponse();
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Blocking
    public Response submitUpload(@RestForm("photo") FileUpload photo) {
        if (photo == null) {
            return inertia.back().withErrors(Map.of("photo", "Please choose a photo.")).toResponse();
        }
        return inertia.back().with("success", "Uploaded 1 file(s) successfully!").toResponse();
    }

    @POST
    @Path("redirect-back")
    @Blocking
    public Response redirectBack() {
        inertia.flash("success", "Redirected back via probe");
        return inertia.back().toResponse();
    }

    private List<Object> entries(String partialData, String resetHeader) {
        var resetProps = resetHeader != null && !resetHeader.isBlank()
            ? List.of(resetHeader.split(",")) : List.<String>of();
        int count;
        if (resetProps.contains("entries")) {
            count = 1;
        } else if (isEntriesReload(partialData)) {
            var session = rc != null ? rc.session() : null;
            var previous = session != null ? session.get("probeEntryCount") : null;
            var prev = previous instanceof Number n ? n.intValue() : 1;
            count = Math.min(prev + 1, ENTRY_POOL.size());
        } else {
            count = 1;
        }
        if (rc != null && rc.session() != null) {
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

    private static void sleepSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static class ProbeForm {
        public String name;
        public String email;
    }

    public static class ProbeSecondaryForm {
        public String title;
        public String body;
    }
}
