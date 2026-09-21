package io.github.diovamny.spring.inertia.integration;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.diovamny.spring.inertia.api.Inertia;

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
}
