package io.github.dg.quarkus.inertia.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.vertx.ext.web.RoutingContext;

import io.github.dg.quarkus.inertia.config.InertiaConfig;

/**
 * Shared CSRF logic used by the JAX-RS {@link InertiaCsrfFilter} and the
 * reactive routes pre-handler: issues an {@code XSRF-TOKEN} cookie value
 * stored in the session and validates the matching header on state-changing
 * requests (Laravel {@code X-XSRF-TOKEN} parity).
 */
@ApplicationScoped
public class InertiaCsrfService {

    static final String SESSION_ATTR = "__inertia_csrf";

    /** Context-local flag set when the reactive pre-handler validated the request. */
    public static final String CONTEXT_HANDLED = "inertia-csrf-handled";

    /** Context-local current session token, set by the pre-handler. */
    public static final String CONTEXT_TOKEN = "inertia-csrf-token";

    @Inject
    InertiaConfig config;

    /**
     * Whether CSRF protection is enabled.
     *
     * @return {@code true} to enforce
     */
    public boolean enabled() {
        return config.csrfEnabled();
    }

    /**
     * Whether the HTTP method can change server state.
     *
     * @param method the HTTP method
     * @return {@code true} for anything but GET/HEAD/OPTIONS
     */
    public boolean isStateChanging(String method) {
        return !"GET".equals(method) && !"HEAD".equals(method) && !"OPTIONS".equals(method);
    }

    /**
     * Return the session token, creating it on first use.
     *
     * @param rc the routing context
     * @return the token, or {@code null} when there is no session
     */
    public String getOrCreateToken(RoutingContext rc) {
        var session = rc.session();
        if (session == null) return null;
        var token = (String) session.get(SESSION_ATTR);
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.put(SESSION_ATTR, token);
        }
        return token;
    }

    /**
     * Resolve the token provided by the client ({@code X-XSRF-TOKEN} with a
     * fallback to {@code X-CSRF-TOKEN}).
     *
     * @param rc the routing context
     * @return the provided token, or {@code null}
     */
    public String resolveProvidedToken(RoutingContext rc) {
        var xsrf = rc.request().getHeader("X-XSRF-TOKEN");
        if (xsrf != null && !xsrf.isBlank()) return xsrf;
        return rc.request().getHeader("X-CSRF-TOKEN");
    }

    /**
     * Constant-time comparison of the stored and provided tokens.
     *
     * @param stored   the session token
     * @param provided the client token
     * @return {@code true} when both are present and equal
     */
    public boolean matches(String stored, String provided) {
        if (stored == null || provided == null || provided.isBlank()) return false;
        return MessageDigest.isEqual(
            stored.getBytes(StandardCharsets.UTF_8),
            provided.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * The {@code Set-Cookie} header that hands the token to the client.
     *
     * @param token the session token
     * @return the header value
     */
    public String cookieHeader(String token) {
        return "XSRF-TOKEN=" + token + "; Path=/; SameSite=Lax";
    }
}
