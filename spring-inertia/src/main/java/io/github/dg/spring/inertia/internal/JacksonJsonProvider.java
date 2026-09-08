package io.github.dg.spring.inertia.internal;

import tools.jackson.databind.ObjectMapper;

import io.github.dg.spring.inertia.model.RawJson;
import io.github.dg.spring.inertia.spi.JsonProvider;

/**
 * Jackson-backed {@link JsonProvider} over the {@code ObjectMapper} managed
 * by Spring Boot; unwraps {@link RawJson} values beforehand.
 */
public class JacksonJsonProvider implements JsonProvider {

    private final ObjectMapper mapper;

    public JacksonJsonProvider(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String toJson(Object value) {
        try {
            var target = RawJsonUnwrapper.containsRawJson(value)
                ? RawJsonUnwrapper.unwrap(value, mapper)
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
