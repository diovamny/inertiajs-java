package io.github.diovamny.quarkus.inertia.protocol;

import io.vertx.core.Context;
import io.vertx.ext.web.RoutingContext;

/**
 * Request-isolated access to Inertia per-request state (G-17, hardened M7).
 *
 * <p>Root cause fixed here: {@code Vertx.currentContext()} locals are scoped
 * to the event-loop {@link Context}, which is shared across sequential and
 * pipelined requests on the same loop — and visible to every
 * {@code @Blocking} worker thread. Storing request headers there leaks state
 * between concurrent requests (partial-component, version, precognition
 * flags, except-once-props). Proven by
 * {@code ReactiveStressTest} (M7): concurrent reactive visits cross-read
 * {@code X-Inertia-Except-Once-Props}.</p>
 *
 * <p>Contract: resolution order is (1) the request-scoped
 * {@link RequestRoutingContext} holder (Quarkus propagates it to
 * {@code @Blocking} worker threads), (2) the Vert.x context local (event-loop
 * synchronous paths and unit tests), (3) {@code CurrentVertxRequest}, (4) the
 * CDI {@link RoutingContext}. Every {@code put} writes to the resolved
 * routing context first (the only truly request-scoped map) and mirrors to
 * the Vert.x event-loop context. Every {@code get} prefers the routing
 * context; the shared-context fallback is only honored on event-loop
 * contexts, never on worker threads.</p>
 */
public final class InertiaContextLocals {

    /** Key under which the routing context is stored in the Vert.x context. */
    public static final String ROUTING_CONTEXT_KEY = "inertia-routing-context";

    private InertiaContextLocals() {
    }

    /**
     * Resolve the routing context for the current request, if any.
     *
     * <p>Order: request-scoped holder (correct on event loop and workers when
     * the request scope is active), Vert.x context local (synchronous paths
     * and unit tests), {@code CurrentVertxRequest}.</p>
     *
     * <p>There is deliberately no blind CDI {@link RoutingContext} fallback:
     * without an active request its lookup returns an unusable proxy that
     * explodes on first use. Callers that can tolerate it (registries) keep
     * their own guarded fallbacks.</p>
     *
     * @param ctx current Vert.x context, may be {@code null}
     * @return routing context or {@code null}
     */
    public static RoutingContext routingContext(Context ctx) {
        var held = requestScopedRoutingContext();
        if (held != null) {
            return held;
        }
        if (ctx != null) {
            Object stored = ctx.getLocal(ROUTING_CONTEXT_KEY);
            if (stored instanceof RoutingContext rc) {
                return rc;
            }
        }
        return currentVertxRequestRoutingContext();
    }

    /**
     * Read a per-request value, preferring the routing context.
     *
     * <p>The shared event-loop fallback is skipped on worker threads: they
     * must never read the shared map (concurrent requests overwrite each
     * other's entries there). Mocks and virtual-thread contexts keep the
     * legacy fallback.</p>
     *
     * @param ctx current Vert.x context, may be {@code null}
     * @param key local key
     * @return value or {@code null}
     */
    public static Object get(Context ctx, String key) {
        RoutingContext rc = routingContext(ctx);
        if (rc != null) {
            Object fromRc = rc.get(key);
            if (fromRc != null) {
                return fromRc;
            }
        }
        if (ctx == null || ctx.isWorkerContext()) {
            return null;
        }
        return ctx.getLocal(key);
    }

    /**
     * Read a per-request {@link String} value.
     *
     * @param ctx current Vert.x context, may be {@code null}
     * @param key local key
     * @return value or {@code null}
     */
    public static String getString(Context ctx, String key) {
        Object value = get(ctx, key);
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * Check a per-request boolean flag.
     *
     * @param ctx current Vert.x context, may be {@code null}
     * @param key local key
     * @return {@code true} when flag is {@link Boolean#TRUE}
     */
    public static boolean isTrue(Context ctx, String key) {
        return Boolean.TRUE.equals(get(ctx, key));
    }

    /**
     * Write a per-request value to both the routing context (authoritative)
     * and the Vert.x context (fallback for event-loop readers).
     *
     * <p>A {@code null} value removes the key from both stores, so a request
     * without a header never inherits a concurrent request's value.</p>
     *
     * @param ctx current Vert.x context, may be {@code null}
     * @param key local key
     * @param value value to store, {@code null} removes from both stores
     */
    public static void put(Context ctx, String key, Object value) {
        RoutingContext rc = routingContext(ctx);
        if (rc != null) {
            if (value == null) {
                rc.remove(key);
            } else {
                rc.put(key, value);
            }
        }
        if (ctx != null) {
            if (value == null) {
                try {
                    ctx.removeLocal(key);
                } catch (Exception ignored) {
                    try {
                        ctx.putLocal(key, null);
                    } catch (Exception alsoIgnored) {
                        // Best effort only.
                    }
                }
            } else {
                ctx.putLocal(key, value);
            }
        }
    }

    /**
     * Write a value known to belong to an explicit routing context (reactive
     * pre-handler path). Mirrors to the Vert.x context when present.
     *
     * @param rc routing context, must not be {@code null}
     * @param ctx current Vert.x context, may be {@code null}
     * @param key local key
     * @param value value to store
     */
    public static void put(RoutingContext rc, Context ctx, String key, Object value) {
        if (value == null) {
            rc.remove(key);
            return;
        }
        rc.put(key, value);
        if (ctx != null) {
            ctx.putLocal(key, value);
        }
    }

    /**
     * Read a value for an explicit routing context, falling back to the
     * resolved routing context and then to Vert.x context (never on worker
     * threads).
     *
     * @param rc routing context, may be {@code null}
     * @param ctx current Vert.x context, may be {@code null}
     * @param key local key
     * @return value or {@code null}
     */
    public static Object get(RoutingContext rc, Context ctx, String key) {
        if (rc != null) {
            Object fromRc = rc.get(key);
            if (fromRc != null) {
                return fromRc;
            }
        }
        if (rc == null) {
            var resolved = routingContext(ctx);
            if (resolved != null) {
                Object fromResolved = resolved.get(key);
                if (fromResolved != null) {
                    return fromResolved;
                }
            }
        }
        if (ctx == null || ctx.isWorkerContext()) {
            return null;
        }
        return ctx.getLocal(key);
    }

    private static RoutingContext requestScopedRoutingContext() {
        try {
            var holder = jakarta.enterprise.inject.spi.CDI.current()
                .select(RequestRoutingContext.class).get();
            if (holder != null && holder.hasRoutingContext()) {
                return holder.getRoutingContext();
            }
        } catch (Exception ignored) {
            // No CDI container (plain unit tests): fall through.
        }
        return null;
    }

    private static RoutingContext currentVertxRequestRoutingContext() {
        try {
            var current = jakarta.enterprise.inject.spi.CDI.current()
                .select(io.quarkus.vertx.http.runtime.CurrentVertxRequest.class).get();
            if (current != null) {
                var rc = current.getCurrent();
                if (rc != null) {
                    return rc;
                }
            }
        } catch (Exception ignored) {
            // No CDI container, no active request, or producer refusal
            // (plain unit tests): fall through.
        }
        return null;
    }
}
