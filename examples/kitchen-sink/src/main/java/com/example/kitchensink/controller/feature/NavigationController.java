package com.example.kitchensink.controller.feature;

import java.util.ArrayList;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;

import com.example.kitchensink.service.Demo;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import com.quarkus.inertia.api.Inertia;
import io.vertx.ext.web.RoutingContext;

@Path("/features/navigation")
public class NavigationController {

    @Inject
    Inertia inertia;

    @GET
    @Path("links")
    @Blocking
    public Uni<Object> links() {
        return inertia.render("Features/Navigation/Links", Map.of("timestamp", Demo.now()));
    }

    @POST
    @Path("links")
    @Blocking
    public Uni<Object> linksPost() {
        return linksAction("POST");
    }

    @PUT
    @Path("links")
    @Blocking
    public Uni<Object> linksPut() {
        return linksAction("PUT");
    }

    @PATCH
    @Path("links")
    @Blocking
    public Uni<Object> linksPatch() {
        return linksAction("PATCH");
    }

    @DELETE
    @Path("links")
    @Blocking
    public Uni<Object> linksDelete() {
        return linksAction("DELETE");
    }

    @GET
    @Path("links-action")
    @Blocking
    public Uni<Object> linksActionGet() {
        return linksAction("GET");
    }

    @POST
    @Path("links-action")
    @Blocking
    public Uni<Object> linksActionPost() {
        return linksAction("POST");
    }

    @Blocking
    private Uni<Object> linksAction(String method) {
        inertia.flash("message", method + " request received at " + Demo.time());
        return inertia.back();
    }

    @GET
    @Path("preserve-state")
    @Blocking
    public Uni<Object> preserveState() {
        return inertia.render("Features/Navigation/PreserveState", Map.of(
            "serverCounter", Demo.randomInt(1, 1000),
            "timestamp", Demo.now()));
    }

    @GET
    @Path("preserve-scroll")
    @Blocking
    public Uni<Object> preserveScroll() {
        return inertia.render("Features/Navigation/PreserveScroll", Map.of(
            "timestamp", Demo.now()));
    }

    @GET
    @Path("view-transitions")
    @Blocking
    public Uni<Object> viewTransitions() {
        return inertia.render("Features/Navigation/ViewTransitions");
    }

    @GET
    @Path("history-management")
    @Blocking
    public Uni<Object> historyManagement(@QueryParam("visit") @DefaultValue("0") int visit) {
        return inertia.render("Features/Navigation/HistoryManagement", Map.of(
            "visit", visit,
            "timestamp", Demo.now()));
    }

    @POST
    @Path("history-management")
    @Blocking
    public Uni<Object> historyAction() {
        return inertia.redirect("/features/navigation/history-management");
    }

    @GET
    @Path("async-requests")
    @Blocking
    public Uni<Object> asyncRequests(@Context RoutingContext rc) {
        if (rc.request().getHeader("X-Inertia") != null) {
            Demo.sleepSeconds(1);
        }
        return inertia.render("Features/Navigation/AsyncRequests", Map.of(
            "timestamp", Demo.now()));
    }

    @GET
    @Path("async-slow")
    @Blocking
    public Uni<Object> asyncSlow(@QueryParam("delay") @DefaultValue("2") int delay) {
        Demo.sleepSeconds(Math.min(delay, 5));
        return inertia.render("Features/Navigation/AsyncRequests", Map.of(
            "timestamp", Demo.now()));
    }

    @GET
    @Path("instant-visits")
    @Blocking
    public Uni<Object> instantVisits() {
        return inertia.render("Features/Navigation/InstantVisits", Map.of(
            "sourceTimestamp", Demo.now(),
            "message", "This is the source page."));
    }

    @GET
    @Path("instant-visit-target")
    @Blocking
    public Uni<Object> instantVisitTarget(@QueryParam("delay") @DefaultValue("2") int delay) {
        Demo.sleepSeconds(Math.min(delay, 5));
        return inertia.render("Features/Navigation/InstantVisitTarget", Map.of(
            "greeting", "Hello from the server!",
            "serverTimestamp", Demo.now(),
            "items", items(3)));
    }

    @GET
    @Path("manual-visits")
    @Blocking
    public Uni<Object> manualVisits() {
        return inertia.render("Features/Navigation/ManualVisits", Map.of(
            "timestamp", Demo.now(),
            "counter", Demo.randomInt(1, 1000)));
    }

    @GET
    @Path("redirects")
    @Blocking
    public Uni<Object> redirectDemo() {
        return inertia.render("Features/Navigation/Redirects", Map.of(
            "timestamp", Demo.now()));
    }

    @POST
    @Path("redirects/back")
    @Blocking
    public Uni<Object> redirectStandard() {
        inertia.flash("message", "Redirected back via redirect()->back()");
        return inertia.back();
    }

    @POST
    @Path("redirects/to-route")
    @Blocking
    public Uni<Object> redirectToRoute() {
        inertia.flash("message", "Redirected via to_route()");
        return inertia.redirect("/features/navigation/redirects");
    }

    @POST
    @Path("redirects/external")
    @Blocking
    public Uni<Object> redirectExternal() {
        return inertia.location("https://cloud.laravel.com");
    }

    @GET
    @Path("scroll-management")
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

    @GET
    @Path("url-fragments")
    @Blocking
    public Uni<Object> urlFragments() {
        return inertia.render("Features/Navigation/UrlFragments", Map.of(
            "timestamp", Demo.now()));
    }

    @GET
    @Path("url-fragments/redirect-hash")
    @Blocking
    public Uni<Object> redirectWithHashGet() {
        return redirectWithHash();
    }

    @POST
    @Path("url-fragments/redirect-hash")
    @Blocking
    public Uni<Object> redirectWithHashPost() {
        return redirectWithHash();
    }

    @Blocking
    private Uni<Object> redirectWithHash() {
        return inertia.redirect("/features/navigation/url-fragments#server-section");
    }

    @GET
    @Path("url-fragments/preserve-redirect")
    @Blocking
    public Uni<Object> preserveFragmentRedirect() {
        inertia.preserveFragment(true);
        return inertia.redirect("/features/navigation/url-fragments/preserve-target");
    }

    @GET
    @Path("url-fragments/preserve-target")
    @Blocking
    public Uni<Object> preserveFragmentTarget() {
        return inertia.render("Features/Navigation/UrlFragments", Map.of(
            "timestamp", Demo.now(),
            "redirectedFrom", "preserveFragment redirect"));
    }

    private java.util.List<Map<String, Object>> items(int count) {
        var items = new ArrayList<Map<String, Object>>();
        for (int i = 1; i <= count; i++) {
            items.add(Map.of("id", i, "name", "Server Item " + (char) ('A' + i - 1)));
        }
        return items;
    }
}
