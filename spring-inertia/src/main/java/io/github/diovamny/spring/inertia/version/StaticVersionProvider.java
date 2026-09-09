package io.github.diovamny.spring.inertia.version;

import io.github.diovamny.spring.inertia.config.InertiaProperties;

/**
 * {@link VersionProvider} based on a fixed version string or on a hash of
 * the web assets.
 *
 * <p>Precedence (highest to lowest):
 * <ol>
 *   <li>Explicit custom version ({@code inertia.version-custom})</li>
 *   <li>Vite manifest hash ({@code .vite/manifest.json})</li>
 *   <li>Webroot asset fingerprint (SHA-256 of all files in META-INF/resources and static/)</li>
 *   <li>Stable fallback: build timestamp from MANIFEST.MF or class hash</li>
 * </ol>
 * </p>
 */
public class StaticVersionProvider extends AbstractVersionProvider {

    public StaticVersionProvider(InertiaProperties properties) {
        super(properties.getVersionCustom(), properties.getVersionStrategy());
    }
}
