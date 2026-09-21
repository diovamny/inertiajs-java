package io.github.diovamny.quarkus.inertia.vertx;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import io.github.diovamny.quarkus.inertia.api.Inertia;
import io.github.diovamny.quarkus.inertia.config.InertiaConfig;
import io.github.diovamny.quarkus.inertia.security.InertiaCsrfService;
import io.github.diovamny.quarkus.inertia.security.InertiaSecurityModes;

/**
 * Adapter CSRF validation for reactive routes ({@code @Route}).
 *
 * <p>Why a separate filter: {@code quarkus-rest-csrf} only sees JAX-RS
 * traffic, and the router pre-handler ({@link InertiaVertxHandler}) runs
 * before route matching so it cannot tell transports apart. This
 * {@code @RouteFilter} runs exclusively in the reactive-route chain:</p>
 * <ul>
 * <li>mode {@code adapter}: the pre-handler already validated; this filter is
 * a no-op (sees the handled flag) and never double-checks;</li>
 * <li>mode {@code framework}: {@code quarkus-rest-csrf} owns JAX-RS while
 * this filter owns the declared reactive paths (see
 * {@code inertia.security.reactive-csrf-paths}), using the same session
 * token, cookie and {@code 303 + flash} failure contract;</li>
 * <li>mode {@code disabled}: no-op.</li>
 * </ul>
 *
 * <p>Why path scoping: router-level filters run before route matching, so
 * this filter cannot tell a reactive route from a JAX-RS resource by itself.
 * Validating every request with the adapter token would break JAX-RS traffic
 * (whose client holds rest-csrf's token instead). The application therefore
 * declares its mutating reactive prefixes; anything else is left to
 * {@code quarkus-rest-csrf}.</p>
 */
@ApplicationScoped
public class InertiaReactiveCsrfFilter {

    @Inject
    InertiaCsrfService csrfService;

    @Inject
    InertiaConfig config;

    @Inject
    ReactiveResponseWriter responseWriter;

    @Inject
    Inertia inertia;

    /**
     * Validate the CSRF token for reactive routes when the adapter owns them.
     *
     * @param rc the routing context
     */
    @RouteFilter(100)
    void filter(RoutingContext rc) {
        if (rc.response().ended() || rc.response().closed()) {
            return;
        }
        var mode = InertiaSecurityModes.effectiveMode(config);
        if (mode == InertiaSecurityModes.Mode.DISABLED) {
            rc.next();
            return;
        }
        if (mode == InertiaSecurityModes.Mode.ADAPTER && alreadyHandled(rc)) {
            rc.next();
            return;
        }
        if (mode == InertiaSecurityModes.Mode.FRAMEWORK
                && !isDeclaredReactivePath(rc.request().path())) {
            // JAX-RS traffic (or undeclared paths): quarkus-rest-csrf decides.
            rc.next();
            return;
        }
        var session = rc.session();
        if (session == null) {
            rc.next();
            return;
        }

        var token = csrfService.getOrCreateToken(rc);
        var ctx = Vertx.currentContext();
        if (token != null) {
            rc.put(InertiaCsrfService.CONTEXT_TOKEN, token);
            rc.put(InertiaCsrfService.CONTEXT_HANDLED, Boolean.TRUE);
            if (ctx != null) {
                ctx.putLocal(InertiaCsrfService.CONTEXT_TOKEN, token);
                ctx.putLocal(InertiaCsrfService.CONTEXT_HANDLED, Boolean.TRUE);
            }
        }

        var method = rc.request().method().name();
        if (!csrfService.isStateChanging(method)) {
            rc.next();
            return;
        }

        if (!csrfService.matches(token, csrfService.resolveProvidedToken(rc))) {
            if (isInertiaVisit(rc)) {
                inertia.flash(config.securityCsrfFlashKey(), config.securityCsrfFlashMessage());
                var target = InertiaSecurityModes.safeFailureTarget(
                    rc.request().getHeader("Referer"),
                    rc.request().scheme(), rc.request().authority().toString(),
                    config.securityCsrfFailurePath());
                responseWriter.write(rc, Response.status(Response.Status.SEE_OTHER)
                    .header("Location", target).build());
            } else {
                responseWriter.write(rc, Response.status(419).entity("CSRF token mismatch").build());
            }
            return;
        }
        rc.next();
    }

    private boolean isDeclaredReactivePath(String path) {
        var configured = config.securityReactiveCsrfPaths();
        if (path == null || configured.isEmpty()) {
            return false;
        }
        for (var prefix : configured.get()) {
            if (prefix == null || prefix.isBlank()) {
                continue;
            }
            var normalized = prefix.trim();
            if (!normalized.startsWith("/")) {
                normalized = "/" + normalized;
            }
            if (path.equals(normalized) || path.startsWith(normalized + "/")) {
                return true;
            }
        }
        return false;
    }

    private boolean alreadyHandled(RoutingContext rc) {
        if (Boolean.TRUE.equals(rc.get(InertiaCsrfService.CONTEXT_HANDLED))) {
            return true;
        }
        var ctx = Vertx.currentContext();
        return ctx != null && InertiaSecurityModes.effectiveMode(config)
            == InertiaSecurityModes.Mode.ADAPTER
            && io.github.diovamny.quarkus.inertia.protocol.InertiaContextLocals.isTrue(
                ctx, InertiaCsrfService.CONTEXT_HANDLED);
    }

    private static boolean isInertiaVisit(RoutingContext rc) {
        var header = rc.request().getHeader("X-Inertia");
        return header != null && ("true".equalsIgnoreCase(header) || Boolean.parseBoolean(header));
    }
}
