package io.github.diovamny.spring.inertia.config;


/**
 * Validates the {@code inertia.*} configuration at startup and fails fast on
 * unsupported values.
 */
public class InertiaConfigValidator {

    private static final String[] STRATEGIES = {"sha256", "vite-manifest", "custom"};

    public InertiaConfigValidator(InertiaProperties properties) {
        this(properties, null);
    }

    public InertiaConfigValidator(InertiaProperties properties,
            org.springframework.core.env.Environment environment) {
        validateVersionStrategy(properties.getVersionStrategy());
        validateStatus(properties.getErrorStatus());
        validateMaxPageBytes(properties.getMaxPageBytes());
        validateRootTemplate(properties.getRootTemplate());
        validateRootView(properties.getRootView());
        validateCsrfRefreshPolicy(properties.getCsrfRefreshPolicy());
        validateSecurity(properties, environment);
    }

    private static void validateCsrfRefreshPolicy(String policy) {
        if (policy == null || (!policy.equals("always") && !policy.equals("lazy"))) {
            throw new IllegalArgumentException(
                "inertia.csrf-refresh-policy must be 'always' or 'lazy', got: " + policy);
        }
    }

    private static void validateSecurity(InertiaProperties properties,
            org.springframework.core.env.Environment environment) {
        var security = properties.getSecurity();
        var mode = io.github.diovamny.spring.inertia.security.InertiaSecurityModes
            .configuredMode(properties);
        if (mode == io.github.diovamny.spring.inertia.security.InertiaSecurityModes.Mode.FRAMEWORK
                && !io.github.diovamny.spring.inertia.security.InertiaSecurityModes.isFrameworkAvailable()) {
            throw new IllegalStateException(
                "inertia.security.mode=framework requires spring-inertia-security "
                    + "(and spring-security-web) on the classpath");
        }
        var effective = io.github.diovamny.spring.inertia.security.InertiaSecurityModes
            .effectiveMode(properties);
        if (effective == io.github.diovamny.spring.inertia.security.InertiaSecurityModes.Mode.ADAPTER
                && mode == io.github.diovamny.spring.inertia.security.InertiaSecurityModes.Mode.AUTO
                && security.isFailOnFallback()) {
            throw new IllegalStateException(
                "inertia.security.mode=auto resolved to the adapter fallback, but "
                    + "inertia.security.fail-on-fallback=true (production requires Spring Security)");
        }
        if (effective == io.github.diovamny.spring.inertia.security.InertiaSecurityModes.Mode.DISABLED
                && io.github.diovamny.spring.inertia.security.InertiaSecurityModes
                    .isProductionProfile(environment)
                && !security.isAllowDisabledInProduction()) {
            throw new IllegalStateException(
                "inertia.security.mode=disabled is not allowed with the prod/production "
                    + "profile unless inertia.security.allow-disabled-in-production=true");
        }
        validateCookieSameSite(security.getCookieSameSite());
        validateLoginUrl(security.getLoginUrl());
        validatePath(security.getCsrfFailurePath(), "inertia.security.csrf-failure-path");
    }

    private static void validateCookieSameSite(String sameSite) {
        if (sameSite == null || sameSite.isBlank()) {
            throw new IllegalArgumentException("inertia.security.cookie-same-site must not be blank");
        }
        var normalized = sameSite.trim();
        if (!normalized.equalsIgnoreCase("Lax")
                && !normalized.equalsIgnoreCase("Strict")
                && !normalized.equalsIgnoreCase("None")) {
            throw new IllegalArgumentException(
                "inertia.security.cookie-same-site must be one of Lax, Strict, None, got: " + sameSite);
        }
    }

    private static void validatePath(String path, String property) {
        if (path == null || path.isBlank() || !path.startsWith("/")) {
            throw new IllegalArgumentException(property + " must be an absolute path, got: " + path);
        }
    }

    private static void validateLoginUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("inertia.security.login-url must not be blank");
        }
        var candidate = url.trim();
        if (candidate.startsWith("/")) {
            return;
        }
        try {
            var uri = new java.net.URI(candidate);
            if (uri.isAbsolute() && "https".equalsIgnoreCase(uri.getScheme())) {
                return;
            }
        } catch (Exception ignored) {
            // fall through to the failure below
        }
        throw new IllegalArgumentException(
            "inertia.security.login-url must be an absolute path or an https URL, got: " + url);
    }

    private static void validateVersionStrategy(String strategy) {
        for (var candidate : STRATEGIES) {
            if (candidate.equalsIgnoreCase(strategy)) {
                return;
            }
        }
        throw new IllegalArgumentException(
            "inertia.version-strategy must be one of " + String.join(", ", STRATEGIES) + ", got: " + strategy);
    }

    private static void validateMaxPageBytes(long maxPageBytes) {
        if (maxPageBytes != -1 && maxPageBytes <= 0) {
            throw new IllegalArgumentException(
                "inertia.max-page-bytes must be -1 (disable) or a positive byte count, got: "
                    + maxPageBytes);
        }
    }

    private static void validateStatus(int status) {
        if (status < 400 || status > 599) {
            throw new IllegalArgumentException(
                "inertia.error-status must be a 4xx/5xx HTTP status, got: " + status);
        }
    }

    private static void validateRootTemplate(String template) {
        if (template == null || template.isBlank()) {
            throw new IllegalArgumentException("inertia.root-template must not be blank");
        }
    }

    private static void validateRootView(String view) {
        if (view != null && view.isBlank()) {
            throw new IllegalArgumentException("inertia.root-view must not be blank");
        }
    }
}
