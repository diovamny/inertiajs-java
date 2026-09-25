package io.github.diovamny.inertia.core.protocol;

/**
 * Same-origin identity for redirect targets (protocol: redirects).
 *
 * <p>Canonical semantics, shared by both adapters (they previously diverged
 * on case sensitivity and default ports): scheme and host compare
 * case-insensitively (RFC 3986); default ports ({@code http:80},
 * {@code https:443}) normalize away; anything unparseable counts as external
 * (fail-closed: a broken target forces a full-page visit, never an internal
 * misroute). A leading {@code /} (including protocol-relative {@code //host}
 * paths, which carry no scheme) counts as internal, matching the adapters'
 * historical behavior.</p>
 */
public final class UrlIdentity {

    private UrlIdentity() {
    }

    /**
     * Whether a redirect target points outside the application.
     *
     * @param location the redirect target, relative or absolute
     * @param requestScheme the current request scheme (e.g. {@code https}), if known
     * @param requestAuthority the current request authority ({@code host} or
     *     {@code host:port}), if known
     * @return {@code true} when the target is external
     */
    public static boolean isExternal(String location, String requestScheme, String requestAuthority) {
        if (location == null || location.isBlank()) {
            return false;
        }
        if (location.startsWith("/")) {
            return false;
        }
        final java.net.URI uri;
        try {
            uri = new java.net.URI(location);
        } catch (Exception e) {
            return true;
        }
        if (!uri.isAbsolute()) {
            return false;
        }
        var host = uri.getHost();
        if (host == null || host.isBlank()) {
            return true;
        }
        if (requestScheme == null || requestAuthority == null
                || !requestScheme.equalsIgnoreCase(uri.getScheme())) {
            return true;
        }
        var wanted = splitAuthority(requestAuthority);
        if (wanted == null) {
            return true;
        }
        if (!wanted.host().equalsIgnoreCase(host)) {
            return true;
        }
        return effectivePort(uri.getScheme(), uri.getPort()) != effectivePort(requestScheme, wanted.port());
    }

    /**
     * Split an authority into host and port.
     *
     * @param authority {@code host}, {@code host:port}, {@code [::1]} or {@code [::1]:port}
     * @return the parts (port {@code -1} when absent), or {@code null} when unparseable
     */
    static HostPort splitAuthority(String authority) {
        if (authority == null || authority.isBlank()) {
            return null;
        }
        var value = authority.trim();
        if (value.startsWith("[")) {
            var close = value.indexOf(']');
            if (close < 0) {
                return null;
            }
            var host = value.substring(0, close + 1);
            var rest = value.substring(close + 1);
            if (rest.isEmpty()) {
                return new HostPort(host, -1);
            }
            if (!rest.startsWith(":")) {
                return null;
            }
            return new HostPort(host, parsePort(rest.substring(1)));
        }
        var colon = value.lastIndexOf(':');
        if (colon < 0) {
            return new HostPort(value, -1);
        }
        var port = parsePort(value.substring(colon + 1));
        if (port == null) {
            return null;
        }
        return new HostPort(value.substring(0, colon), port);
    }

    private static Integer parsePort(String raw) {
        try {
            var port = Integer.parseInt(raw);
            return port >= 0 && port <= 65535 ? port : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int effectivePort(String scheme, int port) {
        if (port >= 0) {
            return port;
        }
        if ("http".equalsIgnoreCase(scheme)) {
            return 80;
        }
        if ("https".equalsIgnoreCase(scheme)) {
            return 443;
        }
        return -1;
    }

    /**
     * A parsed authority.
     *
     * @param host host or bracketed IPv6 literal
     * @param port explicit port, or {@code -1} when absent
     */
    record HostPort(String host, int port) {
    }
}
