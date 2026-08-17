package io.github.dg.spring.inertia.version;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * {@link VersionProvider} hashing the Vite build manifest
 * ({@code .vite/manifest.json}) loaded from the classpath, so the version
 * changes whenever the frontend is rebuilt.
 */
public class ManifestVersionProvider implements VersionProvider {

    public static final String MANIFEST_PATH = ".vite/manifest.json";

    private final String version;

    public ManifestVersionProvider() {
        this.version = hashOf(load());
    }

    private static String load() {
        try (var in = ManifestVersionProvider.class.getClassLoader().getResourceAsStream(MANIFEST_PATH)) {
            if (in == null) {
                throw new IllegalStateException(
                    "ManifestVersionProvider requires " + MANIFEST_PATH + " on the classpath");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String hashOf(String content) {
        try {
            var digest = MessageDigest.getInstance("SHA-256")
                .digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    @Override
    public String version() {
        return version;
    }
}