package io.github.dg.spring.inertia.integration;

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.github.dg.spring.inertia.api.Inertia;
import io.github.dg.spring.inertia.api.Inertia.MergeRule;

@RestController
public class TestController {

    static int deferredHeavyResolved;

    private final Inertia inertia;

    public TestController(Inertia inertia) {
        this.inertia = inertia;
    }

    @GetMapping("/dashboard")
    public Object dashboard() {
        return inertia.render("Dashboard",
            Map.of("title", "Home", "users", List.of("a", "b")));
    }

    @GetMapping("/deferred")
    public Object deferred() {
        inertia.deferred("slow", "data", () -> "lazy-value");
        return inertia.render("DeferredPage", Map.of("title", "t"));
    }

    @GetMapping("/optional")
    public Object optional() {
        return inertia.render("OptionalPage", Map.of(
            "section1", inertia.optional("section1", () -> "one"),
            "section2", inertia.optional("section2", () -> "two")));
    }

    @GetMapping("/deferred-filter")
    public Object deferredFilter() {
        inertia.deferred("slow", "slow", () -> "slow-value");
        inertia.deferred("heavy", "heavy", () -> {
            deferredHeavyResolved++;
            return "heavy-value";
        });
        return inertia.render("DeferredPage", Map.of("title", "t"));
    }

    @GetMapping("/deferred-rescue")
    public Object deferredRescue(@RequestHeader(value = "X-Force-Success", defaultValue = "false") String forceSuccess) {
        inertia.deferred("flaky", "flakyReport", () -> {
            if (!Boolean.parseBoolean(forceSuccess)) {
                throw new IllegalStateException("boom");
            }
            return "ok";
        });
        inertia.rescue("flakyReport");
        return inertia.render("DeferredPage", Map.of("title", "t"));
    }

    @GetMapping("/scroll")
    public Object scroll() {
        inertia.scroll("contacts",
            Map.of("data", List.of(Map.of("id", 1))),
            Map.of("pageName", "cursor", "nextPage", "abc", "currentPage", 1, "matchOn", "id"));
        return inertia.render("ScrollPage", Map.of("title", "t"));
    }

    @GetMapping("/once-custom")
    public Object onceCustom() {
        return inertia.render("OncePage",
            Map.of("aliased", inertia.once("aliased", "v", "shared-key")));
    }

    @GetMapping("/preserve-fragment")
    public Object preserveFragment() {
        inertia.preserveFragment(true);
        return inertia.render("FragmentPage", Map.of("title", "t"));
    }

    @GetMapping("/preserve-redirect")
    public Object preserveRedirect() {
        inertia.preserveFragment(true);
        return inertia.redirect("/flash");
    }

    @GetMapping("/merge-match")
    public Object mergeMatch() {
        inertia.merge("contacts", List.of(Map.of("id", 1)), MergeRule.MERGE, "id");
        return inertia.render("MergePage", Map.of("title", "t"));
    }

    @GetMapping("/shared")
    public Object shared() {
        inertia.share("appName", "MyApp");
        inertia.always("csrf", "token123");
        return inertia.render("SharedPage", Map.of("extra", "e"));
    }

    @GetMapping("/once")
    public Object once() {
        return inertia.render("OncePage", Map.of("notice", inertia.once("notice", "Hello")));
    }

    @GetMapping("/flash")
    public Object flash() {
        return inertia.render("FlashPage", Map.of());
    }

    @GetMapping("/goto")
    public Object gotoUrl() {
        return inertia.redirect("/flash", true);
    }

    @PostMapping("/submit")
    public Object submit() {
        return inertia.redirect("/flash").with("message", "Saved");
    }

    @PostMapping("/form")
    public Object form(@Valid @RequestBody UserForm form) {
        return inertia.redirect("/flash");
    }

    @GetMapping("/html")
    public Object html() {
        return inertia.render("Dashboard", Map.of("title", "Home"));
    }

    public record UserForm(@NotBlank String name, @Email String email) {
    }
}
