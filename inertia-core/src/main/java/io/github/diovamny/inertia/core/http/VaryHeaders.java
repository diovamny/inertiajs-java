package io.github.diovamny.inertia.core.http;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Additive {@code Vary} header handling (Rails parity).
 *
 * <p>Never overwrites pre-existing {@code Vary} tokens (e.g.
 * {@code Accept-Encoding} set by proxies or the container). Tokens are merged
 * case-insensitively, preserving first-seen order. Framework bindings (Vert.x
 * {@code MultiMap}, JAX-RS maps, Spring {@code HttpHeaders}, servlet
 * responses) live in the adapters and delegate to {@link #merge}.</p>
 */
public final class VaryHeaders {

    private VaryHeaders() {
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
     * Merge a list of existing {@code Vary} values with tokens.
     *
     * @param existingValues existing values, may be {@code null}
     * @param tokens tokens to add
     * @return merged header value
     */
    public static String mergeAll(List<String> existingValues, String... tokens) {
        String existing = existingValues == null || existingValues.isEmpty()
            ? null
            : String.join(", ", existingValues);
        return merge(existing, tokens);
    }
}
