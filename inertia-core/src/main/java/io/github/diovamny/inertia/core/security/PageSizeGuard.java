package io.github.diovamny.inertia.core.security;

import java.nio.charset.StandardCharsets;

/**
 * Enforces {@code inertia.max-page-bytes} on serialized page objects (§3.17
 * scenario 11). Both adapters check the JSON <em>after</em> serialization and
 * <em>before</em> writing the response, so an oversized page fails closed
 * with {@code 413} however it was produced (JSON visit or initial HTML).
 * A negative limit disables the check.
 */
public final class PageSizeGuard {

    private PageSizeGuard() {
    }

    /**
     * Check a serialized page payload against the limit.
     *
     * @param json the serialized page, may be {@code null}
     * @param maxBytes the configured limit, negative disables
     * @return the input, for call-site chaining
     * @throws PageTooLargeException when the UTF-8 size exceeds the limit
     */
    public static String check(String json, long maxBytes) {
        if (json == null || maxBytes < 0) {
            return json;
        }
        long bytes = json.getBytes(StandardCharsets.UTF_8).length;
        if (bytes > maxBytes) {
            throw new PageTooLargeException(bytes, maxBytes);
        }
        return json;
    }
}
