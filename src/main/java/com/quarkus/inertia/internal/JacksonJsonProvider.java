package com.quarkus.inertia.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import com.quarkus.inertia.spi.JsonProvider;

/**
 * Jackson-backed {@link JsonProvider}, selected with priority 1 when the
 * application also registers {@link JsonbJsonProvider}; uses the request's
 * {@link ObjectMapper} and unwraps {@link RawJson} values beforehand.
 */
@Alternative
@Priority(1)
@ApplicationScoped
public class JacksonJsonProvider implements JsonProvider {

    private final ObjectMapper mapper;

    @Inject
    public JacksonJsonProvider(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String toJson(Object value) {
        try {
            var target = RawJsonUnwrapper.containsRawJson(value)
                ? RawJsonUnwrapper.unwrap(value)
                : value;
            return mapper.writeValueAsString(target);
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    @Override
    public <T> T fromJson(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }
}
