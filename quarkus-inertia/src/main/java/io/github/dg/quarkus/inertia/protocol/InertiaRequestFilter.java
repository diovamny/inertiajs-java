package io.github.dg.quarkus.inertia.protocol;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.Provider;
import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import io.vertx.core.Vertx;

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

    @Inject
    InertiaHeaderExtractor headerExtractor;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var ctx = Vertx.currentContext();
        if (ctx == null) return;

        var method = requestContext.getMethod();
        var requestUri = requestContext.getUriInfo().getRequestUri().toString();

        ctx.putLocal("inertia-jaxrs", Boolean.TRUE);
        ctx.putLocal("request-method", method);
        ctx.putLocal("request-uri", requestUri);

        if (Boolean.TRUE.equals(ctx.getLocal("inertia-headers-extracted"))) return;

        headerExtractor.extract(ctx, method, requestUri, requestContext::getHeaderString);
        ctx.putLocal("inertia-headers-extracted", Boolean.TRUE);
    }
}