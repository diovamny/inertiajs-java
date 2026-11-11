package com.example.kitchensink.controller.feature;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kitchensink.service.StockSimulator;
import io.github.diovamny.inertia.core.model.LiveChannel;
import io.github.diovamny.inertia.core.model.LivePropListener;
import io.github.diovamny.spring.inertia.api.Inertia;

/**
 * Live Props demo: {@code stockCount} is resolved on every full/selected
 * partial visit and carries the wire metadata the official client needs to
 * subscribe to the private {@code inventory} channel.
 */
@RestController
public class LivePropsController {

    private static final int THROTTLE_MS = 2000;

    private final Inertia inertia;
    private final StockSimulator stock;

    public LivePropsController(Inertia inertia, StockSimulator stock) {
        this.inertia = inertia;
        this.stock = stock;
    }

    @GetMapping("/features/live-props")
    public Object index() {
        inertia.live("stockCount", stock::current, THROTTLE_MS,
            new LivePropListener(LiveChannel.privateChannel("inventory"), "StockUpdated"));
        return inertia.render("Features/LiveProps/Index", Map.of(
            "channelName", "inventory",
            "channelType", "private",
            "throttleMs", THROTTLE_MS));
    }

    @PostMapping("/api/simulate-stock-update")
    public Object simulateStockUpdate() {
        stock.increment();
        return inertia.redirect("/features/live-props");
    }
}
