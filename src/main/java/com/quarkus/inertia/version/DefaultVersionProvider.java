package com.quarkus.inertia.version;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Comparator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.quarkus.inertia.config.InertiaConfig;

/**
 * Default {@link VersionProvider}, bound to {@code inertia.version-strategy}:
 *
 * <ul>
 *   <li>{@code sha256} (default): SHA-256 over the contents of the webroot
 *       ({@code META-INF/resources}), so the version changes whenever the
 *       frontend build output changes;</li>
 *   <li>{@code vite-manifest}: SHA-256 of the Vite manifest resource
 *       ({@code META-INF/resources/.vite/manifest.json});</li>
 *   <li>{@code custom}: the value of {@code inertia.version-custom}.</li>
 * </ul>
 *
 * <p>When no webroot is found, the version falls back to a startup-time hash
 * (changes on every application restart). Also supports runtime overrides
 * through {@link #setVersion(String)}.</p>
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
        if ("sha256".equals(strategy)) {
            return loadVersionFromWebroot();
        }
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

    private String loadVersionFromWebroot() {
        try {
            var resources = getClass().getClassLoader().getResources("META-INF/resources");
            var digest = MessageDigest.getInstance("SHA-256");
            var found = false;
            while (resources.hasMoreElements()) {
                var url = resources.nextElement();
                if (!"file".equals(url.getProtocol())) continue;
                var dir = Paths.get(url.toURI());
                if (!Files.isDirectory(dir)) continue;
                try (var stream = Files.walk(dir)) {
                    var files = stream.filter(Files::isRegularFile)
                        .sorted(Comparator.comparing(Path::toString))
                        .toList();
                    for (var file : files) {
                        found = true;
                        digest.update(Files.readAllBytes(file));
                    }
                }
            }
            return found ? hex(digest.digest()) : fallbackVersion();
        } catch (Exception e) {
            return fallbackVersion();
        }
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

    private String hex(byte[] bytes) {
        var hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
