package com.quarkus.inertia.protocol;

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
        ctx.putLocal("inertia-request", "true".equalsIgnoreCase(inertiaHeader)
            || Boolean.parseBoolean(inertiaHeader));

        ctx.putLocal("request-method", method);

        ctx.putLocal("request-uri", requestUri);

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
            ctx.putLocal("inertia-precognition", "true".equalsIgnoreCase(precognition));
        }

        put(ctx, "inertia-precognition-validate-fields", headers.apply("Precognition-Validate-Only"));

        var purpose = headers.apply("Purpose");
        ctx.putLocal("inertia-prefetch", "prefetch".equalsIgnoreCase(purpose));

        var legacyPrefetch = headers.apply("X-Inertia-Prefetch");
        if (legacyPrefetch != null) {
            ctx.putLocal("inertia-prefetch", "true".equalsIgnoreCase(legacyPrefetch));
        }

        put(ctx, "referer-url", headers.apply("Referer"));
    }

    private static void put(Context ctx, String key, String value) {
        if (value != null) {
            ctx.putLocal(key, value);
        }
    }
}