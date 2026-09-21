package io.github.diovamny.quarkus.inertia.security;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;

/**
 * Resolves the effective CSRF ownership from {@code inertia.security.mode},
 * the legacy {@code inertia.csrf-enabled} alias and framework detection.
 *
 * <p>Precedence (highest to lowest):</p>
 * <ol>
 * <li>Explicit {@code inertia.security.mode} always wins.</li>
 * <li>Legacy {@code inertia.csrf-enabled}: {@code true} maps to
 * {@code adapter}, {@code false} maps to {@code disabled}.</li>
 * <li>Otherwise {@code auto}: {@code framework} when the
 * {@code quarkus-rest-csrf} integration is on the classpath,
 * {@code adapter} when it is not.</li>
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

    private static final String REST_CSRF_MARKER = "io.quarkus.csrf.reactive.runtime.RestCsrfConfig";

    private static final String SECURITY_INTEGRATION_MARKER =
        "io.github.diovamny.quarkus.inertia.security.InertiaQuarkusSecurity";

    private InertiaSecurityModes() {
    }

    /**
     * Whether the Quarkus Security integration is available on the classpath.
     *
     * @return {@code true} when framework mode can be selected
     */
    public static boolean isFrameworkAvailable() {
        try {
            Class.forName(REST_CSRF_MARKER, false, InertiaSecurityModes.class.getClassLoader());
        } catch (Exception e) {
            return false;
        }
        try {
            Class.forName(SECURITY_INTEGRATION_MARKER, false, InertiaSecurityModes.class.getClassLoader());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Resolve the configured mode, without framework detection.
     *
     * @param config the mapped configuration
     * @return the configured mode, defaulting to {@code AUTO}
     */
    public static Mode configuredMode(InertiaConfig config) {
        var raw = config.securityMode();
        if (raw.isPresent() && !raw.get().isBlank()) {
            return parse(raw.get(), "inertia.security.mode");
        }
        var legacy = legacyFlag();
        if (legacy.isPresent()) {
            return Boolean.TRUE.equals(legacy.get()) ? Mode.ADAPTER : Mode.DISABLED;
        }
        return Mode.AUTO;
    }

    /**
     * Resolve the effective ownership, applying {@code auto} detection.
     *
     * @param config the mapped configuration
     * @return {@code FRAMEWORK}, {@code ADAPTER} or {@code DISABLED}
     */
    public static Mode effectiveMode(InertiaConfig config) {
        return effectiveMode(configuredMode(config), isFrameworkAvailable());
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
     * @param config the mapped configuration
     * @return {@code true} when {@code inertia.csrf-enabled} is set without
     *         {@code inertia.security.mode}
     */
    public static boolean isLegacyAliasInUse(InertiaConfig config) {
        return config.securityMode().isEmpty() && legacyFlag().isPresent();
    }

    /**
     * Read the raw legacy {@code inertia.csrf-enabled} flag from the config
     * sources (present only when explicitly set).
     *
     * @return the legacy value, empty when unset or unreadable
     */
    static java.util.Optional<Boolean> legacyFlag() {
        try {
            return org.eclipse.microprofile.config.ConfigProvider.getConfig()
                .getOptionalValue("inertia.csrf-enabled", Boolean.class);
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }

    /**
     * Check whether a {@code Referer} URL is same-origin with the request.
     *
     * @param referer the Referer header value, may be {@code null}
     * @param scheme request scheme (http/https)
     * @param authority request authority (host[:port])
     * @return {@code true} when the referer is a same-origin http(s) URL
     */
    public static boolean isSameOriginReferer(String referer, String scheme, String authority) {
        if (referer == null || referer.isBlank()) {
            return false;
        }
        try {
            var uri = java.net.URI.create(referer);
            if (!uri.isAbsolute()) {
                return false;
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                return false;
            }
            if (!uri.getScheme().equalsIgnoreCase(scheme)) {
                return false;
            }
            var refererAuthority = uri.getAuthority();
            return refererAuthority != null && refererAuthority.equalsIgnoreCase(authority);
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
     * @param authority request authority
     * @param fallbackPath configured safe path
     * @return the redirect target, never external
     */
    public static String safeFailureTarget(String referer, String scheme, String authority,
            String fallbackPath) {
        if (isSameOriginReferer(referer, scheme, authority)) {
            return referer;
        }
        if (fallbackPath == null || fallbackPath.isBlank()) {
            return "/";
        }
        return fallbackPath;
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
