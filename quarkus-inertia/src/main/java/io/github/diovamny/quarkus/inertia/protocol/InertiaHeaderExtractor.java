package io.github.diovamny.quarkus.inertia.protocol;

import java.util.function.Function;
import jakarta.enterprise.context.ApplicationScoped;
import io.vertx.core.Context;

/**
 * Captures the Inertia request headers of the current request into the
 * Vert.x context so the response processors can build the page object
 * without depending on the transport (JAX-RS or reactive routes).
 *
 * <p>Used by both {@link InertiaRequestFilter} (JAX-RS) and the reactive
 * routes pre-handler; the extraction is idempotent and safe to run twice.</p>
 */
@ApplicationScoped
public class InertiaHeaderExtractor {

    /**
     * Copy the Inertia headers into the given Vert.x context.
     *
     * @param ctx        the Vert.x context of the current request
     * @param method     the HTTP method
     * @param requestUri the request URI (absolute)
     * @param headers    case-insensitive header lookup
     */
    public void extract(Context ctx, String method, String requestUri, Function<String, String> headers) {
        var inertiaHeader = headers.apply("X-Inertia");
        InertiaContextLocals.put(ctx, "inertia-request",
            "true".equalsIgnoreCase(inertiaHeader) || Boolean.parseBoolean(inertiaHeader));

        InertiaContextLocals.put(ctx, "request-method", method);

        InertiaContextLocals.put(ctx, "request-uri", requestUri);

        put(ctx, "inertia-version", headers.apply("X-Inertia-Version"));

        put(ctx, "inertia-partial-component", headers.apply("X-Inertia-Partial-Component"));

        put(ctx, "inertia-partial-data", headers.apply("X-Inertia-Partial-Data"));

        put(ctx, "inertia-partial-except", headers.apply("X-Inertia-Partial-Except"));

        put(ctx, "inertia-reset", headers.apply("X-Inertia-Reset"));

        put(ctx, "inertia-except-once-props", headers.apply("X-Inertia-Except-Once-Props"));

        put(ctx, "inertia-error-bag", headers.apply("X-Inertia-Error-Bag"));

        put(ctx, "inertia-scroll-merge-intent", headers.apply("X-Inertia-Infinite-Scroll-Merge-Intent"));

        var precognition = headers.apply("Precognition");
        if (precognition != null) {
            InertiaContextLocals.put(ctx, "inertia-precognition", "true".equalsIgnoreCase(precognition));
        } else {
            InertiaContextLocals.put(ctx, "inertia-precognition", Boolean.FALSE);
        }

        put(ctx, "inertia-precognition-validate-fields", headers.apply("Precognition-Validate-Only"));

        var purpose = headers.apply("Purpose");
        InertiaContextLocals.put(ctx, "inertia-prefetch", "prefetch".equalsIgnoreCase(purpose));

        var legacyPrefetch = headers.apply("X-Inertia-Prefetch");
        if (legacyPrefetch != null) {
            InertiaContextLocals.put(ctx, "inertia-prefetch", "true".equalsIgnoreCase(legacyPrefetch));
        }

        put(ctx, "referer-url", headers.apply("Referer"));
    }

    /**
     * Extract headers binding them to an explicit routing context (reactive
     * routes path). The routing context is the authoritative store; values
     * are mirrored to the Vert.x context for worker threads.
     *
     * @param rc routing context of the current request
     * @param ctx current Vert.x context, may be {@code null}
     * @param method HTTP method
     * @param requestUri request URI
     * @param headers header lookup
     */
    public void extract(io.vertx.ext.web.RoutingContext rc, Context ctx,
            String method, String requestUri, Function<String, String> headers) {
        if (rc != null && ctx != null) {
            ctx.putLocal(InertiaContextLocals.ROUTING_CONTEXT_KEY, rc);
            rc.put(InertiaContextLocals.ROUTING_CONTEXT_KEY, rc);
        } else if (rc != null) {
            rc.put(InertiaContextLocals.ROUTING_CONTEXT_KEY, rc);
        }
        extract(ctx != null ? ctx : null, method, requestUri, headers);
        if (rc != null) {
            // Ensure every extracted value is also visible via rc.get(),
            // even when ctx was null (worker thread edge case).
            mirrorToRoutingContext(rc, ctx, method, requestUri, headers);
        }
    }

    private void mirrorToRoutingContext(io.vertx.ext.web.RoutingContext rc, Context ctx,
            String method, String requestUri, Function<String, String> headers) {
        var inertiaHeader = headers.apply("X-Inertia");
        rc.put("inertia-request", "true".equalsIgnoreCase(inertiaHeader)
            || Boolean.parseBoolean(inertiaHeader));
        rc.put("request-method", method);
        rc.put("request-uri", requestUri);
        putRc(rc, "inertia-version", headers.apply("X-Inertia-Version"));
        putRc(rc, "inertia-partial-component", headers.apply("X-Inertia-Partial-Component"));
        putRc(rc, "inertia-partial-data", headers.apply("X-Inertia-Partial-Data"));
        putRc(rc, "inertia-partial-except", headers.apply("X-Inertia-Partial-Except"));
        putRc(rc, "inertia-reset", headers.apply("X-Inertia-Reset"));
        putRc(rc, "inertia-except-once-props", headers.apply("X-Inertia-Except-Once-Props"));
        putRc(rc, "inertia-error-bag", headers.apply("X-Inertia-Error-Bag"));
        putRc(rc, "inertia-scroll-merge-intent",
            headers.apply("X-Inertia-Infinite-Scroll-Merge-Intent"));
        var precognition = headers.apply("Precognition");
        if (precognition != null) {
            rc.put("inertia-precognition", "true".equalsIgnoreCase(precognition));
        } else {
            rc.put("inertia-precognition", Boolean.FALSE);
        }
        putRc(rc, "inertia-precognition-validate-fields", headers.apply("Precognition-Validate-Only"));
        var purpose = headers.apply("Purpose");
        rc.put("inertia-prefetch", "prefetch".equalsIgnoreCase(purpose));
        var legacyPrefetch = headers.apply("X-Inertia-Prefetch");
        if (legacyPrefetch != null) {
            rc.put("inertia-prefetch", "true".equalsIgnoreCase(legacyPrefetch));
        }
        putRc(rc, "referer-url", headers.apply("Referer"));
    }

    private static void putRc(io.vertx.ext.web.RoutingContext rc, String key, String value) {
        if (value != null) {
            rc.put(key, value);
        } else {
            rc.remove(key);
        }
    }

    private static void put(Context ctx, String key, String value) {
        if (ctx == null) {
            return;
        }
        if (value != null) {
            InertiaContextLocals.put(ctx, key, value);
        } else {
            // Remove stale values from previous requests on the same event loop.
            io.vertx.ext.web.RoutingContext rc = InertiaContextLocals.routingContext(ctx);
            if (rc != null) {
                rc.remove(key);
            }
            try {
                ctx.removeLocal(key);
            } catch (Exception ignored) {
                ctx.putLocal(key, null);
            }
        }
    }
}
