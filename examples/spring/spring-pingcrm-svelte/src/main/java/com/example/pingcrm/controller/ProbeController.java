package com.example.pingcrm.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.github.diovamny.spring.inertia.api.Inertia;

/**
 * E2E feature probe (Fase E): one public page exercising the full Inertia v3
 * contract through the official React client — validation errors, named error
 * bags, deferred props, once props, mergeable props, instant visits, file
 * uploads and redirect-back flash. Mirrors the kitchen-sink feature fixtures
 * so {@code e2e/feature-matrix.spec.ts} can certify React × Spring cells.
 */
@RestController
@RequestMapping("/e2e-probe")
public class ProbeController {

    private static final List<Map<String, Object>> ENTRY_POOL = List.of(
        Map.of("id", 1, "name", "Alpha"),
        Map.of("id", 2, "name", "Beta"),
        Map.of("id", 3, "name", "Gamma"),
        Map.of("id", 4, "name", "Delta"),
        Map.of("id", 5, "name", "Epsilon"));

    private final Inertia inertia;

    public ProbeController(Inertia inertia) {
        this.inertia = inertia;
    }

    @GetMapping
    public Object index(HttpSession session,
            @RequestHeader(value = "X-Inertia-Partial-Data", required = false) String partialData,
            @RequestHeader(value = "X-Inertia-Reset", required = false) String resetHeader) {
        inertia.deferred("probe", "slow", () -> "slow-value");
        var entries = entries(session, partialData, resetHeader);
        inertia.merge("entries", entries, Inertia.MergeRule.MERGE, "id");
        var props = new LinkedHashMap<String, Object>();
        props.put("greeting", "Probe source page.");
        props.put("notice", inertia.once("notice", "Probe notice"));
        props.put("entries", entries);
        return inertia.render("Probe/Index", props);
    }

    @GetMapping("/target")
    public Object target(@RequestParam(value = "delay", required = false, defaultValue = "2") int delay) {
        sleepSeconds(Math.min(Math.max(delay, 0), 5));
        return inertia.render("Probe/Target", Map.of("greeting", "Hello from the server!"));
    }

    @PostMapping(value = "/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object submitValidation(@RequestBody ProbeForm form) {
        var errors = new LinkedHashMap<String, String>();
        if (form.name == null || form.name.isBlank()) {
            errors.put("name", "Please enter your full name.");
        }
        if (form.email == null || form.email.isBlank()) {
            errors.put("email", "We need your email address.");
        }
        if (!errors.isEmpty()) {
            return inertia.back().withErrors(errors);
        }
        return inertia.back().with("success", "Primary probe form submitted!");
    }

    @PostMapping(value = "/validate-secondary", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object submitValidationSecondary(@RequestBody ProbeSecondaryForm form) {
        var errors = new LinkedHashMap<String, String>();
        if (form.title == null || form.title.isBlank()) {
            errors.put("title", "Please enter a title.");
        }
        if (form.body == null || form.body.isBlank()) {
            errors.put("body", "Please enter a body.");
        }
        if (!errors.isEmpty()) {
            return inertia.back().withErrors(errors);
        }
        return inertia.back().with("success", "Secondary probe form submitted!");
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object submitUpload(@RequestPart(value = "photo", required = false) MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            return inertia.back().withErrors(Map.of("photo", "Please choose a photo."));
        }
        return inertia.back().with("success", "Uploaded 1 file(s) successfully!");
    }

    @PostMapping("/redirect-back")
    public Object redirectBack() {
        inertia.flash("success", "Redirected back via probe");
        return inertia.back();
    }

    private List<Object> entries(HttpSession session, String partialData, String resetHeader) {
        var resetProps = resetHeader != null && !resetHeader.isBlank()
            ? List.of(resetHeader.split(",")) : List.<String>of();
        int count;
        if (resetProps.contains("entries")) {
            count = 1;
        } else if (isEntriesReload(partialData)) {
            var previous = session.getAttribute("probeEntryCount");
            var prev = previous instanceof Number n ? n.intValue() : 1;
            count = Math.min(prev + 1, ENTRY_POOL.size());
        } else {
            count = 1;
        }
        session.setAttribute("probeEntryCount", count);
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
