package com.example.kitchensink.controller.feature;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

import com.example.kitchensink.service.StockSimulator;
import io.github.diovamny.inertia.core.model.LiveChannel;
import io.github.diovamny.inertia.core.model.LivePropListener;
import io.github.diovamny.quarkus.inertia.api.Inertia;

/**
 * Live Props demo: {@code stockCount} is resolved on every full/selected
 * partial visit and carries the wire metadata the official client needs to
 * subscribe to the private {@code inventory} channel.
 */
@Path("/features/live-props")
@Blocking
public class LivePropsController {

    private static final int THROTTLE_MS = 2000;

    @Inject
    Inertia inertia;

    @Inject
    StockSimulator stock;

    @GET
    public Uni<Object> index() {
        inertia.live("stockCount", () -> Uni.createFrom().item((Object) stock.current()),
            THROTTLE_MS,
            new LivePropListener(LiveChannel.privateChannel("inventory"), "StockUpdated"));
        return inertia.render("Features/LiveProps/Index", Map.of(
            "channelName", "inventory",
            "channelType", "private",
            "throttleMs", THROTTLE_MS));
    }
}
