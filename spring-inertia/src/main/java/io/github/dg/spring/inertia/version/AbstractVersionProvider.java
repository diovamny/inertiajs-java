package io.github.dg.spring.inertia.version;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Base class for version providers with deterministic asset fingerprinting.
 * <p>
 * Precedence (highest to lowest):
 * <ol>
 *   <li>Explicit custom version (configuration)</li>
 *   <li>Vite manifest hash ({@code .vite/manifest.json})</li>
 *   <li>Webroot asset fingerprint (SHA-256 of all files in META-INF/resources and static/)</li>
 *   <li>Stable fallback: hash of build timestamp from {@code META-INF/MANIFEST.MF}</li>
 * </ol>
 * </p>
 */
public abstract class AbstractVersionProvider implements VersionProvider {

    private final String version;

    protected AbstractVersionProvider(String customVersion, String strategy) {
        this.version = computeVersion(customVersion, strategy);
    }

    private String computeVersion(String customVersion, String strategy) {
        // 1. Explicit custom version wins
        if (customVersion != null && !customVersion.isBlank()) {
            return customVersion;
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

    /**
     * Hash the Vite manifest if present.
     */
    private String hashViteManifest() {
        try (var in = getClass().getClassLoader().getResourceAsStream(".vite/manifest.json")) {
            if (in == null) return null;
            return hash(in.readAllBytes());
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Compute deterministic SHA-256 of all web assets in classpath.
     * Scans both {@code META-INF/resources/} and {@code static/} directories.
     */
    private String hashWebAssets() {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var found = false;

            // Scan META-INF/resources/
            found |= hashClasspathDirectory(digest, "META-INF/resources/");
            // Scan static/
            found |= hashClasspathDirectory(digest, "static/");

            return found ? hex(digest.digest()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Recursively hash all files in a classpath directory.
     * Works for both exploded directories and JAR entries.
     */
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
                // JAR file - need to extract entries
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
        // URL format: jar:file:/path/to.jar!/META-INF/resources/
        var urlStr = url.toString();
        var separator = urlStr.indexOf("!/");
        if (separator < 0) return false;

        var jarUrlStr = urlStr.substring(4, separator); // Remove "jar:" prefix
        var entryPrefix = urlStr.substring(separator + 2); // Path inside JAR

        try (var jarFile = new JarFile(new java.net.URI(jarUrlStr).getPath())) {
            var found = false;
            var entries = new ArrayList<ZipEntry>();
            var enumeration = jarFile.entries();
            while (enumeration.hasMoreElements()) {
                var entry = enumeration.nextElement();
                if (!entry.isDirectory() && entry.getName().startsWith(entryPrefix)) {
                    entries.add(entry);
                }
            }
            entries.sort(Comparator.comparing(ZipEntry::getName));
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

    /**
     * Fallback version using build timestamp from MANIFEST.MF or a stable hash.
     */
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

    @Override
    public String version() {
        return version;
    }
}
