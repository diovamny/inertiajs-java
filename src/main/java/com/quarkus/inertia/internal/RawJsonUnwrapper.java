package com.quarkus.inertia.internal;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.quarkus.inertia.model.RawJson;

/**
 * Converts {@link RawJson} values into native structures before JSON
 * serialization so raw JSON strings are embedded as-is instead of being
 * escaped. Works with both JSON-B and Jackson-backed providers.
 */
public final class RawJsonUnwrapper {

    private RawJsonUnwrapper() {
    }

    public static Object unwrap(Object value) {
        if (value instanceof RawJson raw) {
            return parse(raw.value());
        }
        if (value instanceof Map<?, ?> map) {
            var result = new LinkedHashMap<String, Object>();
            for (var entry : map.entrySet()) {
                result.put(String.valueOf(entry.getKey()), unwrap(entry.getValue()));
            }
            return result;
        }
        if (value instanceof List<?> list) {
            var result = new ArrayList<Object>(list.size());
            for (var item : list) {
                result.add(unwrap(item));
            }
            return result;
        }
        return value;
    }

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

    private static Object parse(String json) {
        try (var reader = jakarta.json.Json.createReader(new StringReader(json))) {
            return fromJsonValue(reader.readValue());
        }
    }

    private static Object fromJsonValue(jakarta.json.JsonValue value) {
        switch (value.getValueType()) {
            case OBJECT -> {
                var map = new LinkedHashMap<String, Object>();
                var object = value.asJsonObject();
                for (var entry : object.entrySet()) {
                    map.put(entry.getKey(), fromJsonValue(entry.getValue()));
                }
                return map;
            }
            case ARRAY -> {
                var list = new ArrayList<Object>();
                value.asJsonArray().forEach(item -> list.add(fromJsonValue(item)));
                return list;
            }
            case STRING -> {
                return value.toString().substring(1, value.toString().length() - 1);
            }
            case NUMBER -> {
                var number = (jakarta.json.JsonNumber) value;
                var decimal = number.bigDecimalValue().stripTrailingZeros();
                if (decimal.scale() <= 0
                        && decimal.compareTo(java.math.BigDecimal.valueOf(Long.MAX_VALUE)) <= 0
                        && decimal.compareTo(java.math.BigDecimal.valueOf(Long.MIN_VALUE)) >= 0) {
                    return decimal.longValue();
                }
                return decimal.doubleValue();
            }
            case TRUE -> {
                return Boolean.TRUE;
            }
            case FALSE -> {
                return Boolean.FALSE;
            }
            case NULL -> {
                return null;
            }
            default -> {
                return null;
            }
        }
    }
}
