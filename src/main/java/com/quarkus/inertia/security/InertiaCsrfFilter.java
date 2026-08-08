package com.quarkus.inertia.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import io.vertx.ext.web.RoutingContext;

import com.quarkus.inertia.config.InertiaConfig;

/**
 * CSRF protection for Inertia requests: issues an {@code XSRF-TOKEN} cookie
 * and validates the matching header on non-GET requests (skipped when
 * {@code inertia.csrf-enabled=false}). Keeps the session token synchronized
 * across requests.
 */
@ApplicationScoped
@Provider
@Priority(Priorities.HEADER_DECORATOR + 5)
public class InertiaCsrfFilter implements ContainerRequestFilter, ContainerResponseFilter {

    static final String SESSION_ATTR = "__inertia_csrf";

    @Inject
    Instance<RoutingContext> routingContext;

    @Inject
    InertiaConfig config;

    @Override
    public void filter(ContainerRequestContext request) {
        if (!config.csrfEnabled()) return;

        var xsrfToken = request.getHeaderString("X-XSRF-TOKEN");
        if (xsrfToken != null && !xsrfToken.isBlank()) {
            request.getHeaders().putSingle("X-CSRF-TOKEN", xsrfToken);
        }

        if (!isStateChanging(request.getMethod())) return;

        if (!tokenMatches(xsrfToken)) {
            request.abortWith(Response.status(419).entity("CSRF token mismatch").build());
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        if (!config.csrfEnabled()) return;

        var token = getOrCreateToken();
        if (token == null) return;

        response.getHeaders().add("Set-Cookie",
            "XSRF-TOKEN=" + token + "; Path=/; SameSite=Lax");
    }

    private boolean isStateChanging(String method) {
        return !"GET".equals(method) && !"HEAD".equals(method) && !"OPTIONS".equals(method);
    }

    private boolean tokenMatches(String provided) {
        if (provided == null || provided.isBlank()) return false;
        var rc = resolveRoutingContext();
        if (rc == null) return true;
        var session = rc.session();
        if (session == null) return true;
        var stored = (String) session.get(SESSION_ATTR);
        if (stored == null) return false;

        return MessageDigest.isEqual(
            stored.getBytes(StandardCharsets.UTF_8),
            provided.getBytes(StandardCharsets.UTF_8));
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