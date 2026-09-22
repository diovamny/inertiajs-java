package io.github.diovamny.inertia.core.security;

import java.util.Locale;

/**
 * Fail-closed validation for Inertia redirect targets (CRLF/header-injection
 * and dangerous-scheme hardening, §3.17 scenarios 3–4).
 *
 * <p>The check runs <em>before</em> any {@code Location} /
 * {@code X-Inertia-Location} header is emitted, in both adapters, so a
 * malicious target can never split a header. Rejected targets throw
 * {@link IllegalArgumentException} (Quarkus maps it to 400 via
 * {@code ErrorResponseFactory#statusFor}; Spring fails closed through the
 * error handler without emitting the header).
 */
public final class RedirectTargets {

    private RedirectTargets() {
    }

    /**
     * Validate a redirect target.
     *
     * @param url the app-supplied target, may be {@code null}
     * @return the normalized target (leading/trailing whitespace stripped)
     * @throws IllegalArgumentException when the target carries control
     *         characters or a forbidden scheme
     */
    public static String check(String url) {
        if (url == null) {
            return null;
        }
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c < 0x20 || c == 0x7F) {
                throw new IllegalArgumentException(
                    "Redirect target must not contain control characters");
            }
        }
        var normalized = url.strip();
        var lower = normalized.toLowerCase(Locale.ROOT);
        if (lower.startsWith("javascript:") || lower.startsWith("data:")
                || lower.startsWith("vbscript:") || lower.startsWith("file:")) {
            throw new IllegalArgumentException("Redirect target uses a forbidden scheme");
        }
        return normalized;
    }
}
