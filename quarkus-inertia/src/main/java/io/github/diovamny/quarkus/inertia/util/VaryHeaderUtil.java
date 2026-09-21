package io.github.diovamny.quarkus.inertia.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Additive {@code Vary} header handling (Rails parity).
 *
 * <p>Never overwrites pre-existing {@code Vary} tokens (e.g.
 * {@code Accept-Encoding} set by proxies or the container). Tokens are merged
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
     * Add tokens to a Vert.x {@code MultiMap} {@code Vary} header.
     *
     * @param headers Vert.x headers
     * @param tokens tokens to add
     */
    public static void addTo(io.vertx.core.MultiMap headers, String... tokens) {
        String existing = headers.get("Vary");
        headers.set("Vary", merge(existing, tokens));
    }

    /**
     * Add tokens to a JAX-RS {@code MultivaluedMap} {@code Vary} header.
     *
     * @param headers JAX-RS headers
     * @param tokens tokens to add
     */
    public static void addTo(jakarta.ws.rs.core.MultivaluedMap<String, Object> headers, String... tokens) {
        Object first = headers.getFirst("Vary");
        headers.putSingle("Vary", merge(first != null ? String.valueOf(first) : null, tokens));
    }

    /**
     * Add tokens to a JAX-RS {@code ResponseBuilder} {@code Vary} header,
     * merging with any value already present on the builder.
     *
     * @param builder response builder
     * @param tokens tokens to add
     * @return the same builder
     */
    public static jakarta.ws.rs.core.Response.ResponseBuilder addTo(
            jakarta.ws.rs.core.Response.ResponseBuilder builder, String... tokens) {
        return builder.header("Vary", merge(null, tokens));
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
