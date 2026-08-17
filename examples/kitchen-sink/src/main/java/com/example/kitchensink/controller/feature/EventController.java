package com.example.kitchensink.controller.feature;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import com.example.kitchensink.service.Demo;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.github.dg.quarkus.inertia.api.Inertia;

@Path("/features/events")
public class EventController {

    @Inject
    Inertia inertia;

    @GET
    @Path("global-events")
    @Blocking
    public Uni<Object> globalEvents() {
        return inertia.render("Features/Events/GlobalEvents");
    }

@POST
    @Path("global-events/action")
    @Blocking
    public Uni<Object> globalEventsAction() {
        inertia.flash("message", "Action completed successfully!");
        return inertia.back();
    }

    @GET
    @Path("once-events")
    @Blocking
    public Uni<Object> onceEvents() {
        return inertia.render("Features/Events/OnceEvents");
    }

    @POST
    @Path("once-events")
    @Blocking
    public Uni<Object> onceEventsAction() {
        inertia.flash("message", "One-shot action fired!");
        return inertia.back();
    }

    @GET
    @Path("visit-callbacks")
    @Blocking
    public Uni<Object> visitCallbacks() {
        return inertia.render("Features/Events/VisitCallbacks");
    }

@POST
    @Path("visit-callbacks/action")
    @Blocking
    public Uni<Object> visitCallbacksAction() {
        inertia.flash("message", "Visit callback action completed!");
        return inertia.back();
    }

    @GET
    @Path("progress")
    @Blocking
    public Uni<Object> progress() {
        return inertia.render("Features/Events/Progress");
    }

@GET
    @Path("progress/slow")
    @Blocking
    public Uni<Object> progressSlow() {
        Demo.sleepSeconds(2);
        return inertia.render("Features/Events/Progress");
    }

    @GET
    @Path("location-event")
    @Blocking
    public Uni<Object> locationEvent() {
        return inertia.render("Features/Events/LocationEvent");
    }

    @GET
    @Path("location-event/deploy")
    @Blocking
    public Uni<Object> deployNewVersion() {
        inertia.version("new-asset-version-hash");
        return inertia.back();
    }
}
