package com.quarkus.inertia.renderer;

import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import com.quarkus.inertia.config.InertiaConfig;

/**
 * Client for the server-side rendering server: forwards the page object as
 * JSON to the configured SSR endpoint ({@code inertia.ssr-url}) and returns
 * the rendered HTML, honoring {@code inertia.ssr-exclude-paths}.
 */
@ApplicationScoped
public class SsrHandler {

    private final InertiaConfig config;
    private final CurrentVertxRequest currentVertxRequest;
    private final Vertx vertx;

    @Inject
    public SsrHandler(InertiaConfig config, CurrentVertxRequest currentVertxRequest, Vertx vertx) {
        this.config = config;
        this.currentVertxRequest = currentVertxRequest;
        this.vertx = vertx;
    }

    SsrHandler(InertiaConfig config, CurrentVertxRequest currentVertxRequest) {
        this(config, currentVertxRequest, null);
    }

    /**
     * Whether SSR should apply to the current request: enabled in the
     * config, not temporarily disabled, and not excluded by path.
     *
     * @return {@code true} when SSR applies
     */
    public boolean isSsrEnabled() {
        if (!config.ssrEnabled()) return false;

        var ctx = Vertx.currentContext();
        if (ctx != null && Boolean.TRUE.equals(ctx.getLocal("inertia-disable-ssr"))) return false;

        var uri = resolveUri(ctx);
        if (uri == null) return true;

        if (matches(uri, config.ssrExcludePaths().orElse(List.of()))) return false;
        if (ctx != null) {
            var localPaths = (List<String>) ctx.getLocal("inertia-ssr-exclude-paths");
            if (localPaths != null && matches(uri, localPaths)) return false;
        }
        return true;
    }

    /**
     * Sends the page to the SSR server and returns its JSON response
     * ({@code head} + {@code body}). Fails with an exception when the SSR
     * server is unreachable; callers fall back to client-side rendering.
     */
    public Uni<JsonObject> render(JsonObject page) {
        if (vertx == null) {
            return Uni.createFrom().failure(new IllegalStateException("SSR is not supported yet"));
        }
        var url = resolveSsrUrl();
        if (url == null || url.isBlank()) {
            return Uni.createFrom().failure(new IllegalStateException("ssr-url is not configured"));
        }
        return Uni.createFrom().deferred(() -> {
            var client = vertx.createHttpClient();
            return Uni.createFrom().emitter(emitter -> {
                emitter.onTermination(client::close);
                var options = new io.vertx.core.http.RequestOptions()
                    .setAbsoluteURI(url)
                    .setMethod(HttpMethod.POST);
                client.request(options, ar -> {
                    if (ar.failed()) {
                        emitter.fail(ar.cause());
                        return;
                    }
                    var req = ar.result();
                    req.putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                    req.putHeader("X-Inertia", "true");
                    req.send(page.encode(), respAr -> {
                        if (respAr.failed()) {
                            emitter.fail(respAr.cause());
                            return;
                        }
                        var resp = respAr.result();
                        resp.body(bodyAr -> {
                            if (bodyAr.failed()) {
                                emitter.fail(bodyAr.cause());
                                return;
                            }
                            var body = bodyAr.result().toString();
                            if (resp.statusCode() >= 400) {
                                emitter.fail(new IllegalStateException(
                                    "SSR server responded with " + resp.statusCode() + ": " + body));
                                return;
                            }
                            emitter.complete(new JsonObject(body));
                        });
                    });
                });
            });
        });
    }

    /**
     * The SSR URL for the current request, resolved from the configured URL
     * and the {@code X-Inertia-SSR-Base-URL} request header.
     *
     * @return the SSR URL, or {@code null} when unknown
     */
    public String resolveSsrUrl() {
        var base = config.ssrUrl();
        if (base == null || base.isBlank()) return base;
        var trimmed = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        if (trimmed.endsWith("/render") || trimmed.endsWith("/__inertia_ssr")) {
            return trimmed;
        }
        return trimmed + "/render";
    }

    private String resolveUri(Context ctx) {
        if (ctx != null) {
            var uri = (String) ctx.getLocal("request-uri");
            if (uri != null) return uri;
        }
        var request = resolveRequest();
        if (request != null) return request.uri();
        return null;
    }

    private boolean matches(String uri, List<String> patterns) {
        for (var pattern : patterns) {
            if (pattern == null || pattern.isBlank()) continue;
            if (pattern.endsWith("*")) {
                if (uri.startsWith(pattern.substring(0, pattern.length() - 1))) return true;
            } else if (pattern.equals(uri) || uri.startsWith(pattern + "/")) {
                return true;
            }
        }
        return false;
    }

    private HttpServerRequest resolveRequest() {
        try {
            var routingContext = currentVertxRequest.getCurrent();
            if (routingContext != null) {
                return routingContext.request();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
