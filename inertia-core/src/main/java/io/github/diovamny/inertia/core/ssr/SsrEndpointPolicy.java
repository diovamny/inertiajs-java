package io.github.diovamny.inertia.core.ssr;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Pure validation policy for the SSR sidecar endpoint.
 *
 * <p>Default posture is local-only: {@code localhost}, {@code 127.0.0.1} and
 * {@code ::1} over local HTTP. A remote destination additionally requires,
 * all at once:</p>
 * <ul>
 *   <li>{@code remoteEnabled} ({@code inertia.ssr-remote-enabled=true});</li>
 *   <li>an {@code https} URL;</li>
 *   <li>a host present in {@code allowedHosts}
 *   ({@code inertia.ssr-allowed-hosts});</li>
 *   <li>no userinfo and no fragment;</li>
 *   <li>an HTTP client that never follows redirects to non-validated hosts
 *   (both adapters disable redirect-following and treat {@code 3xx} as a
 *   CSR-fallback failure).</li>
 * </ul>
 *
 * <p>Failures throw {@link IllegalArgumentException} with a safe message
 * (scheme + normalized host + port only — never tokens, credentials or page
 * payloads). Framework-free: no Spring, Quarkus or Vert.x dependency.</p>
 */
public final class SsrEndpointPolicy {

    private SsrEndpointPolicy() {
    }

    /**
     * Validate an SSR endpoint URL, failing fast on insecure destinations.
     *
     * @param ssrUrl       the configured endpoint (must not be blank)
     * @param remoteEnabled whether remote sidecars are explicitly enabled
     * @param allowedHosts  allowlisted remote hosts (case-insensitive)
     * @return the normalized {@code scheme://host:port} endpoint for safe logging
     * @throws IllegalArgumentException on any insecure or malformed destination
     */
    public static String validate(String ssrUrl, boolean remoteEnabled, Collection<String> allowedHosts) {
        if (ssrUrl == null || ssrUrl.isBlank()) {
            throw new IllegalArgumentException("inertia.ssr-url must not be blank");
        }
        var raw = ssrUrl.trim();
        final URI uri;
        try {
            uri = new URI(raw);
        } catch (Exception e) {
            throw new IllegalArgumentException("inertia.ssr-url is not a valid URI: " + safeHost(raw));
        }
        var scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase(Locale.ROOT) : null;
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new IllegalArgumentException(
                "inertia.ssr-url scheme must be http or https, got: " + safeScheme(scheme));
        }
        if (uri.getRawUserInfo() != null) {
            throw new IllegalArgumentException("inertia.ssr-url must not contain credentials (userinfo)");
        }
        if (uri.getRawFragment() != null) {
            throw new IllegalArgumentException("inertia.ssr-url must not contain a fragment");
        }
        var host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("inertia.ssr-url must contain a host, got: " + safeHost(raw));
        }
        var normalizedHost = normalizeHost(host);
        if (isLocalHost(normalizedHost)) {
            return normalizedEndpoint(scheme, normalizedHost, uri.getPort());
        }
        if (!remoteEnabled) {
            throw new IllegalArgumentException(
                "inertia.ssr-url points to remote host '" + normalizedHost
                    + "' but inertia.ssr-remote-enabled is false (default posture is localhost-only)");
        }
        if (!"https".equals(scheme)) {
            throw new IllegalArgumentException(
                "remote inertia.ssr-url must use https, got scheme '" + scheme + "' for host '"
                    + normalizedHost + "'");
        }
        var allowed = normalizeAll(allowedHosts);
        if (!allowed.contains(normalizedHost)) {
            throw new IllegalArgumentException(
                "remote inertia.ssr-url host '" + normalizedHost
                    + "' is not in inertia.ssr-allowed-hosts");
        }
        return normalizedEndpoint(scheme, normalizedHost, uri.getPort());
    }

    /** Whether a normalized host is the local sidecar. */
    public static boolean isLocalHost(String normalizedHost) {
        return "localhost".equals(normalizedHost)
            || "127.0.0.1".equals(normalizedHost)
            || "::1".equals(normalizedHost);
    }

    private static String normalizeHost(String host) {
        var clean = host.trim().toLowerCase(Locale.ROOT);
        if (clean.startsWith("[") && clean.endsWith("]") && clean.length() > 2) {
            clean = clean.substring(1, clean.length() - 1);
        }
        return clean;
    }

    private static List<String> normalizeAll(Collection<String> hosts) {
        var out = new ArrayList<String>();
        if (hosts == null) {
            return out;
        }
        for (var host : hosts) {
            if (host != null && !host.isBlank()) {
                out.add(normalizeHost(host));
            }
        }
        return out;
    }

    private static String normalizedEndpoint(String scheme, String host, int port) {
        var displayHost = host.contains(":") ? "[" + host + "]" : host;
        var effectivePort = port != -1 ? port
            : "https".equals(scheme) ? 443 : 80;
        return scheme + "://" + displayHost + ":" + effectivePort;
    }

    private static String safeScheme(String scheme) {
        return scheme == null ? "<none>" : scheme;
    }

    private static String safeHost(String raw) {
        if (raw == null) {
            return "<none>";
        }
        // Never echo potential credentials: show at most 64 chars of the raw value.
        var shown = raw.length() <= 64 ? raw : raw.substring(0, 64) + "...";
        return "'" + shown + "'";
    }

    /** Describe an allowlist for safe logging (hosts only, no ports/tokens). */
    public static String describeAllowlist(Collection<String> allowedHosts) {
        var normalized = normalizeAll(allowedHosts);
        return normalized.isEmpty() ? "<empty>" : String.join(",", normalized);
    }

    @Override
    public String toString() {
        return "SsrEndpointPolicy";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SsrEndpointPolicy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(SsrEndpointPolicy.class);
    }
}
