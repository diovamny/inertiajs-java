package io.github.diovamny.quarkus.inertia.security;

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
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;

/**
 * CSRF protection for Inertia requests: issues an {@code XSRF-TOKEN} cookie
 * and validates the matching header on non-GET requests (skipped when
 * {@code inertia.csrf-enabled=false}). Keeps the session token synchronized
 * across requests.
 *
 * <p>This filter only applies to Inertia requests (requests with
 * {@code X-Inertia: true} header). Non-Inertia requests pass through
 * without CSRF validation, allowing other security configurations to handle
 * them.
 *
 * <p>When the reactive routes pre-handler already validated the request
 * (session present before routing), the {@code inertia-csrf-handled} flag
 * makes this filter a no-op so the token is never double-checked.</p>
 */
@ApplicationScoped
@Provider
@Priority(Priorities.HEADER_DECORATOR + 5)
public class InertiaCsrfFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    Instance<RoutingContext> routingContext;

    @Inject
    InertiaConfig config;

    @Inject
    InertiaCsrfService csrfService;

    @Override
    public void filter(ContainerRequestContext request) {
        if (!config.csrfEnabled()) return;
        if (csrfHandled()) return;
        // Only validate state-changing Inertia requests. The CSRF cookie is
        // issued on every response (see the response filter) so the initial
        // HTML visit can obtain the token before an Inertia request occurs.
        if (!isInertiaRequest(request)) return;

        var xsrfToken = request.getHeaderString("X-XSRF-TOKEN");
        if (xsrfToken != null && !xsrfToken.isBlank()) {
            request.getHeaders().putSingle("X-CSRF-TOKEN", xsrfToken);
        }

        if (!csrfService.isStateChanging(request.getMethod())) return;

        if (!tokenMatches(xsrfToken)) {
            request.abortWith(Response.status(419).entity("CSRF token mismatch").build());
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        if (!config.csrfEnabled()) return;
        // Issue the XSRF-TOKEN cookie on every response so the first HTML
        // visit (which is not yet an Inertia request) can obtain the token.
        var token = getOrCreateToken();
        if (token == null) return;

        response.getHeaders().add("Set-Cookie", csrfService.cookieHeader(token));
    }

    private boolean isInertiaRequest(ContainerRequestContext request) {
        var header = request.getHeaderString("X-Inertia");
        return header != null && ("true".equalsIgnoreCase(header) || Boolean.parseBoolean(header));
    }

    private boolean csrfHandled() {
        var ctx = Vertx.currentContext();
        return ctx != null && Boolean.TRUE.equals(ctx.getLocal(InertiaCsrfService.CONTEXT_HANDLED));
    }

    private boolean tokenMatches(String provided) {
        if (provided == null || provided.isBlank()) return false;
        var rc = resolveRoutingContext();
        if (rc == null) return false;
        var session = rc.session();
        if (session == null) return false;
        var stored = (String) session.get(InertiaCsrfService.SESSION_ATTR);
        return csrfService.matches(stored, provided);
    }

    private String getOrCreateToken() {
        var rc = resolveRoutingContext();
        if (rc == null) return null;
        return csrfService.getOrCreateToken(rc);
    }

    private RoutingContext resolveRoutingContext() {
        try {
            return routingContext.get();
        } catch (Exception e) {
            return null;
        }
    }
}
