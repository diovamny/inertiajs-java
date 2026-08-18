package com.example.kitchensink.controller.feature;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.github.dg.quarkus.inertia.api.Inertia;

@Path("/features/layouts")
public class LayoutController {

    @Inject
    Inertia inertia;

    @GET
    @Path("persistent-layouts")
    @Blocking
    public Uni<Object> persistentLayouts() {
        return inertia.render("Features/Layouts/PersistentLayouts");
    }

    @GET
    @Path("persistent-layouts/page-2")
    @Blocking
    public Uni<Object> persistentLayoutsPageTwo() {
        return inertia.render("Features/Layouts/PersistentLayoutsPageTwo");
    }

    @GET
    @Path("nested-layouts")
    @Blocking
    public Uni<Object> nestedLayouts() {
        return inertia.render("Features/Layouts/NestedLayouts");
    }

    @GET
    @Path("head")
    @Blocking
    public Uni<Object> head() {
        return inertia.render("Features/Layouts/Head");
    }

    @GET
    @Path("layout-props")
    @Blocking
    public Uni<Object> layoutProps() {
        return inertia.render("Features/Layouts/LayoutProps");
    }
}
