package io.github.dg.quarkus.inertia.internal;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import io.github.dg.quarkus.inertia.config.InertiaConfig;
import io.github.dg.quarkus.inertia.spi.ErrorMapper;
import io.github.dg.quarkus.inertia.version.VersionProvider;

class ErrorResponseFactoryUnitTest {

    private ErrorResponseFactory factory(int errorStatus, String errorComponent, String version) {
        var factory = new ErrorResponseFactory();
        factory.config = new InertiaConfig() {
            @Override public String rootTemplate() { return "index.html"; }
            @Override public boolean ssrEnabled() { return false; }
            @Override public String ssrUrl() { return "http://localhost:13714"; }
            @Override public String versionStrategy() { return "custom"; }
            @Override public java.util.Optional<String> versionCustom() { return java.util.Optional.of(version); }
            @Override public boolean encryptHistory() { return false; }
            @Override public boolean camelizeProps() { return false; }
            @Override public boolean csrfEnabled() { return true; }
            @Override public java.util.Optional<String> rootView() { return java.util.Optional.empty(); }
            @Override public java.util.Optional<java.util.List<String>> flashKeys() { return java.util.Optional.empty(); }
            @Override public boolean alwaysIncludeErrors() { return true; }
            @Override public int errorStatus() { return errorStatus; }
            @Override public String errorComponent() { return errorComponent; }
            @Override public boolean lazyEtagEnabled() { return true; }
            @Override public java.util.Optional<java.util.List<String>> ssrExcludePaths() { return java.util.Optional.empty(); }
        };
        factory.versionProvider = () -> version;
        return factory;
    }

    @Test
    void returnsErrorPageWithConfiguredStatusWithoutMapper() {
        var factory = factory(500, "ErrorPage", "v1.0.0");
        var response = factory.handle(new RuntimeException("boom"));

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getHeaderString("X-Inertia")).isEqualTo("true");
        assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
        var entity = (Map<String, Object>) response.getEntity();
        assertThat(entity).containsEntry("component", "ErrorPage");
        assertThat(entity).containsEntry("version", "v1.0.0");
        var props = (Map<String, Object>) entity.get("props");
        assertThat(props).containsEntry("message", "boom");
        assertThat(props).containsEntry("status", 500);
    }

    @Test
    void usesExplicitStatusForWebApplicationExceptions() {
        var factory = factory(500, "ErrorPage", "v1.0.0");
        var response = factory.handle(new IllegalStateException("forbidden"), 403);

        assertThat(response.getStatus()).isEqualTo(403);
        var entity = (Map<String, Object>) response.getEntity();
        var props = (Map<String, Object>) entity.get("props");
        assertThat(props).containsEntry("status", 403);
    }

    @Test
    void usesRegisteredMapperWhenResolvable() {
        var mapper = (ErrorMapper) error -> Response.status(500).entity("custom").build();
        var base = factory(500, "ErrorPage", "v1.0.0");
        ErrorResponseFactory factory = new ErrorResponseFactory() {
            @Override
            public ErrorMapper resolveMapper() {
                return mapper;
            }
        };
        factory.config = base.config;
        factory.versionProvider = base.versionProvider;

        var response = factory.handle(new RuntimeException());

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getEntity()).isEqualTo("custom");
    }

    @Test
    void mapsBusinessExceptionsToSemanticStatuses() {
        var factory = factory(500, "ErrorPage", "v1.0.0");

        assertThat(factory.statusFor(new IllegalArgumentException("bad")))
            .isEqualTo(400);
        assertThat(factory.statusFor(new SecurityException("denied")))
            .isEqualTo(403);
        assertThat(factory.statusFor(new IllegalStateException("state")))
            .isEqualTo(409);
        assertThat(factory.statusFor(new jakarta.validation.ValidationException("rule")))
            .isEqualTo(422);
    }

    @Test
    void mapsCauseWhenWrapped() {
        var factory = factory(500, "ErrorPage", "v1.0.0");

        assertThat(factory.statusFor(new RuntimeException(new IllegalArgumentException("bad"))))
            .isEqualTo(400);
    }

    @Test
    void fallsBackToConfiguredStatusForUnknownExceptions() {
        var factory = factory(500, "ErrorPage", "v1.0.0");

        assertThat(factory.statusFor(new RuntimeException("boom"))).isEqualTo(500);
        assertThat(factory.statusFor(null)).isEqualTo(500);
        assertThat(factory.statusFor(new jakarta.validation.ConstraintViolationException("cve", Set.of())))
            .isEqualTo(500);
    }
}
