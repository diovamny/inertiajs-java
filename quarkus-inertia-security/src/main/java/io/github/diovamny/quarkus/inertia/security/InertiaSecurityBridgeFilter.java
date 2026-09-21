package io.github.diovamny.quarkus.inertia.security;

import java.util.Map;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import io.vertx.ext.web.RoutingContext;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;
import io.github.diovamny.quarkus.inertia.model.PageObject;
import io.github.diovamny.quarkus.inertia.spi.FlashStore;
import io.github.diovamny.quarkus.inertia.spi.JsonProvider;
import io.github.diovamny.quarkus.inertia.version.VersionProvider;

/**
 * Protocol bridge for Quarkus Security decisions on JAX-RS responses, active
 * only in effective mode {@code framework} (where {@code quarkus-rest-csrf}
 * owns CSRF and Quarkus owns authentication/authorization):
 *
 * <ul>
 * <li>{@code 401} on an Inertia visit → {@code 409 + X-Inertia-Location}
 * (validated login/OIDC URL, no {@code X-Inertia} header) for a full
 * navigation to the challenge.</li>
 * <li>{@code 403} on an Inertia visit → a valid {@code 403} Inertia page
 * ({@code X-Inertia: true}, {@code Vary: X-Inertia}, configured component,
 * default {@code Errors/Forbidden}).</li>
 * <li>{@code 400} from {@code quarkus-rest-csrf} (empty body, token never
 * verified) on a state-changing Inertia visit → generic flash message plus
 * {@code 303} back to the same-origin referer (or the configured fallback),
 * so the user can retry with a fresh token.</li>
 * <li>Anything else (including non-Inertia traffic) passes through
 * untouched.</li>
 * </ul>
 */
@ApplicationScoped
@Provider
@Priority(Priorities.AUTHORIZATION + 100)
public class InertiaSecurityBridgeFilter implements ContainerResponseFilter {

    /**
     * Request property set by {@code quarkus-rest-csrf} once the token is
     * verified (either on the JAX-RS request context or the routing context).
     */
    static final String CSRF_VERIFIED_PROPERTY = "csrf_token_verified";

    @Inject
    InertiaConfig config;

    @Inject
    FlashStore flashStore;

    @Inject
    JsonProvider jsonProvider;

    @Inject
    VersionProvider versionProvider;

    @Inject
    jakarta.enterprise.inject.Instance<RoutingContext> routingContext;

    @Inject
    io.quarkus.vertx.http.runtime.CurrentVertxRequest currentVertxRequest;

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        if (InertiaSecurityModes.effectiveMode(config) != InertiaSecurityModes.Mode.FRAMEWORK) {
            return;
        }
        if (!InertiaChallengeUrls.isInertiaVisit(request.getHeaderString("X-Inertia"))) {
            return;
        }
        int status = response.getStatus();
        if (status == 401) {
            response.setStatus(409);
            response.getHeaders().putSingle("X-Inertia-Location",
                InertiaChallengeUrls.validatedLoginUrl(config.securityLoginUrl()));
            response.getHeaders().remove("X-Inertia");
            response.setEntity(null, null, null);
            return;
        }
        if (status == 403) {
            renderForbiddenPage(request, response);
            return;
        }
        if (status == 400 && isStateChanging(request.getMethod()) && !isCsrfVerified(request)) {
            rejectCsrf(request, response);
        }
    }

    /**
     * Answer a CSRF failure on an Inertia visit with {@code 303} back to the
     * same-origin referer (or the configured fallback) carrying a generic
     * flash message.
     *
     * @param request the current request
     * @param response the current response
     */
    private void rejectCsrf(ContainerRequestContext request, ContainerResponseContext response) {
        flashStore.put(config.securityCsrfFlashKey(), config.securityCsrfFlashMessage());
        response.setStatus(Response.Status.SEE_OTHER.getStatusCode());
        response.getHeaders().putSingle("Location", safeFailureTarget(request));
        response.setEntity(null, null, null);
    }

    private void renderForbiddenPage(ContainerRequestContext request,
            ContainerResponseContext response) {
        var uri = request.getUriInfo().getRequestUri();
        var path = uri.getRawPath();
        var query = uri.getRawQuery();
        var url = query != null && !query.isBlank() ? path + "?" + query : path;
        var page = new PageObject(config.securityForbiddenComponent(),
            Map.of("status", 403, "message", "Forbidden"), url, versionProvider.getVersion());
        response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
        response.getHeaders().putSingle("X-Inertia", "true");
        io.github.diovamny.quarkus.inertia.util.VaryHeaderUtil.addTo(
            response.getHeaders(), "X-Inertia");
        try {
            response.setEntity(jsonProvider.toJson(page), null, MediaType.APPLICATION_JSON_TYPE);
        } catch (Exception e) {
            response.setEntity(Map.of("component", config.securityForbiddenComponent()),
                null, MediaType.APPLICATION_JSON_TYPE);
        }
    }

    private String safeFailureTarget(ContainerRequestContext request) {
        var rc = resolveRoutingContext();
        if (rc != null) {
            return InertiaSecurityModes.safeFailureTarget(
                rc.request().getHeader("Referer"),
                rc.request().scheme(), rc.request().authority().toString(),
                config.securityCsrfFailurePath());
        }
        return config.securityCsrfFailurePath();
    }

    private boolean isCsrfVerified(ContainerRequestContext request) {
        try {
            if (Boolean.TRUE.equals(request.getProperty(CSRF_VERIFIED_PROPERTY))) {
                return true;
            }
        } catch (Exception ignored) {
            // fall through to the routing-context lookup
        }
        var rc = resolveRoutingContext();
        return rc != null && Boolean.TRUE.equals(rc.get(CSRF_VERIFIED_PROPERTY));
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
