package com.quarkus.inertia.api;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.core.Response;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.quarkus.inertia.protocol.RedirectProcessor;
import com.quarkus.inertia.spi.FlashStore;

class InertiaRedirectUnitTest {

    private FlashStore flashStore;
    private RedirectProcessor processor;
    private HttpServerRequest httpRequest;

    @BeforeEach
    void setUp() {
        flashStore = mock(FlashStore.class);

        var currentVertxRequest = mock(CurrentVertxRequest.class);
        httpRequest = mock(HttpServerRequest.class);
        when(httpRequest.getHeader("X-Inertia")).thenReturn("true");
        when(httpRequest.method()).thenReturn(HttpMethod.POST);
        when(httpRequest.host()).thenReturn("localhost:8080");
        when(httpRequest.scheme()).thenReturn("http");
        when(httpRequest.getHeader("Referer")).thenReturn("/previous");

        var routingContext = mock(RoutingContext.class);
        when(routingContext.request()).thenReturn(httpRequest);
        when(currentVertxRequest.getCurrent()).thenReturn(routingContext);

        processor = new RedirectProcessor(currentVertxRequest);
    }

    private InertiaRedirect back() {
        return new InertiaRedirect(processor.back("/fallback"), flashStore);
    }

    private InertiaRedirect redirect() {
        return new InertiaRedirect(processor.process("/home"), flashStore);
    }

    @Test
    void shouldResolveAsUniToRedirect() {
        var result = back();
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(303);
        assertThat(response.getHeaderString("Location")).isEqualTo("/previous");
    }

    @Test
    void redirectShouldFlashAndReturnSameInstance() {
        var result = redirect();
        assertThat(result.with("success", "Contact created.")).isSameAs(result);
        verify(flashStore).put("success", "Contact created.");
    }

    @Test
    void redirectShouldResolveAsUniToRedirect() {
        var result = redirect().with("success", "Contact created.");
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(303);
        assertThat(response.getHeaderString("Location")).isEqualTo("/home");
    }

    @Test
    void withShouldFlashAndReturnSameInstance() {
        var result = back();
        assertThat(result.with("success", "Registro actualizado correctamente.")).isSameAs(result);
        verify(flashStore).put("success", "Registro actualizado correctamente.");
    }

    @Test
    void flashShouldBeAliasOfWith() {
        var result = back();
        assertThat(result.flash("warning", "cuidado")).isSameAs(result);
        verify(flashStore).put("warning", "cuidado");
    }

    @Test
    void withMapShouldFlashAllValues() {
        var result = back();
        java.util.Map<String, Object> values = java.util.Map.of("success", "ok", "warning", "cuidado");
        assertThat(result.with(values)).isSameAs(result);
        verify(flashStore).putAll(values);
    }

    @Test
    void withErrorsShouldFlashUnderErrorsKey() {
        var result = back();
        var errors = java.util.Map.of("email", "El correo electrónico no es válido.");
        assertThat(result.withErrors(errors)).isSameAs(result);
        verify(flashStore).put("errors", errors);
    }

    @Test
    void withInputShouldFlashUnderInputKey() {
        var result = back();
        var input = java.util.Map.<String, Object>of("email", "correo@mal.com");
        assertThat(result.withInput(input)).isSameAs(result);
        verify(flashStore).put("input", input);
    }

    @Test
    void chainedCallsShouldFlashEveryValue() {
        var result = back();
        result.with("success", "ok").withErrors(java.util.Map.of("email", "invalid"));
        verify(flashStore).put("success", "ok");
        verify(flashStore).put("errors", java.util.Map.of("email", "invalid"));
    }

    @Test
    void chainedRedirectShouldStillResolve() {
        var result = back().with("success", "ok").withErrors(java.util.Map.of("email", "invalid"));
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(303);
        assertThat(response.getHeaderString("Location")).isEqualTo("/previous");
    }
}
