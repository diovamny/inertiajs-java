package io.github.diovamny.quarkus.inertia.tck;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import io.github.diovamny.quarkus.inertia.api.Inertia;

/**
 * Fixed endpoints backing the executable TCK suite
 * ({@code protocol-v3/*.yaml} in {@code inertia-tck}).
 */
@Path("/tck")
public class TckTestResource {

    @Inject
    Inertia inertia;

    @GET
    @Path("/page")
    public Uni<Object> page() {
        return inertia.render("Tck/Page", Map.of("a", "A", "b", "B"));
    }

    @GET
    @Path("/deferred")
    public Uni<Object> deferred() {
        inertia.deferred("slow", "data", () -> Uni.createFrom().item("lazy-value"));
        return inertia.render("Tck/Deferred", Map.of("title", "t"));
    }

    @GET
    @Path("/once")
    public Uni<Object> once() {
        inertia.once("notice", "Hello");
        return inertia.render("Tck/Once", Map.of());
    }

    @POST
    @Path("/submit")
    public Uni<Object> submit(Map<String, Object> body) {
        var name = body != null ? body.get("name") : null;
        if (name == null || name.toString().isBlank()) {
            return inertia.back().withErrors(Map.of("name", "required"));
        }
        return inertia.redirect("/tck/page");
    }

    @POST
    @Path("/submit-multi")
    public Uni<Object> submitMulti() {
        return inertia.back().withErrorMessages(Map.of("name", List.of("required", "must be valid")));
    }

    @GET
    @Path("/redirect-me")
    public Uni<Object> redirectMe() {
        return inertia.redirect("/tck/page");
    }

    @PUT
    @Path("/put-me")
    public Uni<Object> putMe() {
        return inertia.redirect("/tck/page");
    }

    @DELETE
    @Path("/delete-me")
    public Uni<Object> deleteMe() {
        return inertia.redirect("/tck/page");
    }

    @GET
    @Path("/external")
    public Uni<Object> external() {
        return inertia.location("https://example.com");
    }

    @POST
    @Path("/back")
    public Uni<Object> back() {
        return inertia.back();
    }

    @GET
    @Path("/versioned")
    public Uni<Object> versioned() {
        return inertia.render("Tck/Versioned", Map.of());
    }

    @GET
    @Path("/redirect-to")
    public Uni<Object> redirectTo(
            @jakarta.ws.rs.QueryParam("target") String target) {
        return inertia.redirect(target);
    }

    @GET
    @Path("/big-page")
    public Uni<Object> bigPage() {
        return inertia.render("Tck/Big", Map.of("bulk", "x".repeat(100_000)));
    }

    @GET
    @Path("/once-keyed")
    public Uni<Object> onceKeyed() {
        inertia.once("notice", "Keyed", "tckNotice");
        return inertia.render("Tck/OnceKeyed", Map.of("notice", "Keyed"));
    }

    @GET
    @Path("/once-mixed")
    public Uni<Object> onceMixed() {
        inertia.once("live", "Live", "tckLive");
        inertia.once("stale", () -> Uni.createFrom().item((Object) "Stale"),
            "tckStale", Instant.now().minusSeconds(3600));
        return inertia.render("Tck/OnceMixed", Map.of("live", "Live"));
    }

    @GET
    @Path("/merge")
    public Uni<Object> merge() {
        inertia.merge("items", List.of(Map.of("id", 1, "name", "One")), false, "id");
        inertia.prepend("tags", List.of("a", "b"));
        inertia.merge("config", Map.of("theme", "dark"), true);
        return inertia.render("Tck/Merge", Map.of(
            "items", List.of(Map.of("id", 1, "name", "One")),
            "tags", List.of("a", "b"),
            "config", Map.of("theme", "dark")));
    }

    @GET
    @Path("/scroll")
    public Uni<Object> scroll() {
        inertia.scroll("items", List.of(Map.of("id", 1), Map.of("id", 2)),
            Map.of("currentPage", 1, "nextPage", 2, "pageName", "page"));
        return inertia.render("Tck/Scroll", Map.of());
    }

    @GET
    @Path("/shared")
    public Uni<Object> shared() {
        inertia.share("tckShared", "yes");
        inertia.always("tckAlways", "always-yes");
        return inertia.render("Tck/Shared", Map.of("a", "A"));
    }

    @GET
    @Path("/once-combos")
    public Uni<Object> onceCombos() {
        inertia.merge("combo", List.of("a"), false);
        inertia.once("combo", List.of("a"), "tckCombo");
        inertia.optional("comboOpt", () -> Uni.createFrom().item((Object) "CO"));
        inertia.deferred("slow", "comboDef", () -> Uni.createFrom().item((Object) "CD"));
        return inertia.render("Tck/OnceCombos", Map.of("combo", List.of("a")));
    }

    public record TckNameForm(@NotBlank String name) {
    }

    @POST
    @Path("/submit-valid")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Object> submitValid(@Valid TckNameForm form) {
        return inertia.redirect("/tck/page");
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Uni<Object> upload(@RestForm("file") FileUpload file) {
        if (file == null || file.fileName() == null) {
            return Uni.createFrom().item((Object) Response.status(400).build());
        }
        return inertia.redirect("/tck/page");
    }
}
