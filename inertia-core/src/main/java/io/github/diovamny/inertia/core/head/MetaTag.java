package io.github.diovamny.inertia.core.head;

import java.util.Map;

/**
 * A single server-rendered head tag with its deduplication key.
 *
 * @param headKey    unique key within the page ({@code title},
 *                   {@code meta-description}, {@code og:title},
 *                   {@code link-canonical}, ...)
 * @param tag        the HTML tag ({@code title}, {@code meta}, {@code link})
 * @param attributes tag attributes in order
 */
public record MetaTag(String headKey, String tag, Map<String, String> attributes) {

    public MetaTag {
        if (headKey == null || headKey.isBlank()) {
            throw new IllegalArgumentException("headKey must not be blank");
        }
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("tag must not be blank");
        }
        attributes = attributes != null ? Map.copyOf(attributes) : Map.of();
    }
}
