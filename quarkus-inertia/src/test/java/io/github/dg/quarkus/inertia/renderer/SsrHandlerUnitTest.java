package io.github.dg.quarkus.inertia.renderer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.dg.quarkus.inertia.config.InertiaConfig;

class SsrHandlerUnitTest {

    private SsrHandler handler;
    private InertiaConfig config;

    @BeforeEach
    void setUp() {
        var httpRequest = mock(HttpServerRequest.class);
        when(httpRequest.uri()).thenReturn("/admin/dashboard");

        var routingContext = mock(RoutingContext.class);
        when(routingContext.request()).thenReturn(httpRequest);

        var currentVertxRequest = mock(CurrentVertxRequest.class);
        when(currentVertxRequest.getCurrent()).thenReturn(routingContext);

        config = mock(InertiaConfig.class);
        when(config.ssrEnabled()).thenReturn(true);
        when(config.ssrExcludePaths()).thenReturn(Optional.of(List.of("/admin/*", "/public/secret")));

        handler = new SsrHandler(config, currentVertxRequest);
    }

    @Test
    void shouldBeDisabledWhenSsrIsOff() {
        when(config.ssrEnabled()).thenReturn(false);
        assertThat(handler.isSsrEnabled()).isFalse();
    }

    @Test
    void shouldBeEnabledForNonExcludedPath() {
        whenHttpRequestUri("/home");
        assertThat(handler.isSsrEnabled()).isTrue();
    }

    @Test
    void shouldBeDisabledForExactExcludedPath() {
        whenHttpRequestUri("/public/secret");
        assertThat(handler.isSsrEnabled()).isFalse();
    }

    @Test
    void shouldBeDisabledForWildcardExcludedPath() {
        whenHttpRequestUri("/admin/users");
        assertThat(handler.isSsrEnabled()).isFalse();
    }

    private void whenHttpRequestUri(String uri) {
        var httpRequest = mock(HttpServerRequest.class);
        when(httpRequest.uri()).thenReturn(uri);
        var routingContext = mock(RoutingContext.class);
        when(routingContext.request()).thenReturn(httpRequest);
        var currentVertxRequest = mock(CurrentVertxRequest.class);
        when(currentVertxRequest.getCurrent()).thenReturn(routingContext);
        handler = new SsrHandler(config, currentVertxRequest);
    }
}
