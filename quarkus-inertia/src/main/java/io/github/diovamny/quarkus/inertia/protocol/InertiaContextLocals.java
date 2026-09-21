package io.github.diovamny.quarkus.inertia.protocol;

import io.vertx.core.Context;
import io.vertx.ext.web.RoutingContext;

/**
 * Request-isolated access to Inertia per-request state (G-17).
 *
 * <p>Root cause fixed here: {@code Vertx.currentContext()} locals are scoped
 * to the event-loop {@link Context}, which is shared across sequential and
 * pipelined requests on the same loop. Storing request headers there leaks
 * state between concurrent requests (partial-component, version, precognition
 * flags).</p>
 *
 * <p>Contract: every {@code put} writes to the per-request
 * {@link RoutingContext} first (the only truly request-scoped map) and mirrors
 * to the Vert.x context for {@code @Blocking} worker threads that cannot see
 * the routing context directly. Every {@code get} prefers the routing context
 * when it can be resolved, falling back to the Vert.x context for JAX-RS and
 * unit-test paths.</p>
 */
public final class InertiaContextLocals {

    /** Key under which the routing context is stored in the Vert.x context. */
    public static final String ROUTING_CONTEXT_KEY = "inertia-routing-context";

    private InertiaContextLocals() {
    }

    /**
     * Resolve the routing context for the current request, if any.
     *
     * @param ctx current Vert.x context, may be {@code null}
     * @return routing context or {@code null}
     */
    public static RoutingContext routingContext(Context ctx) {
        if (ctx == null) {
            return null;
        }
        Object stored = ctx.getLocal(ROUTING_CONTEXT_KEY);
        if (stored instanceof RoutingContext rc) {
            return rc;
        }
        return null;
    }

    /**
     * Read a per-request value, preferring the routing context.
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
        if (ctx == null) {
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
     * and the Vert.x context (fallback for worker threads).
     *
     * @param ctx current Vert.x context, may be {@code null}
     * @param key local key
     * @param value value to store, {@code null} removes from routing context
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
        if (ctx != null && value != null) {
            ctx.putLocal(key, value);
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
     * Read a value for an explicit routing context, falling back to Vert.x
     * context.
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
        if (ctx == null) {
            return null;
        }
        return ctx.getLocal(key);
    }
}
