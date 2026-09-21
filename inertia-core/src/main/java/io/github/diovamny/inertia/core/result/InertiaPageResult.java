package io.github.diovamny.inertia.core.result;

import java.util.Map;

/**
 * A typed page render request.
 *
 * @param component the frontend component, e.g. {@code "Contacts/Index"}
 * @param props     the page props
 * @param meta      extra page metadata, empty when absent
 */
public record InertiaPageResult(
        String component,
        Map<String, Object> props,
        Map<String, Object> meta) implements InertiaResult {

    public InertiaPageResult {
        if (component == null || component.isBlank()) {
            throw new IllegalArgumentException("component must not be blank");
        }
        props = props != null ? Map.copyOf(props) : Map.of();
        meta = meta != null ? Map.copyOf(meta) : Map.of();
    }

    /**
     * Create a page result with props and no metadata.
     */
    public static InertiaPageResult of(String component, Map<String, Object> props) {
        return new InertiaPageResult(component, props, Map.of());
    }

    /**
     * Create a page result with no props and no metadata.
     */
    public static InertiaPageResult of(String component) {
        return new InertiaPageResult(component, Map.of(), Map.of());
    }
}
