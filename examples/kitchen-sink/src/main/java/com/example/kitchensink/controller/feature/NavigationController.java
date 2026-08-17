package com.example.kitchensink.controller.feature;

import java.util.ArrayList;
import java.util.Map;
import jakarta.inject.Inject;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import com.example.kitchensink.service.Demo;
import io.github.dg.quarkus.inertia.api.Inertia;

@RouteBase(path = "/features/navigation")
public class NavigationController {

    @Inject
    Inertia inertia;

    @Route(path = "links", methods = {
        Route.HttpMethod.GET,
        Route.HttpMethod.POST,
        Route.HttpMethod.PUT,
        Route.HttpMethod.PATCH,
        Route.HttpMethod.DELETE })
    @Blocking
    public Uni<Object> links(RoutingContext rc) {
        if (rc.request().method() != HttpMethod.GET) {
            return linksAction(rc.request().method().name());
        }
        return inertia.render("Features/Navigation/Links", Map.of("timestamp", Demo.now()));
    }

    @Route(path = "links-action", methods = { Route.HttpMethod.GET, Route.HttpMethod.POST })
    @Blocking
    public Uni<Object> linksActionRoute(RoutingContext rc) {
        return linksAction(rc.request().method().name());
    }

    private Uni<Object> linksAction(String method) {
        inertia.flash("message", method + " request received at " + Demo.time());
        return inertia.back();
    }

    @Route(path = "preserve-state")
    @Blocking
    public Uni<Object> preserveState() {
        return inertia.render("Features/Navigation/PreserveState", Map.of(
            "serverCounter", Demo.randomInt(1, 1000),
            "timestamp", Demo.now()));
    }

    @Route(path = "preserve-scroll")
    @Blocking
    public Uni<Object> preserveScroll() {
        return inertia.render("Features/Navigation/PreserveScroll", Map.of(
            "timestamp", Demo.now()));
    }

    @Route(path = "view-transitions")
    @Blocking
    public Uni<Object> viewTransitions() {
        return inertia.render("Features/Navigation/ViewTransitions");
    }

    @Route(path = "history-management", methods = { Route.HttpMethod.GET, Route.HttpMethod.POST })
    @Blocking
    public Uni<Object> historyManagement(RoutingContext rc) {
        if (rc.request().method() != HttpMethod.GET) {
            return inertia.redirect("/features/navigation/history-management");
        }
        var visit = parseParam(rc.request().getParam("visit"), 0);
        return inertia.render("Features/Navigation/HistoryManagement", Map.of(
            "visit", visit,
            "timestamp", Demo.now()));
    }

    @Route(path = "async-requests")
    @Blocking
    public Uni<Object> asyncRequests(RoutingContext rc) {
        if (rc.request().getHeader("X-Inertia") != null) {
            Demo.sleepSeconds(1);
        }
        return inertia.render("Features/Navigation/AsyncRequests", Map.of(
            "timestamp", Demo.now()));
    }

    @Route(path = "async-slow")
    @Blocking
    public Uni<Object> asyncSlow(RoutingContext rc) {
        var delay = parseParam(rc.request().getParam("delay"), 2);
        Demo.sleepSeconds(Math.min(delay, 5));
        return inertia.render("Features/Navigation/AsyncRequests", Map.of(
            "timestamp", Demo.now()));
    }

    @Route(path = "instant-visits")
    @Blocking
    public Uni<Object> instantVisits() {
        return inertia.render("Features/Navigation/InstantVisits", Map.of(
            "sourceTimestamp", Demo.now(),
            "message", "This is the source page."));
    }

    @Route(path = "instant-visit-target")
    @Blocking
    public Uni<Object> instantVisitTarget(RoutingContext rc) {
        var delay = parseParam(rc.request().getParam("delay"), 2);
        Demo.sleepSeconds(Math.min(delay, 5));
        return inertia.render("Features/Navigation/InstantVisitTarget", Map.of(
            "greeting", "Hello from the server!",
            "serverTimestamp", Demo.now(),
            "items", items(3)));
    }

    @Route(path = "manual-visits")
    @Blocking
    public Uni<Object> manualVisits() {
        return inertia.render("Features/Navigation/ManualVisits", Map.of(
            "timestamp", Demo.now(),
            "counter", Demo.randomInt(1, 1000)));
    }

    @Route(path = "redirects")
    @Blocking
    public Uni<Object> redirectDemo() {
        return inertia.render("Features/Navigation/Redirects", Map.of(
            "timestamp", Demo.now()));
    }

    @Route(path = "redirects/back", methods = Route.HttpMethod.POST)
    @Blocking
    public Uni<Object> redirectStandard() {
        inertia.flash("message", "Redirected back via redirect()->back()");
        return inertia.back();
    }

    @Route(path = "redirects/to-route", methods = Route.HttpMethod.POST)
    @Blocking
    public Uni<Object> redirectToRoute() {
        inertia.flash("message", "Redirected via to_route()");
        return inertia.redirect("/features/navigation/redirects");
    }

    @Route(path = "redirects/external", methods = Route.HttpMethod.POST)
    @Blocking
    public Uni<Object> redirectExternal() {
        return inertia.location("https://cloud.laravel.com");
    }

    @Route(path = "scroll-management")
    @Blocking
    public Uni<Object> scrollManagement() {
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

    @Route(path = "url-fragments")
    @Blocking
    public Uni<Object> urlFragments() {
        return inertia.render("Features/Navigation/UrlFragments", Map.of(
            "timestamp", Demo.now()));
    }

    @Route(path = "url-fragments/redirect-hash", methods = { Route.HttpMethod.GET, Route.HttpMethod.POST })
    @Blocking
    public Uni<Object> redirectWithHash() {
        return inertia.redirect("/features/navigation/url-fragments#server-section");
    }

    @Route(path = "url-fragments/preserve-redirect")
    @Blocking
    public Uni<Object> preserveFragmentRedirect() {
        inertia.preserveFragment(true);
        return inertia.redirect("/features/navigation/url-fragments/preserve-target");
    }

    @Route(path = "url-fragments/preserve-target")
    @Blocking
    public Uni<Object> preserveFragmentTarget() {
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