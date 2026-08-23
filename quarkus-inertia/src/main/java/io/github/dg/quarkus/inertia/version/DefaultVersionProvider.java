package io.github.dg.quarkus.inertia.version;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.vertx.core.Vertx;

import io.github.dg.quarkus.inertia.config.InertiaConfig;

/**
 * Default {@link VersionProvider}, bound to {@code inertia.version-strategy}:
 *
 * <p>Precedence (highest to lowest):</p>
 * <ol>
 *   <li>Explicit custom version ({@code inertia.version-custom})</li>
 *   <li>Vite manifest hash ({@code META-INF/resources/.vite/manifest.json})</li>
 *   <li>Webroot asset fingerprint (SHA-256 of all files in META-INF/resources/)</li>
 *   <li>Stable fallback: build timestamp from MANIFEST.MF or class hash</li>
 * </ol>
 *
 * <p>Runtime overrides through {@link #setVersion(String)} are stored per-request
 * in the Vert.x context to avoid cross-request contamination.</p>
 */
@ApplicationScoped
public class DefaultVersionProvider implements VersionProvider {

    private final String version;
    private static final String CONTEXT_VERSION_OVERRIDE = "inertia-version-override";

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
     * The current version: the runtime override when set for the current
     * request, otherwise the version computed at construction time.
     *
     * @return the version string
     */
    @Override
    public String getVersion() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var override = (String) ctx.getLocal(CONTEXT_VERSION_OVERRIDE);
            if (override != null) {
                return override;
            }
        }
        return version;
    }

    /**
     * Override the version for the current request only.
     *
     * @param version the new version
     */
    @Override
    public void setVersion(String version) {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal(CONTEXT_VERSION_OVERRIDE, version);
        }
    }

    private String computeVersion(InertiaConfig config) {
        var strategy = config.versionStrategy();
        var custom = config.versionCustom().orElse(null);

        // 1. Explicit custom version wins
        if (custom != null && !custom.isBlank()) {
            return custom;
        }

        // 2. Vite manifest strategy
        if ("vite-manifest".equalsIgnoreCase(strategy)) {
            String manifestHash = hashViteManifest();
            if (manifestHash != null) {
                return manifestHash;
            }
        }

        // 3. sha256 strategy (default) - fingerprint web assets
        if ("sha256".equalsIgnoreCase(strategy) || "auto".equalsIgnoreCase(strategy)) {
            String assetHash = hashWebAssets();
            if (assetHash != null) {
                return assetHash;
            }
        }

        // 4. Fallback: use build timestamp from manifest or stable hash
        return fallbackVersion();
    }

    private String hashViteManifest() {
        try (var is = getClass().getClassLoader().getResourceAsStream("META-INF/resources/.vite/manifest.json")) {
            if (is == null) return null;
            return hash(is.readAllBytes());
        } catch (IOException e) {
            return null;
        }
    }

    private String hashWebAssets() {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var found = false;

            // Scan META-INF/resources/
            found |= hashClasspathDirectory(digest, "META-INF/resources/");

            return found ? hex(digest.digest()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean hashClasspathDirectory(MessageDigest digest, String prefix) {
        java.util.Enumeration<java.net.URL> urls;
        try {
            urls = getClass().getClassLoader().getResources(prefix);
        } catch (IOException e) {
            return false;
        }
        var found = false;

        while (urls.hasMoreElements()) {
            var url = urls.nextElement();
            if ("file".equals(url.getProtocol())) {
                // Exploded directory
                try {
                    found |= hashFileDirectory(digest, Paths.get(url.toURI()), prefix);
                } catch (java.net.URISyntaxException e) {
                    // Ignore invalid URIs
                }
            } else if ("jar".equals(url.getProtocol())) {
                // JAR file
                found |= hashJarDirectory(digest, url, prefix);
            }
        }
        return found;
    }

    private boolean hashFileDirectory(MessageDigest digest, Path dir, String prefix) {
        if (!Files.isDirectory(dir)) return false;
        var found = false;
        try (var stream = Files.walk(dir)) {
            var files = stream.filter(Files::isRegularFile)
                .sorted(Comparator.comparing(Path::toString))
                .toList();
            for (var file : files) {
                found = true;
                var relativePath = dir.relativize(file).toString().replace('\\', '/');
                digest.update(relativePath.getBytes(StandardCharsets.UTF_8));
                digest.update(Files.readAllBytes(file));
            }
        } catch (IOException e) {
            // Ignore unreadable files
        }
        return found;
    }

    private boolean hashJarDirectory(MessageDigest digest, java.net.URL url, String prefix) {
        var urlStr = url.toString();
        var separator = urlStr.indexOf("!/");
        if (separator < 0) return false;

        var jarUrlStr = urlStr.substring(4, separator);
        var entryPrefix = urlStr.substring(separator + 2);

        try (var jarFile = new java.util.jar.JarFile(new java.net.URI(jarUrlStr).getPath())) {
            var found = false;
            var entries = new java.util.ArrayList<java.util.zip.ZipEntry>();
            var enumeration = jarFile.entries();
            while (enumeration.hasMoreElements()) {
                var entry = enumeration.nextElement();
                if (!entry.isDirectory() && entry.getName().startsWith(entryPrefix)) {
                    entries.add(entry);
                }
            }
            entries.sort(Comparator.comparing(java.util.zip.ZipEntry::getName));
            for (var entry : entries) {
                try (var is = jarFile.getInputStream(entry)) {
                    found = true;
                    var relativePath = entryPrefix.isEmpty()
                        ? entry.getName()
                        : entry.getName().substring(entryPrefix.length());
                    digest.update(relativePath.getBytes(StandardCharsets.UTF_8));
                    is.transferTo(new java.io.OutputStream() {
                        @Override
                        public void write(int b) {
                            digest.update((byte) b);
                        }
                    });
                }
            }
            return found;
        } catch (Exception e) {
            return false;
        }
    }

    private String fallbackVersion() {
        // Try to get build timestamp from manifest
        try (var in = getClass().getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF")) {
            if (in != null) {
                var manifest = new java.util.jar.Manifest(in);
                var attrs = manifest.getMainAttributes();
                var buildTime = attrs.getValue("Build-Time");
                if (buildTime != null && !buildTime.isBlank()) {
                    return hash(buildTime.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (Exception ignored) {
        }
        // Ultimate fallback: class hash (stable per build)
        return hash(getClass().getName().getBytes(StandardCharsets.UTF_8));
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