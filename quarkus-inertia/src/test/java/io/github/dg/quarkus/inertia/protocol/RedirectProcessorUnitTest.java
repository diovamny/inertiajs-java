package io.github.dg.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.net.HostAndPort;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.dg.quarkus.inertia.spi.FlashStore;

class RedirectProcessorUnitTest {

    private RedirectProcessor processor;
    private HttpServerRequest httpRequest;
    private RoutingContext routingContext;
    private FlashStore flashStore;

    @BeforeEach
    void setUp() {
        var currentVertxRequest = mock(CurrentVertxRequest.class);
        httpRequest = mock(HttpServerRequest.class);
        flashStore = mock(FlashStore.class);
        when(httpRequest.getHeader("X-Inertia")).thenReturn(null);
        when(httpRequest.method()).thenReturn(HttpMethod.GET);
        when(httpRequest.authority()).thenReturn(HostAndPort.create("localhost", 8080));
        when(httpRequest.scheme()).thenReturn("http");

        routingContext = mock(RoutingContext.class);
        when(routingContext.request()).thenReturn(httpRequest);
        when(currentVertxRequest.getCurrent()).thenReturn(routingContext);

        processor = new RedirectProcessor(currentVertxRequest, flashStore);
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

    @Test
    void shouldBackWithHeaders() {
        var result = processor.back(303, java.util.Map.of("X-Custom", "custom-value", "X-Another", "another"));
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(303);
        assertThat(response.getHeaderString("Location")).isEqualTo("/");
        assertThat(response.getHeaderString("X-Custom")).isEqualTo("custom-value");
        assertThat(response.getHeaderString("X-Another")).isEqualTo("another");
    }

    @Test
    void shouldBackWithStatusHeadersAndFallback() {
        when(httpRequest.getHeader("Referer")).thenReturn("/previous");
        var result = processor.back(302, java.util.Map.of("X-Custom", "custom-value"), "/fallback");
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaderString("Location")).isEqualTo("/previous");
        assertThat(response.getHeaderString("X-Custom")).isEqualTo("custom-value");
    }

    @Test
    void shouldBackWithFallbackWhenNoReferer() {
        var result = processor.back(302, java.util.Map.of("X-Custom", "custom-value"), "/fallback");
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaderString("Location")).isEqualTo("/fallback");
        assertThat(response.getHeaderString("X-Custom")).isEqualTo("custom-value");
    }

    @Test
    void shouldPreserveCustomHeadersOnConflict() {
        asInertia();
        var result = processor.process("https://example.com", java.util.Map.of("X-Custom", "custom-value"));
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(409);
        assertThat(response.getHeaderString("X-Inertia-Location")).isEqualTo("https://example.com");
        assertThat(response.getHeaderString("X-Custom")).isEqualTo("custom-value");
    }

    @Test
    void shouldReturn303ForNonGetRedirect() {
        when(httpRequest.method()).thenReturn(HttpMethod.POST);
        var result = processor.process("/home");
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(303);
    }

    @Test
    void shouldReturn204WithPrecognitionHeadersForValidateOnlyRequests() throws Exception {
        var vertx = Vertx.vertx();
        var done = new java.util.concurrent.CompletableFuture<Response>();
        vertx.getOrCreateContext().runOnContext(v -> {
            var ctx = io.vertx.core.Vertx.currentContext();
            ctx.putLocal("inertia-precognition", Boolean.TRUE);
            ctx.putLocal("inertia-precognition-validate-fields", "username,email");
            var result = processor.process("/home");
            done.complete((Response) result.await().indefinitely());
        });
        var response = done.get(10, java.util.concurrent.TimeUnit.SECONDS);
        assertThat(response.getStatus()).isEqualTo(204);
        assertThat(response.getHeaderString("Precognition")).isEqualTo("true");
        assertThat(response.getHeaderString("Precognition-Success")).isEqualTo("true");
        assertThat(response.getHeaderString("Location")).isNull();
        verify(flashStore).drain();
        vertx.close();
    }

    @Test
    void shouldReturn302ForValidateOnlyWithoutPrecognitionHeader() throws Exception {
        var vertx = Vertx.vertx();
        var done = new java.util.concurrent.CompletableFuture<Response>();
        vertx.getOrCreateContext().runOnContext(v -> {
            var ctx = io.vertx.core.Vertx.currentContext();
            ctx.putLocal("inertia-precognition-validate-fields", "username,email");
            var result = processor.process("/home");
            done.complete((Response) result.await().indefinitely());
        });
        var response = done.get(10, java.util.concurrent.TimeUnit.SECONDS);
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaderString("Location")).isEqualTo("/home");
        verify(flashStore, never()).drain();
        vertx.close();
    }

    @Test
    void shouldReturnNormalRedirectForFragmentDuringPrefetch() throws Exception {
        when(httpRequest.getHeader("X-Inertia")).thenReturn("true");
        when(httpRequest.getHeader("Purpose")).thenReturn("prefetch");
        var vertx = Vertx.vertx();
        var done = new java.util.concurrent.CompletableFuture<Response>();
        vertx.getOrCreateContext().runOnContext(v -> {
            var ctx = io.vertx.core.Vertx.currentContext();
            ctx.putLocal("inertia-prefetch", Boolean.TRUE);
            var result = processor.process("/section#top");
            done.complete((Response) result.await().indefinitely());
        });
        var response = done.get(10, java.util.concurrent.TimeUnit.SECONDS);
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaderString("Location")).isEqualTo("/section#top");
        assertThat(response.getHeaderString("X-Inertia-Redirect")).isNull();
        vertx.close();
    }
}
