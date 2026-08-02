package com.quarkus.inertia.version;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.quarkus.inertia.config.InertiaConfig;

@ApplicationScoped
public class DefaultVersionProvider implements VersionProvider {

    private final String version;
    private volatile String runtimeVersion;

    @Inject
    public DefaultVersionProvider(InertiaConfig config) {
        this.version = computeVersion(config);
    }

    @Override
    public String getVersion() {
        if (runtimeVersion != null) return runtimeVersion;
        return version;
    }

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
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(Instant.now().toString().getBytes());
            var hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String loadVersionFromManifest() {
        try (var is = getClass().getClassLoader().getResourceAsStream("META-INF/resources/.vite/manifest.json")) {
            if (is == null) return fallbackVersion();
            var bytes = is.readAllBytes();
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(bytes);
            var hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return fallbackVersion();
        }
    }

    private String fallbackVersion() {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(Instant.now().toString().getBytes());
            var hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            return "unknown";
        }
    }
}
