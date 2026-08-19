package io.github.dg.spring.inertia.testing;

import java.util.Map;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;

/**
 * Deserialized view of the Inertia page payload for tests.
 */
public record InertiaPage(
    String component,
    Map<String, Object> props,
    String url,
    String version,
    Map<String, Object> flash
) {

    private static final ObjectMapper DEFAULT = new ObjectMapper();

    /**
     * Parse a page payload.
     *
     * @param json the JSON document
     * @return the parsed page
     */
    public static InertiaPage fromJson(String json) {
        return fromJson(json, DEFAULT);
    }

    /**
     * Parse a page payload with a custom mapper.
     *
     * @param json   the JSON document
     * @param mapper the mapper to use
     * @return the parsed page
     */
    public static InertiaPage fromJson(String json, ObjectMapper mapper) {
        try {
            var node = mapper.readTree(json);
            Map<String, Object> props = mapper.convertValue(
                node.path("props"), new TypeReference<Map<String, Object>>() { });
            Map<String, Object> flash = node.has("flash")
                ? mapper.convertValue(node.path("flash"), new TypeReference<Map<String, Object>>() { })
                : null;
            return new InertiaPage(
                node.path("component").asText(),
                props,
                node.path("url").asText(),
                node.path("version").asText(null),
                flash);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Inertia page JSON", e);
        }
    }

    /**
     * The value of a prop.
     *
     * @param name the prop name
     * @return the value or {@code null}
     */
    public Object prop(String name) {
        return props != null ? props.get(name) : null;
    }

    /**
     * Whether the page carries the given prop.
     *
     * @param name the prop name
     * @return {@code true} when present
     */
    public boolean hasProp(String name) {
        return props != null && props.containsKey(name);
    }

    /**
     * The value of a top-level flash key.
     *
     * @param key the flash key
     * @return the value or {@code null} when absent
     */
    public Object flash(String key) {
        return flash != null ? flash.get(key) : null;
    }
}