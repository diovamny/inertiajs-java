package com.example.pingcrm.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import com.quarkus.inertia.api.Inertia;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/")

@Blocking
public class DashboardController {

    @Inject
    Inertia inertia;

    @GET
    @Blocking
    public Uni<Object> index() {
        return inertia.render("Dashboard/Index");
    }
}
