package io.github.diovamny.quarkus.inertia.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.diovamny.inertia.core.model.PageObject;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

/**
 * Protocol-exact root assembly for placeholder templates carrying
 * {@code __INERTIA_ROOT__}: SSR success embeds the sidecar body verbatim
 * (single script, single {@code #app} with {@code data-server-rendered});
 * CSR output stays byte-identical to the legacy template shape.
 */
class SsrRootAssemblyTest {

    private HtmlRenderer renderer(boolean ssrEnabled, Uni<JsonObject> ssrResponse) {
        var config = mock(io.github.diovamny.quarkus.inertia.config.InertiaConfig.class);
        when(config.maxPageBytes()).thenReturn(33554432L);
        when(config.rootTemplate()).thenReturn("root-test.html");
        when(config.rootView()).thenReturn(Optional.empty());
        when(config.useQute()).thenReturn(false);
        var ssrHandler = mock(SsrHandler.class);
        when(ssrHandler.isSsrEnabled()).thenReturn(ssrEnabled);
        if (ssrResponse != null) {
            when(ssrHandler.render(any())).thenReturn(ssrResponse);
        }
        var nonceProviders = mock(jakarta.enterprise.inject.Instance.class);
        when(nonceProviders.isUnsatisfied()).thenReturn(true);
        return new HtmlRenderer(null, null, mock(jakarta.enterprise.inject.Instance.class),
            config, ssrHandler, nonceProviders);
    }

    private PageObject page() throws Exception {
        var props = new LinkedHashMap<String, Object>();
        props.put("message", "Hello");
        var json = new ObjectMapper().writeValueAsString(Map.of(
            "component", "Home", "props", props, "url", "/", "version", "v1"));
        assertTrue(json.contains("Home"));
        return new PageObject("Home", props, "/", "v1", null, null, null,
            null, null, null, null, null, null, null, null, null, null, null);
    }

    private static int occurrences(String html, String needle) {
        return html.split(java.util.regex.Pattern.quote(needle), -1).length - 1;
    }

    @Test
    void csrShellMatchesLegacyBytes() throws Exception {
        var page = page();
        var json = new ObjectMapper().writeValueAsString(Map.of(
            "component", "Home", "props", Map.of("message", "Hello"),
            "url", "/", "version", "v1"));
        var html = renderer(false, null).renderSync(page, json);

        var expectedShell = "<div id=\"app\"></div>\n    <script type=\"application/json\" data-page=\"app\">"
            + io.github.diovamny.inertia.core.security.SafeJsonEncoder.encodeForScript(json)
            + "</script>";
        assertTrue(html.contains(expectedShell), "CSR shell must match the legacy template bytes");
        assertEquals(1, occurrences(html, "data-page=\"app\""));
        assertEquals(1, occurrences(html, "id=\"app\""));
        assertFalse(html.contains("__INERTIA_"));
    }

    @Test
    void ssrSuccessIsProtocolExact() throws Exception {
        var body = "<script data-page=\"app\" type=\"application/json\">{\"component\":\"Welcome\"}</script>"
            + "<div data-server-rendered=\"true\" id=\"app\">Hi</div>";
        // Real handler + stub sidecar (no SsrHandler mocks: renderSync awaits
        // the Uni, which only completes on a real event loop).
        var vertx = io.vertx.core.Vertx.vertx();
        try {
            var server = vertx.createHttpServer();
            server.requestHandler(req -> {
                if (!"/render".equals(req.path())) {
                    req.response().setStatusCode(404).end("not found");
                    return;
                }
                req.bodyHandler(payload -> req.response()
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("body", body).encode()));
            });
            var port = new java.util.concurrent.atomic.AtomicInteger();
            Uni.createFrom().emitter(emitter -> server.listen(0, ar -> {
                if (ar.failed()) {
                    emitter.fail(ar.cause());
                } else {
                    port.set(ar.result().actualPort());
                    emitter.complete(null);
                }
            })).await().indefinitely();
            var config = mock(io.github.diovamny.quarkus.inertia.config.InertiaConfig.class);
            when(config.maxPageBytes()).thenReturn(33554432L);
            when(config.rootTemplate()).thenReturn("root-test.html");
            when(config.rootView()).thenReturn(Optional.empty());
            when(config.useQute()).thenReturn(false);
            when(config.ssrEnabled()).thenReturn(true);
            when(config.ssrUrl()).thenReturn("http://localhost:" + port.get() + "/render");
            when(config.ssrConnectTimeout()).thenReturn(java.time.Duration.ofSeconds(5));
            when(config.ssrReadTimeout()).thenReturn(java.time.Duration.ofSeconds(10));
            when(config.ssrBreakerFailureThreshold()).thenReturn(3);
            when(config.ssrBreakerCooldown()).thenReturn(java.time.Duration.ofSeconds(30));
            var ssrHandler = new SsrHandler(config, null, vertx);
            var nonceProviders = mock(jakarta.enterprise.inject.Instance.class);
            when(nonceProviders.isUnsatisfied()).thenReturn(true);
            var renderer = new HtmlRenderer(null, null, mock(jakarta.enterprise.inject.Instance.class),
                config, ssrHandler, nonceProviders);
            var html = renderer.renderSync(page(), "{}");

            assertEquals(1, occurrences(html, "data-page=\"app\""));
            assertEquals(1, occurrences(html, "id=\"app\""));
            assertTrue(html.contains("<div data-server-rendered=\"true\" id=\"app\">Hi</div>"));
            assertFalse(html.contains("__INERTIA_"));
            server.close().toCompletionStage().toCompletableFuture().join();
        } finally {
            vertx.close().toCompletionStage().toCompletableFuture().join();
        }
    }
}
