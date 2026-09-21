package io.github.diovamny.quarkus.inertia.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.vertx.ext.web.RoutingContext;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;

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
     * Whether the adapter-owned CSRF protection is active: only in effective
     * mode {@code adapter}. In {@code framework} mode {@code quarkus-rest-csrf}
     * owns CSRF; in {@code disabled} mode nobody does.
     *
     * @return {@code true} to enforce
     */
    public boolean enabled() {
        return InertiaSecurityModes.effectiveMode(config) == InertiaSecurityModes.Mode.ADAPTER;
    }

    /**
     * Whether the {@code lazy} refresh policy is active
     * ({@code inertia.csrf-refresh-policy=lazy}).
     *
     * @return {@code true} to skip redundant re-emission
     */
    public boolean isLazyRefresh() {
        return "lazy".equals(config.csrfRefreshPolicy());
    }

    /**
     * Decide whether the {@code XSRF-TOKEN} cookie must be (re-)emitted.
     * Always emits, except under the {@code lazy} policy on idempotent
     * requests that already present a cookie equal to the session token
     * (keeps those responses cacheable by CDNs and reverse proxies).
     *
     * @param lazy            whether the lazy policy is active
     * @param method          the HTTP method
     * @param presentedCookie the incoming {@code XSRF-TOKEN} cookie value
     * @param sessionToken    the current session token
     * @return {@code true} to emit {@code Set-Cookie}
     */
    public static boolean shouldEmitCookie(boolean lazy, String method,
            String presentedCookie, String sessionToken) {
        if (!lazy) {
            return true;
        }
        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            return true;
        }
        return presentedCookie == null || presentedCookie.isBlank()
            || sessionToken == null || !presentedCookie.equals(sessionToken);
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
     * The cookie stays readable by the client ({@code HttpOnly=false}) so the
     * official Inertia v3 client can echo it in {@code X-XSRF-TOKEN};
     * {@code SameSite}, {@code Secure}, {@code Path} and {@code Domain} come
     * from the {@code inertia.security.cookie-*} settings.
     *
     * @param token the session token
     * @return the header value
     */
    public String cookieHeader(String token) {
        var path = config.securityCookiePath();
        var sameSite = sameSite(config.securityCookieSameSite());
        var builder = new StringBuilder("XSRF-TOKEN=").append(token)
            .append("; Path=").append(path == null || path.isBlank() ? "/" : path.trim())
            .append("; SameSite=").append(sameSite);
        if (config.securityCookieSecure()) {
            builder.append("; Secure");
        }
        var domain = config.securityCookieDomain();
        if (domain.isPresent() && !domain.get().isBlank()) {
            builder.append("; Domain=").append(domain.get().trim());
        }
        return builder.toString();
    }

    private static String sameSite(String configured) {
        if (configured == null || configured.isBlank()) {
            return "Lax";
        }
        var normalized = configured.trim();
        return normalized.substring(0, 1).toUpperCase() + normalized.substring(1).toLowerCase();
    }
}
