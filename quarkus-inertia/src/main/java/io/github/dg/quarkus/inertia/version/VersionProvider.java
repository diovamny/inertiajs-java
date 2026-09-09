package io.github.dg.quarkus.inertia.version;

/**
 * Source of the asset version reported to the client for cache-busting.
 *
 * <p>The client compares it with the previous version and forces a full
 * reload when they differ. Implement this interface as a CDI bean to plug a
 * custom strategy; the default implementation honors
 * {@code inertia.version-strategy}.</p>
 */
public interface VersionProvider {

    /**
     * The current asset version.
     *
     * @return the version string
     */
    String getVersion();

    /**
     * Override the version at runtime.
     *
     * <p>Default is a no-op; only {@link DefaultVersionProvider} supports
     * runtime overrides.</p>
     *
     * @param version the new version
     */
    default void setVersion(String version) {
        // no-op for custom providers; DefaultVersionProvider supports runtime overrides
    }
}
