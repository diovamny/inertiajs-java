package io.github.dg.quarkus.inertia.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.runtime.StartupEvent;

/**
 * Validates the Inertia configuration at application startup: the root
 * template must not be blank and the version strategy must be one of the
 * supported values.
 */
@ApplicationScoped
public class InertiaConfigValidator {

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
    }
}
