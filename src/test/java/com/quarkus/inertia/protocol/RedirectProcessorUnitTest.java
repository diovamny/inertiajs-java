package com.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.core.Response;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RedirectProcessorUnitTest {

    private RedirectProcessor processor;
    private HttpServerRequest httpRequest;
    private RoutingContext routingContext;

    @BeforeEach
    void setUp() {
        var currentVertxRequest = mock(CurrentVertxRequest.class);
        httpRequest = mock(HttpServerRequest.class);
        when(httpRequest.getHeader("X-Inertia")).thenReturn(null);
        when(httpRequest.method()).thenReturn(HttpMethod.GET);
        when(httpRequest.host()).thenReturn("localhost:8080");
        when(httpRequest.scheme()).thenReturn("http");

        routingContext = mock(RoutingContext.class);
        when(routingContext.request()).thenReturn(httpRequest);
        when(currentVertxRequest.getCurrent()).thenReturn(routingContext);

        processor = new RedirectProcessor(currentVertxRequest);
    }

    private void asInertia() {
        when(httpRequest.getHeader("X-Inertia")).thenReturn("true");
    }

    @Test
    void shouldReturn302ForRedirect() {
        var result = processor.process("/home");
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaderString("Location")).isEqualTo("/home");
    }

    @Test
    void shouldReturn302ForExternalLocationWhenNonInertia() {
        var result = processor.external("https://example.com");
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaderString("Location")).isEqualTo("https://example.com");
    }

    @Test
    void shouldReturn409ForExternalLocationWhenInertia() {
        asInertia();
        var result = processor.external("https://example.com");
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(409);
        assertThat(response.getHeaderString("X-Inertia-Location")).isEqualTo("https://example.com");
        assertThat(response.getHeaderString("Location")).isNull();
    }

    @Test
    void shouldReturn302ForInternalLocationWhenInertia() {
        asInertia();
        var result = processor.external("/home");
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaderString("Location")).isEqualTo("/home");
        assertThat(response.getHeaderString("X-Inertia-Location")).isNull();
    }

    @Test
    void shouldReturn409ForFullPageRedirectWhenInertia() {
        asInertia();
        var result = processor.process("/admin", true);
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(409);
        assertThat(response.getHeaderString("X-Inertia-Location")).isEqualTo("/admin");
        assertThat(response.getHeaderString("Location")).isNull();
    }

    @Test
    void shouldReturn302ForFullPageRedirectWhenNonInertia() {
        var result = processor.process("/admin", true);
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaderString("Location")).isEqualTo("/admin");
        assertThat(response.getHeaderString("X-Inertia-Location")).isNull();
    }

    @Test
    void shouldReturn302ForSameHostLocationWhenInertia() {
        asInertia();
        var result = processor.external("http://localhost:8080/other");
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaderString("Location")).isEqualTo("http://localhost:8080/other");
        assertThat(response.getHeaderString("X-Inertia-Location")).isNull();
    }

    @Test
    void shouldBackToRefererWhenPresent() {
        when(httpRequest.getHeader("Referer")).thenReturn("/previous");
        var result = processor.back();
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaderString("Location")).isEqualTo("/previous");
    }

    @Test
    void shouldBackToFallbackWhenNoReferer() {
        var result = processor.back("/fallback");
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaderString("Location")).isEqualTo("/fallback");
    }

    @Test
    void shouldBackWithCustomStatusAndFallback() {
        var result = processor.back(303, "/fallback");
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(303);
        assertThat(response.getHeaderString("Location")).isEqualTo("/fallback");
    }

    @Test
    void shouldPreferRefererOverCustomFallback() {
        when(httpRequest.getHeader("Referer")).thenReturn("/previous");
        var result = processor.back(303, "/fallback");
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(303);
        assertThat(response.getHeaderString("Location")).isEqualTo("/previous");
    }
}
