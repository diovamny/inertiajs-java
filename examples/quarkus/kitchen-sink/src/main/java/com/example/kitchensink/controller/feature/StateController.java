package com.example.kitchensink.controller.feature;

import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

import io.github.diovamny.quarkus.inertia.api.Inertia;

@Path("/features/state")
public class StateController {

    @Inject
    Inertia inertia;

    @GET
    @Path("remember")
    @Blocking
    public Uni<Object> remember() {
        return inertia.render("Features/State/Remember");
    }

    @GET
    @Path("flash-data")
    @Blocking
    public Uni<Object> flashData() {
        return inertia.render("Features/State/FlashData");
    }

    @POST
    @Path("flash-data")
    @Blocking
    public Uni<Object> storeFlashData() {
        inertia.flash("message", "This is a flash message from the server!");
        inertia.flash("type", "success");
        return inertia.back();
    }

    @POST
    @Path("flash-data/error")
    @Blocking
    public Uni<Object> storeFlashDataError() {
        inertia.flash("message", "Something went wrong!");
        inertia.flash("type", "error");
        return inertia.back();
    }

    @POST
    @Path("flash-data/warning")
    @Blocking
    public Uni<Object> storeFlashDataWarning() {
        inertia.flash("message", "Please check your input.");
        inertia.flash("type", "warning");
        return inertia.back();
    }

    @GET
    @Path("shared-props")
    @Blocking
    public Uni<Object> sharedProps() {
        return inertia.render("Features/State/SharedProps");
    }
}
