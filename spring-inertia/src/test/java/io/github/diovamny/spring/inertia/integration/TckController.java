package io.github.diovamny.spring.inertia.integration;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.github.diovamny.spring.inertia.api.Inertia;
import io.github.diovamny.spring.inertia.api.Inertia.MergeRule;

/**
 * Fixed endpoints backing the executable TCK suite
 * ({@code protocol-v3/*.yaml} in {@code inertia-tck}).
 */
@RestController
@RequestMapping("/tck")
public class TckController {

    private final Inertia inertia;

    public TckController(Inertia inertia) {
        this.inertia = inertia;
    }

    @GetMapping("/page")
    public Object page() {
        return inertia.render("Tck/Page", Map.of("a", "A", "b", "B"));
    }

    @GetMapping("/deferred")
    public Object deferred() {
        inertia.deferred("slow", "data", () -> "lazy-value");
        return inertia.render("Tck/Deferred", Map.of("title", "t"));
    }

    @GetMapping("/once")
    public Object once() {
        return inertia.render("Tck/Once", Map.of("notice", inertia.once("notice", "Hello")));
    }

    @PostMapping("/submit")
    public Object submit(@RequestBody Map<String, Object> body) {
        var name = body.get("name");
        if (name == null || name.toString().isBlank()) {
            return inertia.back().withErrors(Map.of("name", "required"));
        }
        return inertia.redirect("/tck/page");
    }

    @PostMapping("/submit-multi")
    public Object submitMulti() {
        return inertia.back().withErrorMessages(Map.of("name", List.of("required", "must be valid")));
    }

    @GetMapping("/redirect-me")
    public Object redirectMe() {
        return inertia.redirect("/tck/page");
    }

    @PutMapping("/put-me")
    public Object putMe() {
        return inertia.redirect("/tck/page");
    }

    @DeleteMapping("/delete-me")
    public Object deleteMe() {
        return inertia.redirect("/tck/page");
    }

    @GetMapping("/external")
    public Object external() {
        return inertia.redirect("https://example.com", true);
    }

    @PostMapping("/back")
    public Object back() {
        return inertia.back();
    }

    @GetMapping("/versioned")
    public Object versioned() {
        return inertia.render("Tck/Versioned", Map.of());
    }

    @GetMapping("/once-keyed")
    public Object onceKeyed() {
        return inertia.render("Tck/OnceKeyed",
            Map.of("notice", inertia.once("notice", "Keyed", "tckNotice")));
    }

    @GetMapping("/once-mixed")
    public Object onceMixed() {
        return inertia.render("Tck/OnceMixed", Map.of(
            "live", inertia.once("live", "Live", "tckLive"),
            "stale", inertia.once("stale", "Stale", "tckStale", Duration.ofMillis(-1000))));
    }

    @GetMapping("/merge")
    public Object merge() {
        return inertia.render("Tck/Merge", Map.of(
            "items", inertia.merge("items", List.of(Map.of("id", 1, "name", "One")),
                MergeRule.MERGE, "id"),
            "tags", inertia.merge("tags", List.of("a", "b"), MergeRule.PREPEND),
            "config", inertia.merge("config", Map.of("theme", "dark"), MergeRule.DEEP_MERGE)));
    }

    @GetMapping("/merge-nested")
    public Object mergeNested() {
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

    @GetMapping("/scroll")
    public Object scroll() {
        inertia.scroll("items", List.of(Map.of("id", 1), Map.of("id", 2)),
            Map.of("currentPage", 1, "nextPage", 2, "pageName", "page"));
        return inertia.render("Tck/Scroll", Map.of());
    }

    @GetMapping("/shared")
    public Object shared() {
        inertia.share("tckShared", "yes");
        inertia.always("tckAlways", "always-yes");
        return inertia.render("Tck/Shared", Map.of("a", "A"));
    }

    @GetMapping("/once-combos")
    public Object onceCombos() {
        var combo = inertia.merge("combo",
            inertia.once("combo", List.of("a"), "tckCombo"), MergeRule.MERGE);
        var opt = inertia.optional("comboOpt", () -> "CO");
        inertia.deferred("slow", "comboDef", () -> "CD");
        return inertia.render("Tck/OnceCombos", Map.of("combo", combo, "comboOpt", opt));
    }

    public record TckNameForm(@NotBlank String name) {
    }

    @PostMapping("/submit-valid")
    public Object submitValid(@Valid @RequestBody TckNameForm form) {
        return inertia.redirect("/tck/page");
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return inertia.redirect("/tck/page");
    }
}
