package io.github.dg.spring.inertia.config;


/**
 * Validates the {@code inertia.*} configuration at startup and fails fast on
 * unsupported values.
 */
public class InertiaConfigValidator {

    private static final String[] STRATEGIES = {"sha256", "vite-manifest", "custom"};

    public InertiaConfigValidator(InertiaProperties properties) {
        validateVersionStrategy(properties.getVersionStrategy());
        validateStatus(properties.getErrorStatus());
        validateRootTemplate(properties.getRootTemplate());
        validateRootView(properties.getRootView());
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
