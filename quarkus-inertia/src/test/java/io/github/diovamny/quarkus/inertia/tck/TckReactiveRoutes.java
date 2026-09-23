package io.github.diovamny.quarkus.inertia.tck;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.ws.rs.core.Response;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

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
    @Route(path = "deferred")
    public Uni<Object> deferred() {
        inertia.deferred("slow", "data", () -> Uni.createFrom().item((Object) "lazy-value"));
        return inertia.render("Tck/Deferred", Map.of("title", "t"));
    }

    @Blocking
    @Route(path = "once")
    public Uni<Object> once() {
        inertia.once("notice", "Hello");
        return inertia.render("Tck/Once", Map.of());
    }

    @Blocking
    @Route(path = "submit", methods = HttpMethod.POST)
    public Uni<Object> submit(RoutingContext rc) {
        io.vertx.core.json.JsonObject body = null;
        try {
            body = rc.body().asJsonObject();
        } catch (Exception ignored) {
            // falls through to a blank name below
        }
        var name = body != null ? body.getString("name") : null;
        if (name == null || name.isBlank()) {
            return inertia.back().withErrors(Map.of("name", "required"));
        }
        return inertia.redirect("/tck/page");
    }

    @Blocking
    @Route(path = "redirect-me")
    public Uni<Object> redirectMe() {
        return inertia.redirect("/tck/page");
    }

    @Blocking
    @Route(path = "put-me", methods = HttpMethod.PUT)
    public Uni<Object> putMe() {
        return inertia.redirect("/tck/page");
    }

    @Blocking
    @Route(path = "delete-me", methods = HttpMethod.DELETE)
    public Uni<Object> deleteMe() {
        return inertia.redirect("/tck/page");
    }

    @Blocking
    @Route(path = "back", methods = HttpMethod.POST)
    public Uni<Object> back() {
        return inertia.back();
    }

    @Blocking
    @Route(path = "versioned")
    public Uni<Object> versioned() {
        return inertia.render("Tck/Versioned", Map.of());
    }

    @Blocking
    @Route(path = "external")
    public Uni<Object> external() {
        return inertia.location("https://example.com");
    }

    @Blocking
    @Route(path = "once-keyed")
    public Uni<Object> onceKeyed() {
        inertia.once("notice", "Keyed", "tckNotice");
        return inertia.render("Tck/OnceKeyed", Map.of("notice", "Keyed"));
    }

    @Blocking
    @Route(path = "once-mixed")
    public Uni<Object> onceMixed() {
        inertia.once("live", "Live", "tckLive");
        inertia.once("stale", () -> Uni.createFrom().item((Object) "Stale"),
            "tckStale", Instant.now().minusSeconds(3600));
        return inertia.render("Tck/OnceMixed", Map.of("live", "Live"));
    }

    @Blocking
    @Route(path = "merge")
    public Uni<Object> merge() {
        inertia.merge("items", List.of(Map.of("id", 1, "name", "One")), false, "id");
        inertia.prepend("tags", List.of("a", "b"));
        inertia.merge("config", Map.of("theme", "dark"), true);
        return inertia.render("Tck/Merge", Map.of(
            "items", List.of(Map.of("id", 1, "name", "One")),
            "tags", List.of("a", "b"),
            "config", Map.of("theme", "dark")));
    }

    @Blocking
    @Route(path = "merge-nested")
    public Uni<Object> mergeNested() {
        var posts = Map.of("data", List.of(Map.of("id", 1, "title", "One")),
            "pinned", List.of(Map.of("id", 9, "title", "Nine")));
        var value = inertia.mergeable("posts", posts)
            .append("data")
            .prepend("pinned")
            .matchOn("data.id")
            .value();
        var config = inertia.mergeable("config", Map.of("theme", "dark")).deep("theme").value();
        return inertia.render("Tck/MergeNested", Map.of("posts", value, "config", config));
    }

    @Blocking
    @Route(path = "scroll")
    public Uni<Object> scroll() {
        inertia.scroll("items", List.of(Map.of("id", 1), Map.of("id", 2)),
            Map.of("currentPage", 1, "nextPage", 2, "pageName", "page"));
        return inertia.render("Tck/Scroll", Map.of());
    }

    @Blocking
    @Route(path = "shared")
    public Uni<Object> shared() {
        inertia.share("tckShared", "yes");
        inertia.always("tckAlways", "always-yes");
        return inertia.render("Tck/Shared", Map.of("a", "A"));
    }

    @Blocking
    @Route(path = "once-combos")
    public Uni<Object> onceCombos() {
        inertia.merge("combo", List.of("a"), false);
        inertia.once("combo", List.of("a"), "tckCombo");
        inertia.optional("comboOpt", () -> Uni.createFrom().item((Object) "CO"));
        inertia.deferred("slow", "comboDef", () -> Uni.createFrom().item((Object) "CD"));
        return inertia.render("Tck/OnceCombos", Map.of("combo", List.of("a")));
    }

    @Inject
    Validator validator;

    @Blocking
    @Route(path = "submit-multi", methods = HttpMethod.POST)
    public Uni<Object> submitMulti() {
        return inertia.back().withErrorMessages(Map.of("name", List.of("required", "must be valid")));
    }

    @Blocking
    @Route(path = "submit-valid", methods = HttpMethod.POST)
    public Uni<Object> submitValid(RoutingContext rc) {
        io.vertx.core.json.JsonObject body = null;
        try {
            body = rc.body().asJsonObject();
        } catch (Exception ignored) {
            // falls through to a blank name below
        }
        var name = body != null ? body.getString("name") : null;
        var violations = validator.validate(new TckTestResource.TckNameForm(name));
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return inertia.redirect("/tck/page");
    }

    @Blocking
    @Route(path = "upload", methods = HttpMethod.POST)
    public Uni<Object> upload(RoutingContext rc) {
        if (rc.fileUploads() == null || rc.fileUploads().isEmpty()) {
            return Uni.createFrom().item((Object) Response.status(400).build());
        }
        return inertia.redirect("/tck/page");
    }

    @Blocking
    @Route(path = "redirect-to")
    public Uni<Object> redirectTo(RoutingContext rc) {
        var targets = rc.queryParam("target");
        return inertia.redirect(targets.isEmpty() ? "/" : targets.get(0));
    }

    @Blocking
    @Route(path = "big-page")
    public Uni<Object> bigPage() {
        return inertia.render("Tck/Big", Map.of("bulk", "x".repeat(100_000)));
    }
}
