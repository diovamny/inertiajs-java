package io.github.dg.spring.inertia.version;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * {@link VersionProvider} based on a fixed version string or on a hash of
 * the web assets.
 *
 * <p>When {@code custom} is present it is returned as-is. Otherwise the
 * SHA-256 of the combined web-root bytes is computed: the strategy is
 * configurable through {@code inertia.version-strategy} ({@code sha256} by
 * default, {@code vite-manifest} to hash {@code .vite/manifest.json}
 * instead).</p>
 */
public class StaticVersionProvider implements VersionProvider {

    private final String version;

    public StaticVersionProvider(String custom) {
        this.version = custom != null ? custom : compute();
    }

    private static String compute() {
        var strategy = System.getProperty("inertia.version-strategy", "sha256");
        if ("vite-manifest".equalsIgnoreCase(strategy)) {
            return hashOf(classpath(".vite/manifest.json"));
        }
        return hashOf(
            classpath("static/app.js") +
            classpath("static/") +
            classpath("META-INF/resources/")
        );
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

    private static String classpath(String path) {
        try (var in = StaticVersionProvider.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                return "";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String version() {
        return version;
    }
}