package io.github.diovamny.inertia.core.result;

import java.util.Map;

/**
 * A typed page render request.
 *
 * @param component the frontend component, e.g. {@code "Contacts/Index"}
 * @param props     the page props
 * @param meta      extra page metadata, empty when absent
 * @param preserveBigIntegers whether the client must revive {@code $bigint}
 *                  markers into native {@code BigInt} for this page
 */
public record InertiaPageResult(
        String component,
        Map<String, Object> props,
        Map<String, Object> meta,
        boolean preserveBigIntegers) implements InertiaResult {

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
        return new InertiaPageResult(component, props, Map.of(), false);
    }

    /**
     * Create a page result with no props and no metadata.
     */
    public static InertiaPageResult of(String component) {
        return new InertiaPageResult(component, Map.of(), Map.of(), false);
    }

    /**
     * Create a page result that opts into BigInt revival for this response
     * (Inertia 3.8.0 {@code preserveBigIntegers} per-response opt-in).
     */
    public static InertiaPageResult of(String component, Map<String, Object> props,
            boolean preserveBigIntegers) {
        return new InertiaPageResult(component, props, Map.of(), preserveBigIntegers);
    }
}
