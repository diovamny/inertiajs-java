package io.github.diovamny.quarkus.inertia.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

import io.github.diovamny.quarkus.inertia.security.InertiaSecurityModes;

/**
 * Validates the Inertia configuration at application startup: the root
 * template must not be blank, the version strategy must be supported, and the
 * security mode must satisfy the production guardrails (framework presence,
 * fallback refusal, disabled-mode confirmation).
 */
@ApplicationScoped
public class InertiaConfigValidator {

    private static final Logger LOG = Logger.getLogger(InertiaConfigValidator.class);

    private final InertiaConfig config;

    @Inject
    public InertiaConfigValidator(InertiaConfig config) {
        this.config = config;
    }

    void onStart(@Observes StartupEvent event) {
        var root = config.rootTemplate();
        if (root == null || root.isBlank()) {
            throw new IllegalStateException("inertia.root-template must not be blank");
        }
        var strategy = config.versionStrategy();
        if (!strategy.equals("sha256") && !strategy.equals("custom") && !strategy.equals("vite-manifest")) {
            throw new IllegalArgumentException(
                "inertia.version-strategy must be 'sha256', 'custom', or 'vite-manifest', got: " + strategy
            );
        }
        validateSecurity();
    }

    private void validateSecurity() {
        var configured = InertiaSecurityModes.configuredMode(config);
        var effective = InertiaSecurityModes.effectiveMode(config);
        if (configured == InertiaSecurityModes.Mode.FRAMEWORK
                && !InertiaSecurityModes.isFrameworkAvailable()) {
            throw new IllegalStateException(
                "inertia.security.mode=framework requires quarkus-inertia-security "
                    + "(and quarkus-rest-csrf) on the classpath");
        }
        if (effective == InertiaSecurityModes.Mode.ADAPTER
                && configured == InertiaSecurityModes.Mode.AUTO
                && config.securityFailOnFallback()) {
            throw new IllegalStateException(
                "inertia.security.mode=auto resolved to the adapter fallback, but "
                    + "inertia.security.fail-on-fallback=true "
                    + "(production requires Quarkus Security)");
        }
        if (effective == InertiaSecurityModes.Mode.DISABLED
                && LaunchMode.current() == LaunchMode.NORMAL
                && !config.securityAllowDisabledInProduction()) {
            throw new IllegalStateException(
                "inertia.security.mode=disabled is not allowed in prod mode unless "
                    + "inertia.security.allow-disabled-in-production=true");
        }
        validateCookieSameSite(config.securityCookieSameSite());
        validateLoginUrl(config.securityLoginUrl());
        validatePath(config.securityCsrfFailurePath(), "inertia.security.csrf-failure-path");
        if (InertiaSecurityModes.isLegacyAliasInUse(config)) {
            LOG.warnf("inertia.csrf-enabled is deprecated; set inertia.security.mode=%s instead",
                effective.name().toLowerCase());
        }
        if (configured == InertiaSecurityModes.Mode.AUTO
                && effective == InertiaSecurityModes.Mode.ADAPTER) {
            LOG.warn("Inertia security mode=auto resolved to the adapter fallback "
                + "(quarkus-rest-csrf integration not detected). The adapter CSRF filter "
                + "protects Inertia visits only and is not a substitute for authentication "
                + "and authorization. Set inertia.security.fail-on-fallback=true to refuse "
                + "this fallback in production.");
        }
        if (effective == InertiaSecurityModes.Mode.DISABLED) {
            LOG.warn("Inertia CSRF protection is disabled (mode=disabled). "
                + "Only acceptable for tests or cookieless APIs.");
        }
        if (effective == InertiaSecurityModes.Mode.FRAMEWORK
                && config.securityReactiveCsrfPaths().isEmpty()
                && isReactiveRoutesPresent()) {
            LOG.warn("inertia.security.mode=framework with no "
                + "inertia.security.reactive-csrf-paths: mutating reactive (@Route) "
                + "endpoints are NOT CSRF-protected (quarkus-rest-csrf only sees "
                + "JAX-RS). Declare the reactive prefixes or migrate them to JAX-RS.");
        }
    }

    private static boolean isReactiveRoutesPresent() {
        try {
            Class.forName("io.quarkus.vertx.web.Route",
                false, InertiaConfigValidator.class.getClassLoader());
            return true;
        } catch (Exception e) {
            return false;
        }
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
}
