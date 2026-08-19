package com.example.kitchensink.controller.feature;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kitchensink.service.Demo;
import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class NavigationController {

    private final Inertia inertia;

    public NavigationController(Inertia inertia) {
        this.inertia = inertia;
    }

    @RequestMapping(path = "/features/navigation/links",
            method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                    RequestMethod.PATCH, RequestMethod.DELETE })
    public Object links(HttpMethod method) {
        if (method != HttpMethod.GET) {
            return linksAction(method.name());
        }
        return inertia.render("Features/Navigation/Links", Map.of("timestamp", Demo.now()));
    }

    @RequestMapping(path = "/features/navigation/links-action",
            method = { RequestMethod.GET, RequestMethod.POST })
    public Object linksActionRoute(HttpMethod method) {
        return linksAction(method.name());
    }

    private Object linksAction(String method) {
        inertia.flash("message", method + " request received at " + Demo.time());
        return inertia.back();
    }

    @RequestMapping("/features/navigation/preserve-state")
    public Object preserveState() {
        return inertia.render("Features/Navigation/PreserveState", Map.of(
            "serverCounter", Demo.randomInt(1, 1000),
            "timestamp", Demo.now()));
    }

    @RequestMapping("/features/navigation/preserve-scroll")
    public Object preserveScroll() {
        return inertia.render("Features/Navigation/PreserveScroll", Map.of(
            "timestamp", Demo.now()));
    }

    @RequestMapping("/features/navigation/view-transitions")
    public Object viewTransitions() {
        return inertia.render("Features/Navigation/ViewTransitions", null);
    }

    @RequestMapping(path = "/features/navigation/history-management",
            method = { RequestMethod.GET, RequestMethod.POST })
    public Object historyManagement(HttpMethod method,
            @RequestParam(value = "visit", required = false) String visitRaw) {
        if (method != HttpMethod.GET) {
            return inertia.redirect("/features/navigation/history-management");
        }
        var visit = parseParam(visitRaw, 0);
        return inertia.render("Features/Navigation/HistoryManagement", Map.of(
            "visit", visit,
            "timestamp", Demo.now()));
    }

    @RequestMapping("/features/navigation/async-requests")
    public Object asyncRequests(@RequestHeader(value = "X-Inertia", required = false) String inertiaHeader) {
        if (inertiaHeader != null) {
            Demo.sleepSeconds(1);
        }
        return inertia.render("Features/Navigation/AsyncRequests", Map.of(
            "timestamp", Demo.now()));
    }

    @RequestMapping("/features/navigation/async-slow")
    public Object asyncSlow(@RequestParam(value = "delay", required = false) String delayRaw) {
        var delay = parseParam(delayRaw, 2);
        Demo.sleepSeconds(Math.min(delay, 5));
        return inertia.render("Features/Navigation/AsyncRequests", Map.of(
            "timestamp", Demo.now()));
    }

    @RequestMapping("/features/navigation/instant-visits")
    public Object instantVisits() {
        return inertia.render("Features/Navigation/InstantVisits", Map.of(
            "sourceTimestamp", Demo.now(),
            "message", "This is the source page."));
    }

    @RequestMapping("/features/navigation/instant-visit-target")
    public Object instantVisitTarget(@RequestParam(value = "delay", required = false) String delayRaw) {
        var delay = parseParam(delayRaw, 2);
        Demo.sleepSeconds(Math.min(delay, 5));
        return inertia.render("Features/Navigation/InstantVisitTarget", Map.of(
            "greeting", "Hello from the server!",
            "serverTimestamp", Demo.now(),
            "items", items(3)));
    }

    @RequestMapping("/features/navigation/manual-visits")
    public Object manualVisits() {
        return inertia.render("Features/Navigation/ManualVisits", Map.of(
            "timestamp", Demo.now(),
            "counter", Demo.randomInt(1, 1000)));
    }

    @RequestMapping("/features/navigation/redirects")
    public Object redirectDemo() {
        return inertia.render("Features/Navigation/Redirects", Map.of(
            "timestamp", Demo.now()));
    }

    @RequestMapping(path = "/features/navigation/redirects/back", method = RequestMethod.POST)
    public Object redirectStandard() {
        inertia.flash("message", "Redirected back via redirect()->back()");
        return inertia.back();
    }

    @RequestMapping(path = "/features/navigation/redirects/to-route", method = RequestMethod.POST)
    public Object redirectToRoute() {
        inertia.flash("message", "Redirected via to_route()");
        return inertia.redirect("/features/navigation/redirects");
    }

    @RequestMapping(path = "/features/navigation/redirects/external", method = RequestMethod.POST)
    public Object redirectExternal() {
        return inertia.location("https://cloud.laravel.com");
    }

    @RequestMapping("/features/navigation/scroll-management")
    public Object scrollManagement() {
        var items = new ArrayList<Map<String, Object>>();
        for (int i = 1; i <= 50; i++) {
            items.add(Map.of(
                "id", i,
                "title", "Item #" + i,
                "description", Demo.fakeSentence()));
        }
        return inertia.render("Features/Navigation/ScrollManagement", Map.of(
            "timestamp", Demo.now(),
            "items", items));
    }

    @RequestMapping("/features/navigation/url-fragments")
    public Object urlFragments() {
        return inertia.render("Features/Navigation/UrlFragments", Map.of(
            "timestamp", Demo.now()));
    }

    @RequestMapping(path = "/features/navigation/url-fragments/redirect-hash",
            method = { RequestMethod.GET, RequestMethod.POST })
    public Object redirectWithHash() {
        return inertia.redirect("/features/navigation/url-fragments#server-section");
    }

    @RequestMapping("/features/navigation/url-fragments/preserve-redirect")
    public Object preserveFragmentRedirect() {
        inertia.preserveFragment(true);
        return inertia.redirect("/features/navigation/url-fragments/preserve-target");
    }

    @RequestMapping("/features/navigation/url-fragments/preserve-target")
    public Object preserveFragmentTarget() {
        return inertia.render("Features/Navigation/UrlFragments", Map.of(
            "timestamp", Demo.now(),
            "redirectedFrom", "preserveFragment redirect"));
    }

    private int parseParam(String raw, int defaultValue) {
        if (raw == null || raw.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private java.util.List<Map<String, Object>> items(int count) {
        var items = new ArrayList<Map<String, Object>>();
        for (int i = 1; i <= count; i++) {
            items.add(Map.of("id", i, "name", "Server Item " + (char) ('A' + i - 1)));
        }
        return items;
    }
}