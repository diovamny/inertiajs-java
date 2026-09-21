package io.github.diovamny.spring.inertia.util;

import org.springframework.http.HttpHeaders;

import io.github.diovamny.inertia.core.http.VaryHeaders;

/**
 * Additive {@code Vary} header handling (Rails parity).
 *
 * <p>Framework binding over {@link VaryHeaders}: merges {@code Vary} tokens
 * on Spring and servlet header containers without dropping pre-existing
 * values. Pure merge logic lives in {@code inertia-core}.</p>
 */
public final class VaryHeaderUtil {

    private VaryHeaderUtil() {
    }

    /**
     * Merge existing {@code Vary} value with additional tokens.
     *
     * @param existing existing header value, may be {@code null}
     * @param tokens tokens to add
     * @return merged header value
     */
    public static String merge(String existing, String... tokens) {
        return VaryHeaders.merge(existing, tokens);
    }

    /**
     * Add tokens to {@code Vary} in Spring {@link HttpHeaders} without
     * dropping existing values.
     *
     * @param headers headers to mutate
     * @param tokens tokens to add
     */
    public static void addTo(HttpHeaders headers, String... tokens) {
        String existing = headers.getFirst("Vary");
        headers.set("Vary", merge(existing, tokens));
    }

    /**
     * Add tokens to {@code Vary} on a servlet response wrapper without
     * dropping existing values.
     *
     * @param response response carrying the header
     * @param tokens tokens to add
     */
    public static void addTo(jakarta.servlet.http.HttpServletResponse response, String... tokens) {
        String existing = response.getHeader("Vary");
        response.setHeader("Vary", merge(existing, tokens));
    }
}
