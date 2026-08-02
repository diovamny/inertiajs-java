package com.quarkus.inertia.protocol;

import java.util.Map;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;

import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.renderer.HtmlRenderer;
import com.quarkus.inertia.response.JsonResponseProcessor;
import com.quarkus.inertia.version.VersionProvider;

@RequestScoped
public class ResponseProcessor {

    private final HtmlRenderer htmlRenderer;
    private final JsonResponseProcessor jsonProcessor;
    private final CurrentVertxRequest currentVertxRequest;
    private final VersionProvider versionProvider;

    @Inject
    public ResponseProcessor(HtmlRenderer htmlRenderer, JsonResponseProcessor jsonProcessor,
                             CurrentVertxRequest currentVertxRequest,
                             VersionProvider versionProvider) {
        this.htmlRenderer = htmlRenderer;
        this.jsonProcessor = jsonProcessor;
        this.currentVertxRequest = currentVertxRequest;
        this.versionProvider = versionProvider;
    }

    public Uni<Object> process(PageObject page) {
        if (isPrecognition()) {
            return jsonProcessor.write(page)
                .map(json -> {
                    var rawErrors = page.props().get("errors");
                    boolean hasErrors = rawErrors instanceof Map && !((Map<?, ?>) rawErrors).isEmpty();
                    if (hasErrors) {
                        return Response.status(422)
                            .entity(json)
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .header("X-Inertia", "true")
                            .header("Vary", "X-Inertia")
                            .build();
                    }
                    return Response.noContent()
                        .header("X-Inertia", "true")
                        .header("Vary", "X-Inertia")
                        .build();
                })
                .map(Object.class::cast);
        }

        if (!isInertiaRequest()) {
            return htmlRenderer.render(page)
                .map(html -> Response.ok(html, MediaType.TEXT_HTML_TYPE)
                    .header("Vary", "X-Inertia")
                    .build())
                .map(Object.class::cast);
        }

        if (isGetRequest()) {
            var clientVersion = getClientVersion();
            if (clientVersion != null && !clientVersion.equals(page.version())) {
                return Uni.createFrom().item(
                    Response.status(Response.Status.CONFLICT)
                        .header("X-Inertia-Location", page.url())
                        .header("X-Inertia-Version", versionProvider.getVersion())
                        .header("Vary", "X-Inertia")
                        .build()
                );
            }
        }

        return jsonProcessor.write(page).map(Object.class::cast);
    }

    private boolean isInertiaRequest() {
        var vertxContext = Vertx.currentContext();
        if (vertxContext != null) {
            var request = vertxContext.getLocal("inertia-request");
            if (request != null) return Boolean.TRUE.equals(request);
        }
        var request = resolveRequest();
        if (request != null) {
            var header = request.getHeader("X-Inertia");
            return "true".equalsIgnoreCase(header) || Boolean.parseBoolean(header);
        }
        return false;
    }

    private boolean isGetRequest() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var method = ctx.getLocal("request-method");
            if (method != null) return "GET".equalsIgnoreCase((String) method);
        }
        var request = resolveRequest();
        if (request != null) {
            return "GET".equalsIgnoreCase(request.method().toString());
        }
        return true;
    }

    private String getClientVersion() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var version = ctx.getLocal("inertia-version");
            if (version != null) return (String) version;
        }
        var request = resolveRequest();
        if (request != null) {
            return request.getHeader("X-Inertia-Version");
        }
        return null;
    }

    private boolean isPrecognition() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal("inertia-precognition");
            return Boolean.TRUE.equals(val);
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
