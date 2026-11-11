package com.example.kitchensink.service;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * In-memory stock counter backing the Live Props demo. The endpoint
 * {@code POST /api/simulate-stock-update} plays the role of the external
 * broadcast producer: it mutates the value the live prop resolver reads.
 */
@ApplicationScoped
public class StockSimulator {

    private final AtomicInteger stock = new AtomicInteger(24);

    /**
     * Current stock value read by the live-prop resolver.
     *
     * @return the current count
     */
    public int current() {
        return stock.get();
    }

    /**
     * Simulate an inventory broadcast by incrementing the counter.
     *
     * @return the new count
     */
    public int increment() {
        return stock.incrementAndGet();
    }
}
