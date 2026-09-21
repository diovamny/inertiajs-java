package io.github.diovamny.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * G-17 regression: per-request state must prefer the routing context over the
 * shared event-loop context, so concurrent requests never contaminate each
 * other.
 */
class InertiaContextLocalsTest {

    @Test
    void prefersRoutingContextOverVertxContext() {
        var ctx = mock(io.vertx.core.Context.class);
        var rc = mock(io.vertx.ext.web.RoutingContext.class);
        when(ctx.getLocal(InertiaContextLocals.ROUTING_CONTEXT_KEY)).thenReturn(rc);
        when(rc.get("inertia-partial-component")).thenReturn("Contacts/Index");
        when(ctx.getLocal("inertia-partial-component")).thenReturn("Stale/Component");

        assertEquals("Contacts/Index", InertiaContextLocals.get(ctx, "inertia-partial-component"));
        assertTrue(InertiaContextLocals.isTrue(ctx, "inertia-request") == false);
    }

    @Test
    void fallsBackToVertxContextWhenRoutingContextAbsent() {
        var ctx = mock(io.vertx.core.Context.class);
        when(ctx.getLocal(InertiaContextLocals.ROUTING_CONTEXT_KEY)).thenReturn(null);
        when(ctx.getLocal("inertia-version")).thenReturn("abc123");

        assertEquals("abc123", InertiaContextLocals.getString(ctx, "inertia-version"));
    }

    @Test
    void putMirrorsToBothStores() {
        var ctx = mock(io.vertx.core.Context.class);
        var rc = mock(io.vertx.ext.web.RoutingContext.class);
        when(ctx.getLocal(InertiaContextLocals.ROUTING_CONTEXT_KEY)).thenReturn(rc);
        Map<String, Object> rcStore = new HashMap<>();
        when(rc.get("request-method")).thenAnswer(inv -> rcStore.get("request-method"));
        org.mockito.Mockito.doAnswer(inv -> {
            rcStore.put("request-method", inv.getArgument(1));
            return null;
        }).when(rc).put(org.mockito.ArgumentMatchers.eq("request-method"),
            org.mockito.ArgumentMatchers.any());

        InertiaContextLocals.put(ctx, "request-method", "POST");

        org.mockito.Mockito.verify(ctx).putLocal("request-method", "POST");
        org.mockito.Mockito.verify(rc).put("request-method", "POST");
    }

    @Test
    void concurrentMergesNeverContaminate() throws Exception {
        int threads = 20;
        int iterations = 100;
        var pool = Executors.newFixedThreadPool(threads);
        var latch = new CountDownLatch(threads * iterations);
        var failed = new AtomicBoolean(false);
        for (int t = 0; t < threads; t++) {
            final String token = "Token-" + t;
            for (int i = 0; i < iterations; i++) {
                pool.submit(() -> {
                    try {
                        var merged = io.github.diovamny.quarkus.inertia.util.VaryHeaderUtil.merge(
                            "Accept-Encoding", token, "X-Inertia");
                        if (!merged.contains(token) || !merged.contains("Accept-Encoding")) {
                            failed.set(true);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        pool.shutdownNow();
        assertTrue(!failed.get());
    }
}
