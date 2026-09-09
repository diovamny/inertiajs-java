package io.github.dg.quarkus.inertia.renderer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.dg.quarkus.inertia.config.InertiaConfig;

class SsrHandlerRenderUnitTest {

    private Vertx vertx;
    private HttpServer server;
    private int port;
    private SsrHandler handler;
    private CurrentVertxRequest currentVertxRequest;

    @BeforeEach
    void setUp() {
        vertx = Vertx.vertx();
        server = vertx.createHttpServer();
        server.requestHandler(req -> {
            HttpServerResponse resp = req.response();
            if (!"/render".equals(req.path())) {
                resp.setStatusCode(404).end("not found");
                return;
            }
            var page = req.bodyHandler(body -> {
                var json = new JsonObject(body.toString());
                var component = json.getString("component");
                resp.putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                        .put("body", "<div id=\"app\">Rendered " + component + "</div>")
                        .put("head", List.of("<title>" + component + "</title>"))
                        .encode());
            });
        });
        var actualPort = new java.util.concurrent.atomic.AtomicInteger();
        Uni.createFrom().emitter(emitter ->
            server.listen(0, ar -> {
                if (ar.failed()) emitter.fail(ar.cause());
                else {
                    actualPort.set(ar.result().actualPort());
                    emitter.complete(null);
                }
            })).await().indefinitely();
        port = actualPort.get();
        currentVertxRequest = mock(CurrentVertxRequest.class);
        var httpRequest = mock(io.vertx.core.http.HttpServerRequest.class);
        when(httpRequest.uri()).thenReturn("/ssr-test");
        var routingContext = mock(RoutingContext.class);
        when(routingContext.request()).thenReturn(httpRequest);
        when(currentVertxRequest.getCurrent()).thenReturn(routingContext);

        var config = mock(InertiaConfig.class);
        when(config.ssrEnabled()).thenReturn(true);
        when(config.ssrUrl()).thenReturn("http://localhost:" + port);
        when(config.ssrExcludePaths()).thenReturn(Optional.empty());

        handler = new SsrHandler(config, currentVertxRequest, vertx);
    }

    @AfterEach
    void tearDown() {
        if (server != null) server.close();
        if (vertx != null) vertx.close();
    }

    @Test
    void rendersPageThroughSsrServer() {
        var page = new JsonObject()
            .put("component", "Home")
            .put("url", "/ssr-test")
            .put("version", "1.0.0")
            .put("props", new JsonObject());

        var result = handler.render(page).await().indefinitely();

        assertThat(result.getString("body")).contains("Rendered Home");
        assertThat(result.getJsonArray("head")).contains("<title>Home</title>");
    }

    @Test
    void appendsRenderPathToConfiguredUrl() {
        assertThat(handler.resolveSsrUrl()).isEqualTo("http://localhost:" + port + "/render");
    }

    @Test
    void doesNotDuplicateRenderPath() {
        var config = mock(InertiaConfig.class);
        when(config.ssrUrl()).thenReturn("http://localhost:13714/render");
        var bare = new SsrHandler(config, currentVertxRequest, vertx);
        assertThat(bare.resolveSsrUrl()).isEqualTo("http://localhost:13714/render");
    }

    @Test
    void failsWhenServerUnreachable() {
        var config = mock(InertiaConfig.class);
        when(config.ssrUrl()).thenReturn("http://localhost:1");
        var isolated = new SsrHandler(config, currentVertxRequest, vertx);

        var failed = isolated.render(new JsonObject().put("component", "X")).onFailure()
            .recoverWithItem((JsonObject) null)
            .await().indefinitely();

        assertThat(failed).isNull();
    }
}
