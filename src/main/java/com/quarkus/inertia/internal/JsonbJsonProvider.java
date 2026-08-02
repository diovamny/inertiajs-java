package com.quarkus.inertia.internal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;

import com.quarkus.inertia.spi.JsonProvider;

@ApplicationScoped
public class JsonbJsonProvider implements JsonProvider {

    private final Jsonb jsonb;

    @Inject
    public JsonbJsonProvider(Jsonb jsonb) {
        this.jsonb = jsonb;
    }

    @Override
    public String toJson(Object value) {
        return jsonb.toJson(value);
    }

    @Override
    public <T> T fromJson(String json, Class<T> type) {
        return jsonb.fromJson(json, type);
    }
}
