package io.github.diovamny.quarkus.inertia.protocol;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.Provider;
import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

/**
 * Request filter that captures the Inertia request headers into the Vert.x
 * context so {@code inertia.back()} and the page object builder can resolve
 * the previous URL, request method, version and partial-reload hints.
 * Runs as a header decorator on every request.
 *
 * <p>The reactive routes pre-handler already extracts these headers; this
 * filter only re-affirms the JAX-RS view (request URI, method and the
 * {@code inertia-jaxrs} flag) to keep the JAX-RS URL semantics unchanged.</p>
 */
@ApplicationScoped
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class InertiaRequestFilter implements ContainerRequestFilter {

    private static final String SESSION_COOKIE_NAME = "vertx-web.session";

    @Inject
    InertiaHeaderExtractor headerExtractor;

    @Inject
    CurrentVertxRequest currentVertxRequest;

    @Inject
    RequestRoutingContext requestRoutingContext;

    @Inject
    RequestSessionId requestSessionId;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var ctx = Vertx.currentContext();
        if (ctx == null) return;

        var method = requestContext.getMethod();
        var requestUri = requestContext.getUriInfo().getRequestUri();
        var rawPath = requestUri.getRawPath();
        var rawQuery = requestUri.getRawQuery();
        var url = rawQuery != null && !rawQuery.isBlank()
            ? rawPath + "?" + rawQuery
            : rawPath;

        // Set routing context first so InertiaContextLocals mirrors into it (G-17).
        RoutingContext rc = null;
        try {
            rc = currentVertxRequest.getCurrent();
            if (rc != null) {
                ctx.putLocal("inertia-routing-context", rc);
                ctx.putLocal(InertiaContextLocals.ROUTING_CONTEXT_KEY, rc);
                rc.put("inertia-routing-context", rc);
                rc.put(InertiaContextLocals.ROUTING_CONTEXT_KEY, rc);
                requestRoutingContext.setRoutingContext(rc);
            }
        } catch (Exception ignored) {
        }

        if (rc != null) {
            rc.put("inertia-jaxrs", Boolean.TRUE);
            rc.put("request-method", method);
            rc.put("request-uri", url);
        }
        ctx.putLocal("inertia-jaxrs", Boolean.TRUE);
        InertiaContextLocals.put(ctx, "request-method", method);
        InertiaContextLocals.put(ctx, "request-uri", url);

        // Extract session ID from cookie for test mode (propagated via request scope)
        var cookieHeader = requestContext.getHeaderString("Cookie");
        if (cookieHeader != null) {
            var cookies = cookieHeader.split(";");
            for (var cookie : cookies) {
                var parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && SESSION_COOKIE_NAME.equals(parts[0])) {
                    requestSessionId.setSessionId(parts[1]);
                    // Test mode: also store in test holder for fallback
                    if (isTestMode() && rc != null) {
                        TestSessionHolder.put(parts[1], rc);
                    }
                    break;
                }
            }
        }

        if (InertiaContextLocals.isTrue(ctx, "inertia-headers-extracted")) return;

        headerExtractor.extract(ctx, method, url, requestContext::getHeaderString);
        InertiaContextLocals.put(ctx, "inertia-headers-extracted", Boolean.TRUE);
        if (rc != null) {
            rc.put("inertia-headers-extracted", Boolean.TRUE);
        }
    }

    private boolean isTestMode() {
        try {
            return System.getProperty("quarkus.test") != null
                    || System.getProperty("io.quarkus.test.junit.QuarkusTest") != null
                    || Class.forName("io.quarkus.test.junit.QuarkusTest") != null;
        } catch (Exception e) {
            return false;
        }
    }
}
