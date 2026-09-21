package io.github.diovamny.spring.inertia.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Validates challenge URLs (login/OIDC) before they are emitted in
 * {@code X-Inertia-Location}: absolute paths are always same-origin safe,
 * absolute URLs are only allowed over {@code https} (OIDC providers).
 * Anything else falls back to {@code /login}.
 */
final class InertiaChallengeUrls {

    private InertiaChallengeUrls() {
    }

    static String validatedLoginUrl(String configured, HttpServletRequest request) {
        if (configured == null || configured.isBlank()) {
            return "/login";
        }
        var candidate = configured.trim();
        if (candidate.startsWith("/")) {
            return candidate;
        }
        try {
            var uri = new java.net.URI(candidate);
            if (uri.isAbsolute() && "https".equalsIgnoreCase(uri.getScheme())) {
                return candidate;
            }
        } catch (Exception ignored) {
            // fall through to the safe default
        }
        return "/login";
    }

    static boolean isInertiaVisit(HttpServletRequest request) {
        var value = request.getHeader("X-Inertia");
        return value != null && ("true".equalsIgnoreCase(value) || Boolean.parseBoolean(value));
    }
}
