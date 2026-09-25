package io.github.diovamny.inertia.core.protocol;

/**
 * Asset-version staleness decision (protocol: asset versioning).
 *
 * <p>A mismatch on a {@code GET} Inertia request (that is not a prefetch)
 * produces {@code 409} with the current version, so the client reloads fresh
 * assets. Non-{@code GET} requests never 409 (the follow-up {@code GET}
 * redirect still may). Missing versions on either side mean "untracked",
 * never stale.</p>
 */
public final class VersionPolicy {

    private VersionPolicy() {
    }

    /**
     * Whether the request carries a stale asset version.
     *
     * @param method the HTTP method, case-insensitive ({@code GET} expected)
     * @param clientVersion the {@code X-Inertia-Version} value, if any
     * @param serverVersion the current asset version, if any
     * @param prefetch whether this is a prefetch visit (never stale)
     * @return {@code true} when the server must answer {@code 409}
     */
    public static boolean isStale(String method, String clientVersion, String serverVersion,
            boolean prefetch) {
        if (prefetch) {
            return false;
        }
        if (method == null || !"GET".equalsIgnoreCase(method)) {
            return false;
        }
        if (clientVersion == null || serverVersion == null) {
            return false;
        }
        return !clientVersion.equals(serverVersion);
    }

    /**
     * The {@code 409} control payload for a stale version.
     *
     * @param location the URL the client must visit
     * @param serverVersion the current asset version
     */
    public record VersionMismatch(String location, String serverVersion) {
    }

    /**
     * Build the {@code 409} control payload for a stale version.
     *
     * @param url the request URL (reload target)
     * @param serverVersion the current asset version
     * @return the mismatch payload
     */
    public static VersionMismatch mismatch(String url, String serverVersion) {
        return new VersionMismatch(url, serverVersion);
    }
}
