package io.github.diovamny.spring.inertia.util;

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.http.HttpHeaders;

/**
 * Additive {@code Vary} header handling (Rails parity).
 *
 * <p>Never overwrites pre-existing {@code Vary} tokens (e.g.
 * {@code Accept-Encoding} set by proxies). Tokens are merged
 * case-insensitively, preserving first-seen order.</p>
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
        Set<String> current = new LinkedHashSet<>();
        if (existing != null && !existing.isBlank()) {
            for (String item : existing.split(",")) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    current.add(trimmed);
                }
            }
        }
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            String trimmed = token.trim();
            boolean found = false;
            for (String item : current) {
                if (item.equalsIgnoreCase(trimmed)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                current.add(trimmed);
            }
        }
        return String.join(", ", current);
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
