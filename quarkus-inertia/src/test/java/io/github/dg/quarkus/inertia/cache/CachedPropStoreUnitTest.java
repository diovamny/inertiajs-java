package io.github.dg.quarkus.inertia.cache;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import io.smallrye.mutiny.Uni;

class CachedPropStoreUnitTest {

    @Test
    void computesOnceAndReusesCachedValue() {
        var store = new CachedPropStore();
        var calls = new AtomicInteger();

        var first = store.compute("stats", null, () -> {
            calls.incrementAndGet();
            return Uni.createFrom().item("value-1");
        }).await().indefinitely();
        var second = store.compute("stats", null, () -> {
            calls.incrementAndGet();
            return Uni.createFrom().item("value-2");
        }).await().indefinitely();

        assertThat(first).isEqualTo("value-1");
        assertThat(second).isEqualTo("value-1");
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void cachesByKeyIndependently() {
        var store = new CachedPropStore();
        var aCalls = new AtomicInteger();
        var bCalls = new AtomicInteger();

        store.compute("a", null, () -> {
            aCalls.incrementAndGet();
            return Uni.createFrom().item(1);
        }).await().indefinitely();
        store.compute("b", null, () -> {
            bCalls.incrementAndGet();
            return Uni.createFrom().item(2);
        }).await().indefinitely();

        store.compute("a", null, () -> {
            aCalls.incrementAndGet();
            return Uni.createFrom().item(99);
        }).await().indefinitely();

        assertThat(aCalls.get()).isEqualTo(1);
        assertThat(bCalls.get()).isEqualTo(1);
    }

    @Test
    void reEvaluatesAfterTtlExpires() {
        var store = new CachedPropStore();
        var calls = new AtomicInteger();

        store.compute("stats", Duration.ofSeconds(1), () -> {
            calls.incrementAndGet();
            return Uni.createFrom().item("fresh");
        }).await().indefinitely();

        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        store.compute("stats", Duration.ofSeconds(1), () -> {
            calls.incrementAndGet();
            return Uni.createFrom().item("stale");
        }).await().indefinitely();

        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void evictRemovesEntry() {
        var store = new CachedPropStore();
        var calls = new AtomicInteger();

        store.compute("k", null, () -> {
            calls.incrementAndGet();
            return Uni.createFrom().item("x");
        }).await().indefinitely();

        store.evict("k");
        store.compute("k", null, () -> {
            calls.incrementAndGet();
            return Uni.createFrom().item("y");
        }).await().indefinitely();

        assertThat(calls.get()).isEqualTo(2);
    }
}
