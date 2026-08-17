package io.github.dg.spring.inertia.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import io.github.dg.spring.inertia.model.RawJson;

/**
 * Converts {@link RawJson} values into native structures before JSON
 * serialization so raw JSON strings are embedded as-is instead of being
 * escaped.
 */
public final class RawJsonUnwrapper {

    private RawJsonUnwrapper() {
    }

    /**
     * Recursively replace every {@link RawJson} in the value tree by its
     * parsed JSON structure.
     *
     * @param value  the value to convert
     * @param mapper the mapper used to parse raw JSON documents
     * @return a copy of the value without {@link RawJson} wrappers
     */
    public static Object unwrap(Object value, ObjectMapper mapper) {
        if (value instanceof RawJson raw) {
            return parse(raw.value(), mapper);
        }
        if (value instanceof Map<?, ?> map) {
            var result = new LinkedHashMap<String, Object>();
            for (var entry : map.entrySet()) {
                result.put(String.valueOf(entry.getKey()), unwrap(entry.getValue(), mapper));
            }
            return result;
        }
        if (value instanceof List<?> list) {
            var result = new ArrayList<Object>(list.size());
            for (var item : list) {
                result.add(unwrap(item, mapper));
            }
            return result;
        }
        return value;
    }

    /**
     * Whether the value tree contains any {@link RawJson} wrapper.
     *
     * @param value the value to inspect
     * @return {@code true} when at least one wrapper is present
     */
    public static boolean containsRawJson(Object value) {
        if (value instanceof RawJson) return true;
        if (value instanceof Map<?, ?> map) {
            for (var entry : map.entrySet()) {
                if (containsRawJson(entry.getValue())) return true;
            }
        } else if (value instanceof List<?> list) {
            for (var item : list) {
                if (containsRawJson(item)) return true;
            }
        }
        return false;
    }

    private static Object parse(String json, ObjectMapper mapper) {
        try {
            return fromNode(mapper.readTree(json));
        } catch (Exception e) {
            return json;
        }
    }

    private static Object fromNode(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isObject()) {
            var map = new LinkedHashMap<String, Object>();
            node.properties().forEach(entry -> map.put(entry.getKey(), fromNode(entry.getValue())));
            return map;
        }
        if (node.isArray()) {
            var list = new ArrayList<Object>();
            node.forEach(item -> list.add(fromNode(item)));
            return list;
        }
        if (node.isTextual()) return node.textValue();
        if (node.isBoolean()) return node.booleanValue();
        if (node.isIntegralNumber()) return node.longValue();
        if (node.isFloatingPointNumber()) return node.doubleValue();
        return node.asText();
    }
}