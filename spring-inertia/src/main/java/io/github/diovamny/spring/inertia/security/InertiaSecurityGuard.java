package io.github.diovamny.spring.inertia.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import io.github.diovamny.spring.inertia.config.InertiaProperties;

/**
 * Startup guardrail for the security integration: logs a visible warning when
 * the adapter fallback is active (instead of Spring Security) and when the
 * legacy {@code inertia.csrf-enabled} alias is in use.
 *
 * <p>Hard failures (framework mode without the integration, fallback refusal,
 * disabled CSRF in production) are enforced earlier by the config validator;
 * this listener only reports.</p>
 */
public class InertiaSecurityGuard implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(InertiaSecurityGuard.class);

    private final InertiaProperties properties;

    public InertiaSecurityGuard(InertiaProperties properties) {
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        var configured = InertiaSecurityModes.configuredMode(properties);
        var effective = InertiaSecurityModes.effectiveMode(properties);
        if (InertiaSecurityModes.isLegacyAliasInUse(properties)) {
            LOG.warn("inertia.csrf-enabled is deprecated; set inertia.security.mode={} instead",
                effective.name().toLowerCase());
        }
        if (configured == InertiaSecurityModes.Mode.AUTO
                && effective == InertiaSecurityModes.Mode.ADAPTER) {
            LOG.warn("Inertia security mode=auto resolved to the adapter fallback "
                + "(Spring Security integration not detected). The adapter CSRF filter "
                + "protects Inertia visits only and is not a substitute for authentication "
                + "and authorization. Set inertia.security.fail-on-fallback=true to refuse "
                + "this fallback in production.");
        }
        if (effective == InertiaSecurityModes.Mode.DISABLED) {
            LOG.warn("Inertia CSRF protection is disabled (mode=disabled). "
                + "Only acceptable for tests or cookieless APIs.");
        }
    }
}
