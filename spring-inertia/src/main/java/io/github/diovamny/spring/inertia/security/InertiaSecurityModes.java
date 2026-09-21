package io.github.diovamny.spring.inertia.security;

import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import io.github.diovamny.spring.inertia.config.InertiaProperties;

/**
 * Resolves the effective CSRF ownership from {@code inertia.security.mode},
 * the legacy {@code inertia.csrf-enabled} alias and framework detection.
 *
 * <p>Precedence (highest to lowest):</p>
 * <ol>
 * <li>Explicit {@code inertia.security.mode} always wins.</li>
 * <li>Legacy {@code inertia.csrf-enabled}: {@code true} maps to
 * {@code adapter}, {@code false} maps to {@code disabled}.</li>
 * <li>Otherwise {@code auto}: {@code framework} when the security integration
 * is on the classpath, {@code adapter} when it is not.</li>
 * </ol>
 */
public final class InertiaSecurityModes {

    /** Effective CSRF ownership. */
    public enum Mode {
        AUTO,
        FRAMEWORK,
        ADAPTER,
        DISABLED
    }

    private static final String SECURITY_FILTER_CHAIN = "org.springframework.security.web.SecurityFilterChain";

    private static final String SECURITY_INTEGRATION_MARKER =
        "io.github.diovamny.spring.inertia.security.InertiaSpringSecurity";

    private InertiaSecurityModes() {
    }

    /**
     * Whether the Spring Security integration is available on the classpath.
     *
     * @return {@code true} when framework mode can be selected
     */
    public static boolean isFrameworkAvailable() {
        return ClassUtils.isPresent(SECURITY_FILTER_CHAIN, InertiaSecurityModes.class.getClassLoader())
            && ClassUtils.isPresent(SECURITY_INTEGRATION_MARKER, InertiaSecurityModes.class.getClassLoader());
    }

    /**
     * Resolve the configured mode, without framework detection.
     *
     * @param properties the bound properties
     * @return the configured mode, defaulting to {@code AUTO}
     */
    public static Mode configuredMode(InertiaProperties properties) {
        var raw = properties.getSecurity().getMode();
        if (raw != null && !raw.isBlank()) {
            return parse(raw, "inertia.security.mode");
        }
        if (properties.isCsrfEnabledSet()) {
            return properties.isCsrfEnabled() ? Mode.ADAPTER : Mode.DISABLED;
        }
        return Mode.AUTO;
    }

    /**
     * Resolve the effective ownership, applying {@code auto} detection.
     *
     * @param properties the bound properties
     * @return {@code FRAMEWORK}, {@code ADAPTER} or {@code DISABLED}
     */
    public static Mode effectiveMode(InertiaProperties properties) {
        return effectiveMode(configuredMode(properties), isFrameworkAvailable());
    }

    /**
     * Apply {@code auto} detection to an already parsed mode.
     *
     * @param configured the configured mode
     * @param frameworkAvailable whether the framework integration is present
     * @return the effective ownership (never {@code AUTO})
     */
    public static Mode effectiveMode(Mode configured, boolean frameworkAvailable) {
        if (configured == Mode.AUTO) {
            return frameworkAvailable ? Mode.FRAMEWORK : Mode.ADAPTER;
        }
        return configured;
    }

    /**
     * Whether the legacy alias is in use and a migration warning applies.
     *
     * @param properties the bound properties
     * @return {@code true} when {@code inertia.csrf-enabled} is set without
     *         {@code inertia.security.mode}
     */
    public static boolean isLegacyAliasInUse(InertiaProperties properties) {
        var raw = properties.getSecurity().getMode();
        return (raw == null || raw.isBlank()) && properties.isCsrfEnabledSet();
    }

    /**
     * Check whether a {@code Referer} URL is same-origin with the request.
     * Only same-origin referers may be used as redirect targets; anything
     * else (external, invalid) must fall back to a configured safe path.
     *
     * @param referer the Referer header value, may be {@code null}
     * @param scheme request scheme (http/https)
     * @param host request server name
     * @param port request server port
     * @return {@code true} when the referer is a same-origin http(s) URL
     */
    public static boolean isSameOriginReferer(String referer, String scheme, String host, int port) {
        if (referer == null || referer.isBlank()) {
            return false;
        }
        try {
            var uri = new java.net.URI(referer);
            if (!uri.isAbsolute()) {
                return false;
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                return false;
            }
            if (!uri.getScheme().equalsIgnoreCase(scheme)) {
                return false;
            }
            if (!uri.getHost().equalsIgnoreCase(host)) {
                return false;
            }
            return uri.getPort() == -1 || uri.getPort() == port;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Resolve the safe CSRF-failure target: the referer when same-origin,
     * otherwise the configured fallback path (default {@code /}).
     *
     * @param referer the Referer header value, may be {@code null}
     * @param scheme request scheme
     * @param host request server name
     * @param port request server port
     * @param fallbackPath configured safe path
     * @return the redirect target, never external
     */
    public static String safeFailureTarget(String referer, String scheme, String host, int port,
            String fallbackPath) {
        if (isSameOriginReferer(referer, scheme, host, port)) {
            return referer;
        }
        if (fallbackPath == null || fallbackPath.isBlank()) {
            return "/";
        }
        return fallbackPath;
    }

    /**
     * Whether the application runs with a production profile active.
     *
     * @param environment the Spring environment, may be {@code null}
     * @return {@code true} when {@code prod} or {@code production} is active
     */
    public static boolean isProductionProfile(Environment environment) {
        if (environment == null) {
            return false;
        }
        for (var profile : environment.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    private static Mode parse(String raw, String property) {
        for (var mode : Mode.values()) {
            if (mode.name().equalsIgnoreCase(raw.trim())) {
                return mode;
            }
        }
        throw new IllegalArgumentException(
            property + " must be one of auto, framework, adapter, disabled, got: " + raw);
    }
}
