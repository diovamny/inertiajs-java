package com.example.kitchensink.controller.feature;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

import com.example.kitchensink.service.StockSimulator;
import io.github.diovamny.quarkus.inertia.api.Inertia;

/**
 * Simulates the external broadcast producer for the Live Props demo: mutates
 * the in-memory stock value and redirects back so the next render resolves the
 * live prop with fresh data.
 */
@Path("/api/simulate-stock-update")
@Blocking
public class StockSimulationResource {

    @Inject
    Inertia inertia;

    @Inject
    StockSimulator stock;

    @POST
    public Uni<Object> simulate() {
        stock.increment();
        return inertia.redirect("/features/live-props");
    }
}
