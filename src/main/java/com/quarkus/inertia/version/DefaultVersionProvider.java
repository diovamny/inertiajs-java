package com.quarkus.inertia.version;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.quarkus.inertia.config.InertiaConfig;

/**
 * Default {@link VersionProvider}, bound to {@code inertia.version-strategy}:
 *
 * <ul>
 *   <li>{@code sha256} (default): hash of the current time, so the version
 *       changes on every application restart;</li>
 *   <li>{@code vite-manifest}: SHA-256 of the Vite manifest resource
 *       ({@code META-INF/resources/.vite/manifest.json}), falling back to the
 *       time hash when absent;</li>
 *   <li>{@code custom}: the value of {@code inertia.version-custom}.</li>
 * </ul>
 *
 * <p>Also supports runtime overrides through {@link #setVersion(String)}.</p>
 */
@ApplicationScoped
public class DefaultVersionProvider implements VersionProvider {

    private final String version;
    private volatile String runtimeVersion;

    /**
     * Create the provider and compute the initial version from the config.
     *
     * @param config the Inertia configuration
     */
    @Inject
    public DefaultVersionProvider(InertiaConfig config) {
        this.version = computeVersion(config);
    }

    /**
     * The current version: the runtime override when set, otherwise the
     * version computed at construction time.
     *
     * @return the version string
     */
    @Override
    public String getVersion() {
        if (runtimeVersion != null) return runtimeVersion;
        return version;
    }

    /**
     * Override the version for the remaining application lifetime.
     *
     * @param version the new version
     */
    @Override
    public void setVersion(String version) {
        this.runtimeVersion = version;
    }

    private String computeVersion(InertiaConfig config) {
        var strategy = config.versionStrategy();
        if ("vite-manifest".equals(strategy)) {
            return loadVersionFromManifest();
        }
        if ("custom".equals(strategy)) {
            var custom = config.versionCustom();
            if (custom.isPresent() && !custom.get().isBlank()) {
                return custom.get();
            }
        }
        return fallbackVersion();
    }

    private String loadVersionFromManifest() {
        try (var is = getClass().getClassLoader().getResourceAsStream("META-INF/resources/.vite/manifest.json")) {
            if (is == null) return fallbackVersion();
            return hash(is.readAllBytes());
        } catch (Exception e) {
            return fallbackVersion();
        }
    }

    private String fallbackVersion() {
        return hash(Instant.now().toString().getBytes());
    }

    private String hash(byte[] input) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hex = new StringBuilder();
            for (byte b : digest.digest(input)) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            return "unknown";
        }
    }
}