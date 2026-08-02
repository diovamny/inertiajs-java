package com.quarkus.inertia.security;

import java.util.UUID;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
@Provider
@Priority(Priorities.HEADER_DECORATOR + 5)
public class InertiaCsrfFilter implements ContainerRequestFilter, ContainerResponseFilter {

    static final String SESSION_ATTR = "__inertia_csrf";

    @Inject
    Instance<RoutingContext> routingContext;

    @Override
    public void filter(ContainerRequestContext request) {
        var xsrfToken = request.getHeaderString("X-XSRF-TOKEN");
        if (xsrfToken != null && !xsrfToken.isBlank()) {
            request.getHeaders().putSingle("X-CSRF-TOKEN", xsrfToken);
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        var token = getOrCreateToken();
        if (token == null) return;

        response.getHeaders().add("Set-Cookie",
            "XSRF-TOKEN=" + token + "; Path=/; SameSite=Lax");
    }

    private String getOrCreateToken() {
        var rc = resolveRoutingContext();
        if (rc == null) return null;
        var session = rc.session();
        if (session == null) return null;

        var token = (String) session.get(SESSION_ATTR);
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.put(SESSION_ATTR, token);
        }
        return token;
    }

    private RoutingContext resolveRoutingContext() {
        try {
            return routingContext.get();
        } catch (Exception e) {
            return null;
        }
    }
}
