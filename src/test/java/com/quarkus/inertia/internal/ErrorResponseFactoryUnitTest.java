package com.quarkus.inertia.internal;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import com.quarkus.inertia.spi.ErrorMapper;

class ErrorResponseFactoryUnitTest {

    @Test
    void returns530WithErrorPayloadWithoutMapper() {
        var factory = new ErrorResponseFactory();
        var response = factory.handle(new IllegalStateException("boom"));

        assertThat(response.getStatus()).isEqualTo(530);
        assertThat(response.getHeaderString("X-Inertia")).isEqualTo("true");
        assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
        var entity = (Map<String, Object>) response.getEntity();
        var error = (Map<String, Object>) entity.get("error");
        assertThat(error).containsEntry("message", "boom");
    }

    @Test
    void usesRegisteredMapperWhenResolvable() {
        var mapper = (ErrorMapper) error -> Response.status(500).entity("custom").build();
        var factory = new ErrorResponseFactory() {
            @Override
            public ErrorMapper resolveMapper() {
                return mapper;
            }
        };

        var response = factory.handle(new RuntimeException());

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getEntity()).isEqualTo("custom");
    }
}
