package com.quarkus.inertia.internal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;

import com.quarkus.inertia.spi.JsonProvider;

/**
 * JSON-B backed {@link JsonProvider}, the default when Jackson is not
 * installed as a CDI bean; unwraps {@link RawJson} values beforehand.
 */
@ApplicationScoped
public class JsonbJsonProvider implements JsonProvider {

    private final Jsonb jsonb;

    @Inject
    public JsonbJsonProvider(Jsonb jsonb) {
        this.jsonb = jsonb;
    }

    @Override
    public String toJson(Object value) {
        var target = RawJsonUnwrapper.containsRawJson(value)
            ? RawJsonUnwrapper.unwrap(value)
            : value;
        return jsonb.toJson(target);
    }

    @Override
    public <T> T fromJson(String json, Class<T> type) {
        return jsonb.fromJson(json, type);
    }
}
