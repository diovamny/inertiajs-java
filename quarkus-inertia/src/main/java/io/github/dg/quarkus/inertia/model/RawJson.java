package io.github.dg.quarkus.inertia.model;

import java.util.Objects;

/**
 * Wrapper for a raw JSON string that must be embedded verbatim in the page
 * object, without being re-serialized/escaped by the JSON provider.
 *
 * <p>Use it for values that are already serialized JSON documents, e.g.
 * cached page fragments or JSON produced by another service:</p>
 * <pre>{@code
 * inertia.render("Dashboard",
 *     Map.of("widget", inertia.rawJson("{\"kind\":\"chart\",\"data\":[1,2,3]}")))
 * }</pre>
 */
public final class RawJson {

    private final String json;

    private RawJson(String json) {
        this.json = Objects.requireNonNull(json, "json must not be null");
    }

    /**
     * Wrap a raw JSON document.
     *
     * @param json the JSON document as a string; must not be {@code null}
     * @return the wrapper
     */
    public static RawJson of(String json) {
        return new RawJson(json);
    }

    /**
     * Return the wrapped JSON document.
     *
     * @return the raw JSON string
     */
    public String value() {
        return json;
    }

    /**
     * The raw JSON document, so the wrapper prints as its content.
     *
     * @return the JSON string
     */
    @Override
    public String toString() {
        return json;
    }
}