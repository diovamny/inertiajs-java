package io.github.diovamny.spring.inertia.security;

import java.security.SecureRandom;
import java.util.Base64;

import io.github.diovamny.spring.inertia.internal.InertiaRequestContext;

/**
 * Session-bound CSRF token for Inertia visits: the token lives in the
 * {@code HttpSession}, is exposed to the frontend as the
 * {@code XSRF-TOKEN} cookie and must be echoed back in the
 * {@code X-XSRF-TOKEN} (or {@code X-CSRF-TOKEN}) header.
 */
public class InertiaCsrfService {

    /** Session attribute holding the CSRF token. */
    public static final String SESSION_ATTR = "__inertia_csrf";

    /** Request attribute marking the request as CSRF-verified. */
    public static final String CONTEXT_HANDLED = "inertia-csrf-handled";

    /** Request attribute exposing the token to the response filter. */
    public static final String CONTEXT_TOKEN = "inertia-csrf-token";

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * The token for the current session, generating one on first use.
     *
     * @return the token
     */
    public String token() {
        var session = InertiaRequestContext.request() != null
            ? InertiaRequestContext.request().getSession(true)
            : null;
        if (session == null) {
            return "";
        }
        var stored = session.getAttribute(SESSION_ATTR);
        if (stored != null) {
            return String.valueOf(stored);
        }
        var token = generate();
        session.setAttribute(SESSION_ATTR, token);
        return token;
    }

    /**
     * Constant-time comparison of a submitted token against the session
     * token.
     *
     * @param submitted the header value
     * @return {@code true} when the tokens match
     */
    public boolean validate(String submitted) {
        if (submitted == null || submitted.isBlank()) {
            return false;
        }
        return MessageDigestEquals.equals(submitted, token());
    }

    /**
     * Whether the current request has been CSRF-verified.
     *
     * @return {@code true} when verified
     */
    public boolean isHandled() {
        return Boolean.TRUE.equals(InertiaRequestContext.get(CONTEXT_HANDLED));
    }

    /**
     * Mark the current request as CSRF-verified.
     */
    public void setHandled(boolean handled) {
        InertiaRequestContext.set(CONTEXT_HANDLED, handled);
    }

    /**
     * Expose the token to the response filter via the request attributes.
     *
     * @param token the token
     */
    public void setToken(String token) {
        InertiaRequestContext.set(CONTEXT_TOKEN, token);
    }

    private static String generate() {
        var bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static final class MessageDigestEquals {
        static boolean equals(String a, String b) {
            var aa = a.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            var bb = b.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            if (aa.length != bb.length) {
                return false;
            }
            int result = 0;
            for (int i = 0; i < aa.length; i++) {
                result |= aa[i] ^ bb[i];
            }
            return result == 0;
        }
    }
}
