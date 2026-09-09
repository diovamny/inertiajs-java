package io.github.diovamny.spring.inertia.model;

import java.util.Map;

/**
 * Registration of a scroll prop: the value the client keeps across visits
 * and the metadata consumed by the InfiniteScroll component.
 *
 * @param value    the scroll value, or {@code null} when only metadata is set
 * @param wrapper  the element wrapping the prop in the frontend markup, or
 *                 {@code null} to use the default {@code "data"}
 * @param metadata extra scroll metadata (e.g. {@code previousPage},
 *                 {@code nextPage}, {@code currentPage}, {@code pageName},
 *                 {@code matchOn})
 */
public record ScrollProp(Object value, String wrapper, Map<String, Object> metadata) {

    /**
     * Create a metadata-only registration.
     *
     * @param metadata the scroll metadata
     * @return a scroll prop without a value
     */
    public static ScrollProp metadataOnly(Map<String, Object> metadata) {
        return new ScrollProp(null, null, metadata);
    }

    /**
     * The wrapper element name, defaulting to {@code "data"}.
     *
     * @return the wrapper name
     */
    public String wrapperOrData() {
        return wrapper != null && !wrapper.isBlank() ? wrapper : "data";
    }
}
