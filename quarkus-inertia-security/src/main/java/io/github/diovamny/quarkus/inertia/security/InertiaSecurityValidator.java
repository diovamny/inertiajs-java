package io.github.diovamny.quarkus.inertia.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.Config;
import org.jboss.logging.Logger;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;

/**
 * Startup guardrail for the Quarkus Security integration: in effective mode
 * {@code framework} token verification must stay enabled and must cover JSON
 * posts. Fails fast otherwise, because the adapter stands down its own JAX-RS
 * CSRF filter in that mode and silent unprotection would follow.
 */
@ApplicationScoped
public class InertiaSecurityValidator {

    private static final Logger LOG = Logger.getLogger(InertiaSecurityValidator.class);

    private final InertiaConfig config;
    private final Config rawConfig;

    @Inject
    public InertiaSecurityValidator(InertiaConfig config, Config rawConfig) {
        this.config = config;
        this.rawConfig = rawConfig;
    }

    void onStart(@Observes StartupEvent event) {
        if (InertiaSecurityModes.effectiveMode(config) != InertiaSecurityModes.Mode.FRAMEWORK) {
            return;
        }
        var verify = rawConfig.getOptionalValue("quarkus.rest-csrf.verify-token", Boolean.class);
        if (verify.isPresent() && !verify.get()) {
            throw new IllegalStateException(
                "inertia.security.mode=framework requires quarkus.rest-csrf.verify-token=true "
                    + "(disabling verification while the adapter stands down is silent unprotection)");
        }
        var formOnly = rawConfig.getOptionalValue("quarkus.rest-csrf.require-form-url-encoded",
            Boolean.class);
        if (formOnly.isEmpty() || formOnly.get()) {
            throw new IllegalStateException(
                "inertia.security.mode=framework requires "
                    + "quarkus.rest-csrf.require-form-url-encoded=false: "
                    + "Inertia posts JSON, which the default form-only check skips");
        }
        // A rest-csrf signature key signs the cookie, so a client that only
        // echoes the cookie (every official Inertia client) can never pass
        // verification: the check requires cookie == sign(header), which no
        // echo satisfies (verified against quarkus-rest-csrf 3.39.2 bytecode).
        // Inertia SPAs must therefore leave the key UNSET (plain double-submit
        // + SameSite + HTTPS). Only server-rendered flows that embed the raw
        // token (e.g. Qute `{csrfToken}`) may use a signature key.
        var key = rawConfig.getOptionalValue("quarkus.rest-csrf.token-signature-key", String.class);
        if (key.isPresent() && !key.get().isBlank()) {
            LOG.warn("quarkus.rest-csrf.token-signature-key is set: official Inertia "
                + "clients echo the XSRF-TOKEN cookie and cannot pass signed "
                + "verification (cookie must equal sign(header)). Unset the key "
                + "for Inertia SPA frontends; keep it only for server-rendered "
                + "raw-token flows.");
        }
        LOG.info("Inertia Quarkus Security bridge active: 401 -> 409 challenges, "
            + "403 Inertia pages, rest-csrf failure -> 303 recovery");
    }
}
