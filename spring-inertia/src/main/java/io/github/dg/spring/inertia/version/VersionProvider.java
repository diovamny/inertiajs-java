package io.github.dg.spring.inertia.version;

/**
 * Source of the asset version used for cache-busting and the
 * {@code X-Inertia-Version} header.
 */
public interface VersionProvider {

    /**
     * The current asset version.
     *
     * @return the version string; must not be {@code null}
     */
    String version();
}
