package com.quarkus.inertia.renderer;

import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import com.quarkus.inertia.config.InertiaConfig;

@ApplicationScoped
public class SsrHandler {

    private final InertiaConfig config;
    private final CurrentVertxRequest currentVertxRequest;

    @Inject
    public SsrHandler(InertiaConfig config, CurrentVertxRequest currentVertxRequest) {
        this.config = config;
        this.currentVertxRequest = currentVertxRequest;
    }

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

    public String render(JsonObject page) {
        throw new UnsupportedOperationException("SSR is not supported yet");
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
