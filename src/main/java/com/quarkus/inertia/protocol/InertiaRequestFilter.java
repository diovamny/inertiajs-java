package com.quarkus.inertia.protocol;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.Provider;
import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import io.vertx.core.Vertx;

@ApplicationScoped
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class InertiaRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var ctx = Vertx.currentContext();
        if (ctx == null) return;

        var inertiaHeader = requestContext.getHeaderString("X-Inertia");
        ctx.putLocal("inertia-request", "true".equalsIgnoreCase(inertiaHeader)
            || Boolean.parseBoolean(inertiaHeader));

        ctx.putLocal("request-method", requestContext.getMethod());

        ctx.putLocal("request-uri", requestContext.getUriInfo().getRequestUri().toString());

        var version = requestContext.getHeaderString("X-Inertia-Version");
        if (version != null) {
            ctx.putLocal("inertia-version", version);
        }

        var partialComponent = requestContext.getHeaderString("X-Inertia-Partial-Component");
        if (partialComponent != null) {
            ctx.putLocal("inertia-partial-component", partialComponent);
        }

        var partialData = requestContext.getHeaderString("X-Inertia-Partial-Data");
        if (partialData != null) {
            ctx.putLocal("inertia-partial-data", partialData);
        }

        var partialExcept = requestContext.getHeaderString("X-Inertia-Partial-Except");
        if (partialExcept != null) {
            ctx.putLocal("inertia-partial-except", partialExcept);
        }

        var reset = requestContext.getHeaderString("X-Inertia-Reset");
        if (reset != null) {
            ctx.putLocal("inertia-reset", reset);
        }

        var exceptOnce = requestContext.getHeaderString("X-Inertia-Except-Once-Props");
        if (exceptOnce != null) {
            ctx.putLocal("inertia-except-once-props", exceptOnce);
        }

        var errorBag = requestContext.getHeaderString("X-Inertia-Error-Bag");
        if (errorBag != null) {
            ctx.putLocal("inertia-error-bag", errorBag);
        }

        var scrollMergeIntent = requestContext.getHeaderString("X-Inertia-Infinite-Scroll-Merge-Intent");
        if (scrollMergeIntent != null) {
            ctx.putLocal("inertia-scroll-merge-intent", scrollMergeIntent);
        }

        var precognition = requestContext.getHeaderString("X-Inertia-Precognition");
        if (precognition != null) {
            ctx.putLocal("inertia-precognition", "true".equalsIgnoreCase(precognition));
        }

        var prefetch = requestContext.getHeaderString("X-Inertia-Prefetch");
        if (prefetch != null) {
            ctx.putLocal("inertia-prefetch", "true".equalsIgnoreCase(prefetch));
        }

        var referer = requestContext.getHeaderString("Referer");
        if (referer != null) {
            ctx.putLocal("referer-url", referer);
        }
    }
}
