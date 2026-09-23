package io.github.diovamny.spring.inertia.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import io.github.diovamny.spring.inertia.config.InertiaProperties;
import io.github.diovamny.spring.inertia.internal.JacksonJsonProvider;
import io.github.diovamny.inertia.core.model.PageObject;

class SsrClientSidecarTest {

    private static PageObject page() {
        return new PageObject("Home", Map.of("message", "Hello"), "/", "v1",
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private static SsrClient clientFor(String ssrUrl) {
        var properties = new InertiaProperties();
        properties.setSsrUrl(ssrUrl);
        properties.setSsrConnectTimeout(Duration.ofSeconds(2));
        properties.setSsrReadTimeout(Duration.ofSeconds(2));
        return new SsrClient(properties, new JacksonJsonProvider(new ObjectMapper()));
    }

    @Test
    void validSidecarRenders() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/render", exchange -> {
            var body = "{\"head\":[\"<title>t</title>\"],\"body\":\"<div>ssr</div>\"}"
                .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            var result = clientFor("http://127.0.0.1:" + server.getAddress().getPort() + "/render")
                .render(page());
            assertTrue(result.isPresent());
            assertEquals("<div>ssr</div>", result.get().body());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void redirectIsNeverFollowedAndFallsBackToCsr() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/render", exchange -> {
            exchange.getResponseHeaders().add("Location", "http://127.0.0.1:9/evil");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.start();
        try {
            var result = clientFor("http://127.0.0.1:" + server.getAddress().getPort() + "/render")
                .render(page());
            assertTrue(result.isEmpty(), "a 302 sidecar must fall back to CSR, never follow");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void invalidBodyFallsBackToCsr() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/render", exchange -> {
            var body = "not-json".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            var result = clientFor("http://127.0.0.1:" + server.getAddress().getPort() + "/render")
                .render(page());
            assertTrue(result.isEmpty());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void downSidecarFallsBackToCsr() {
        var result = clientFor("http://127.0.0.1:9/render").render(page());
        assertTrue(result.isEmpty());
    }
}
