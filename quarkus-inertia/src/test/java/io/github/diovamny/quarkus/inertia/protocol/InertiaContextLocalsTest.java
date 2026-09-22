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
    void staleVertxLocalNeverOverridesRoutingContext() {
        // Previous session's value lingers in the shared event-loop context;
        // the fresh routing context must win (G-17 session-change case).
        var ctx = mock(io.vertx.core.Context.class);
        var rc = mock(io.vertx.ext.web.RoutingContext.class);
        when(ctx.getLocal(InertiaContextLocals.ROUTING_CONTEXT_KEY)).thenReturn(rc);
        when(rc.get("inertia-partial-component")).thenReturn("Fresh/Page");
        when(ctx.getLocal("inertia-partial-component")).thenReturn("Stale/Page");

        assertEquals("Fresh/Page", InertiaContextLocals.get(ctx, "inertia-partial-component"));
    }

    @Test
    void explicitRoutingContextsStayIsolatedAcrossSessions() {
        // Two concurrent sessions sharing one event-loop context: each
        // explicit routing context reads back its own value.
        var ctx = mock(io.vertx.core.Context.class);
        var rcA = mock(io.vertx.ext.web.RoutingContext.class);
        var rcB = mock(io.vertx.ext.web.RoutingContext.class);
        Map<String, Object> storeA = new HashMap<>();
        Map<String, Object> storeB = new HashMap<>();
        when(rcA.get("inertia-version")).thenAnswer(inv -> storeA.get("inertia-version"));
        when(rcB.get("inertia-version")).thenAnswer(inv -> storeB.get("inertia-version"));
        org.mockito.Mockito.doAnswer(inv -> {
            storeA.put("inertia-version", inv.getArgument(1));
            return null;
        }).when(rcA).put(org.mockito.ArgumentMatchers.eq("inertia-version"),
            org.mockito.ArgumentMatchers.any());
        org.mockito.Mockito.doAnswer(inv -> {
            storeB.put("inertia-version", inv.getArgument(1));
            return null;
        }).when(rcB).put(org.mockito.ArgumentMatchers.eq("inertia-version"),
            org.mockito.ArgumentMatchers.any());

        InertiaContextLocals.put(rcA, ctx, "inertia-version", "version-A");
        InertiaContextLocals.put(rcB, ctx, "inertia-version", "version-B");

        assertEquals("version-A", InertiaContextLocals.get(rcA, ctx, "inertia-version"));
        assertEquals("version-B", InertiaContextLocals.get(rcB, ctx, "inertia-version"));
    }

    @Test
    void concurrentSessionsNeverContaminate() throws Exception {
        // 20 sessions x 100 round-trips on the explicit (rc, ctx) path: no
        // static shared state may leak a value across sessions.
        int sessions = 20;
        int iterations = 100;
        var pool = Executors.newFixedThreadPool(sessions);
        var latch = new CountDownLatch(sessions * iterations);
        var failed = new AtomicBoolean(false);
        for (int t = 0; t < sessions; t++) {
            final String expected = "session-" + t;
            for (int i = 0; i < iterations; i++) {
                pool.submit(() -> {
                    try {
                        var ctx = mock(io.vertx.core.Context.class);
                        var rc = mock(io.vertx.ext.web.RoutingContext.class);
                        Map<String, Object> store = new HashMap<>();
                        when(rc.get("inertia-once-nonce")).thenAnswer(inv -> store.get("inertia-once-nonce"));
                        org.mockito.Mockito.doAnswer(inv -> {
                            store.put("inertia-once-nonce", inv.getArgument(1));
                            return null;
                        }).when(rc).put(org.mockito.ArgumentMatchers.eq("inertia-once-nonce"),
                            org.mockito.ArgumentMatchers.any());
                        InertiaContextLocals.put(rc, ctx, "inertia-once-nonce", expected);
                        Object actual = InertiaContextLocals.get(rc, ctx, "inertia-once-nonce");
                        if (!expected.equals(actual)) {
                            failed.set(true);
                        }
                    } catch (Exception e) {
                        failed.set(true);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }
        assertTrue(latch.await(60, TimeUnit.SECONDS));
        pool.shutdownNow();
        assertTrue(!failed.get(), "per-session values leaked across sessions");
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
