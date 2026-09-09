package io.github.dg.quarkus.inertia.protocol;

import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.RequestScoped;

/**
 * Request-scoped holder for the Vert.x routing context.
 * Quarkus propagates request-scoped beans to {@code @Blocking} worker threads,
 * allowing session access in blocking endpoints.
 */
@RequestScoped
public class RequestRoutingContext {

    private RoutingContext routingContext;

    public RoutingContext getRoutingContext() {
        return routingContext;
    }

    public void setRoutingContext(RoutingContext routingContext) {
        this.routingContext = routingContext;
    }

    public boolean hasRoutingContext() {
        return routingContext != null;
    }
}
