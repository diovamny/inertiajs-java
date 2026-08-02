package com.quarkus.inertia.model;

import java.util.Objects;

public final class RawJson {

    private final String json;

    private RawJson(String json) {
        this.json = Objects.requireNonNull(json, "json must not be null");
    }

    public static RawJson of(String json) {
        return new RawJson(json);
    }

    public String value() {
        return json;
    }

    @Override
    public String toString() {
        return json;
    }
}
