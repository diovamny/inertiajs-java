package io.github.diovamny.inertia.tck;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single normative protocol case: one HTTP request plus the expectations
 * every certified adapter must satisfy. Stack-specific divergences (where the
 * adapters' documented contracts genuinely differ, e.g. adapter-mode CSRF
 * failure codes) live in the {@code expect-spring} / {@code expect-quarkus}
 * overrides, deep-merged over {@code expect}.
 */
public final class TckCase {

    private final String id;
    private final String description;
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final String body;
    private final String contentType;
    private final Map<String, Object> multipart;
    private final Map<String, Object> expect;
    private final Map<String, Object> expectSpring;
    private final Map<String, Object> expectQuarkus;

    @SuppressWarnings("unchecked")
    public TckCase(Map<String, Object> raw) {
        this.id = required(raw, "id");
        this.description = String.valueOf(raw.getOrDefault("description", ""));
        var request = (Map<String, Object>) raw.getOrDefault("request", Map.of());
        this.method = String.valueOf(request.getOrDefault("method", "GET"));
        this.path = required(request, "path");
        this.headers = strings((Map<String, Object>) request.getOrDefault("headers", Map.of()));
        var rawBody = request.get("body");
        this.body = rawBody != null ? String.valueOf(rawBody) : null;
        this.contentType = (String) request.get("contentType");
        this.multipart = (Map<String, Object>) request.get("multipart");
        this.expect = (Map<String, Object>) raw.getOrDefault("expect", Map.of());
        this.expectSpring = (Map<String, Object>) raw.get("expect-spring");
        this.expectQuarkus = (Map<String, Object>) raw.get("expect-quarkus");
    }

    private static String required(Map<String, Object> map, String key) {
        var value = map.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("TCK case is missing required key: " + key);
        }
        return value.toString();
    }

    private static Map<String, String> strings(Map<String, Object> map) {
        var result = new LinkedHashMap<String, String>();
        map.forEach((k, v) -> {
            if (v != null) {
                result.put(k, v.toString());
            }
        });
        return Collections.unmodifiableMap(result);
    }

    public String id() {
        return id;
    }

    public String description() {
        return description;
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public String body() {
        return body;
    }

    public String contentType() {
        return contentType;
    }

    /**
     * Multipart spec ({@code boundary} + {@code parts} list), or {@code null}
     * for a plain body. When present the runner builds a CRLF-correct
     * {@code multipart/form-data} payload.
     */
    public Map<String, Object> multipart() {
        return multipart;
    }

    /**
     * Expectations for the given stack after deep-merging the stack override.
     */
    public Map<String, Object> expectFor(String stack) {
        Map<String, Object> override = "spring".equals(stack) ? expectSpring
            : "quarkus".equals(stack) ? expectQuarkus : null;
        if (override == null || override.isEmpty()) {
            return expect;
        }
        return deepMerge(expect, override);
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> deepMerge(Map<String, Object> base, Map<String, Object> override) {
        var merged = new LinkedHashMap<>(base);
        override.forEach((key, value) -> {
            var current = merged.get(key);
            if (current instanceof Map && value instanceof Map) {
                merged.put(key, deepMerge((Map<String, Object>) current, (Map<String, Object>) value));
            } else {
                merged.put(key, value);
            }
        });
        return merged;
    }
}
