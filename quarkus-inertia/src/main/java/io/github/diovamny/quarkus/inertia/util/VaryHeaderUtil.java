package io.github.diovamny.quarkus.inertia.util;

import java.util.List;

import io.github.diovamny.inertia.core.http.VaryHeaders;

/**
 * Additive {@code Vary} header handling (Rails parity).
 *
 * <p>Framework binding over {@link VaryHeaders}: merges {@code Vary} tokens
 * on Vert.x and JAX-RS header containers without dropping pre-existing
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
        return VaryHeaders.mergeAll(existingValues, tokens);
    }
}
