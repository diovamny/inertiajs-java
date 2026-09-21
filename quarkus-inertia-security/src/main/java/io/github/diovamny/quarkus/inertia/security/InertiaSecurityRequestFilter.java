package io.github.diovamny.quarkus.inertia.security;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import io.vertx.ext.web.RoutingContext;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;
import io.github.diovamny.quarkus.inertia.spi.FlashStore;

/**
 * Pre-controller CSRF hardening for Inertia visits in effective mode
 * {@code framework}.
 *
 * <p>{@code quarkus-rest-csrf} only verifies a token when one is presented;
 * requests without any token pass through to the resource. Forging a
 * cookie-only request is exactly the cross-site attack CSRF protection must
 * stop, so this filter rejects state-changing Inertia visits that present no
 * token at all ({@code 303} back to the same-origin referer or the configured
 * fallback, with a generic flash message) before any controller runs. Visits
 * that do present a token are left to {@code quarkus-rest-csrf}, whose
 * failures the {@link InertiaSecurityBridgeFilter} maps.</p>
 *
 * <p>Runs at {@code AUTHORIZATION} priority so authentication (and its
 * {@code 401} challenges, mapped to {@code 409} for Inertia visits) happens
 * first, and non-Inertia traffic is never touched.</p>
 */
@ApplicationScoped
@Provider
@Priority(Priorities.AUTHORIZATION)
public class InertiaSecurityRequestFilter implements ContainerRequestFilter {

    @Inject
    InertiaConfig config;

    @Inject
    FlashStore flashStore;

    @Inject
    jakarta.enterprise.inject.Instance<RoutingContext> routingContext;

    @Inject
    io.quarkus.vertx.http.runtime.CurrentVertxRequest currentVertxRequest;

    @Override
    public void filter(ContainerRequestContext request) {
        if (InertiaSecurityModes.effectiveMode(config) != InertiaSecurityModes.Mode.FRAMEWORK) {
            return;
        }
        if (!InertiaChallengeUrls.isInertiaVisit(request.getHeaderString("X-Inertia"))) {
            return;
        }
        if (!isStateChanging(request.getMethod())) {
            return;
        }
        if (hasPresentedToken(request)) {
            return;
        }
        flashStore.put(config.securityCsrfFlashKey(), config.securityCsrfFlashMessage());
        request.abortWith(Response.status(Response.Status.SEE_OTHER)
            .header("Location", safeFailureTarget())
            .build());
    }

    private boolean hasPresentedToken(ContainerRequestContext request) {
        var header = request.getHeaderString("X-XSRF-TOKEN");
        if (header != null && !header.isBlank()) {
            return true;
        }
        var fallback = request.getHeaderString("X-CSRF-TOKEN");
        return fallback != null && !fallback.isBlank();
    }

    private String safeFailureTarget() {
        var rc = resolveRoutingContext();
        if (rc != null) {
            return InertiaSecurityModes.safeFailureTarget(
                rc.request().getHeader("Referer"),
                rc.request().scheme(), rc.request().authority().toString(),
                config.securityCsrfFailurePath());
        }
        return config.securityCsrfFailurePath();
    }

    private static boolean isStateChanging(String method) {
        return !"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)
            && !"OPTIONS".equalsIgnoreCase(method);
    }

    private RoutingContext resolveRoutingContext() {
        try {
            var rc = routingContext.get();
            if (rc != null) {
                return rc;
            }
        } catch (Exception ignored) {
            // no active routing context
        }
        try {
            return currentVertxRequest.getCurrent();
        } catch (Exception ignored) {
            return null;
        }
    }
}
