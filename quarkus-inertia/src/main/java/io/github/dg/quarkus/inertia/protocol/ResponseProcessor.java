package io.github.dg.quarkus.inertia.protocol;

import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;

import io.github.dg.quarkus.inertia.model.PageObject;
import io.github.dg.quarkus.inertia.renderer.HtmlRenderer;
import io.github.dg.quarkus.inertia.response.JsonResponseProcessor;
import io.github.dg.quarkus.inertia.version.VersionProvider;
import io.github.dg.quarkus.inertia.vertx.ReactiveResponseWriter;

/**
 * Turns a finished {@link PageObject} into the actual HTTP response:
 * JSON (200) for Inertia requests, JSON (304) for precognition requests,
 * and HTML (200) for regular page loads. Adds the asset version and
 * {@code X-Inertia} headers.
 *
 * <p>Mutating requests (POST/PUT/PATCH/DELETE) redirect with 303 See Other
 * per Inertia v3 protocol.</p>
 */
@ApplicationScoped
public class ResponseProcessor {

    private final HtmlRenderer htmlRenderer;
    private final JsonResponseProcessor jsonProcessor;
    private final CurrentVertxRequest currentVertxRequest;
    private final VersionProvider versionProvider;
    private final ReactiveResponseWriter reactiveWriter;

    @Inject
    public ResponseProcessor(HtmlRenderer htmlRenderer, JsonResponseProcessor jsonProcessor,
                             CurrentVertxRequest currentVertxRequest,
                             VersionProvider versionProvider,
                             ReactiveResponseWriter reactiveWriter) {
        this.htmlRenderer = htmlRenderer;
        this.jsonProcessor = jsonProcessor;
        this.currentVertxRequest = currentVertxRequest;
        this.versionProvider = versionProvider;
        this.reactiveWriter = reactiveWriter;
    }

    /**
     * Serialize and return the HTTP response for the given page object (reactive).
     *
     * @param page the finished page object
     * @return the HTTP response as a Uni
     */
    public Uni<Object> process(PageObject page) {
        if (isPrecognition()) {
            var rawErrors = page.props().get("errors");
            boolean hasErrors = rawErrors instanceof Map && !((Map<?, ?>) rawErrors).isEmpty();
            if (hasErrors) {
                return reactiveWriter.write(Response.status(422)
                    .entity(Map.of("errors", rawErrors))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .header("X-Inertia", "true")
                    .header("Precognition", "true")
                    .header("Vary", "X-Inertia, Precognition")
                    .build());
            }
            return reactiveWriter.write(Response.noContent()
                .header("X-Inertia", "true")
                .header("Precognition", "true")
                .header("Precognition-Success", "true")
                .header("Vary", "X-Inertia, Precognition")
                .build());
        }

        if (!isInertiaRequest()) {
            return htmlRenderer.render(page)
                .map(html -> {
                    Integer status = pageStatus();
                    var builder = (status != null && status != 200)
                        ? Response.status(status)
                        : Response.ok();
                    return builder.entity(html)
                        .type(MediaType.TEXT_HTML_TYPE)
                        .header("Vary", "X-Inertia")
                        .build();
                })
                .chain(reactiveWriter::write);
        }

        if (isGetRequest()) {
            var clientVersion = getClientVersion();
            if (clientVersion != null && !clientVersion.equals(page.version()) && !isPrefetch()) {
                return reactiveWriter.write(
                    Response.status(Response.Status.CONFLICT)
                        .header("X-Inertia-Location", page.url())
                        .header("X-Inertia-Version", versionProvider.getVersion())
                        .header("Vary", "X-Inertia, X-Inertia-Version")
                        .build()
                );
            }
        }

        // Default to 200 when the controller did not set an explicit status.
        // The 303 See Other conversion for mutating requests is handled by the
        // redirect processing in InertiaResponseFilter / InertiaVertxHandler,
        // not here: a rendered Inertia page keeps its explicit status or 200.
        var status = pageStatus();
        int effectiveStatus = (status != null) ? status : 200;

        return jsonProcessor.write(page, effectiveStatus).chain(reactiveWriter::write);
    }

    /**
     * Serialize and return the HTTP response for the given page object (synchronous).
     *
     * @param page the finished page object
     * @return the HTTP response
     */
    public Response processSync(PageObject page) {
        if (isPrecognition()) {
            var rawErrors = page.props().get("errors");
            boolean hasErrors = rawErrors instanceof Map && !((Map<?, ?>) rawErrors).isEmpty();
            if (hasErrors) {
                return Response.status(422)
                    .entity(Map.of("errors", rawErrors))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .header("X-Inertia", "true")
                    .header("Precognition", "true")
                    .header("Vary", "X-Inertia, Precognition")
                    .build();
            }
            return Response.noContent()
                .header("X-Inertia", "true")
                .header("Precognition", "true")
                .header("Precognition-Success", "true")
                .header("Vary", "X-Inertia, Precognition")
                .build();
        }

        if (!isInertiaRequest()) {
            String html = htmlRenderer.renderSync(page, jsonProcessor.serializeSync(page));
            Integer status = pageStatus();
            var builder = (status != null && status != 200)
                ? Response.status(status)
                : Response.ok();
            return builder.entity(html)
                .type(MediaType.TEXT_HTML_TYPE)
                .header("Vary", "X-Inertia")
                .build();
        }

        if (isGetRequest()) {
            var clientVersion = getClientVersion();
            if (clientVersion != null && !clientVersion.equals(page.version()) && !isPrefetch()) {
                return Response.status(Response.Status.CONFLICT)
                    .header("X-Inertia-Location", page.url())
                    .header("X-Inertia-Version", versionProvider.getVersion())
                    .header("Vary", "X-Inertia, X-Inertia-Version")
                    .build();
            }
        }

        var status = pageStatus();
        int effectiveStatus = (status != null) ? status : 200;

        return jsonProcessor.writeSync(page, effectiveStatus);
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

    private boolean isMutatingRequest() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var method = ctx.getLocal("request-method");
            if (method != null) {
                var m = ((String) method).toUpperCase();
                return "POST".equals(m) || "PUT".equals(m) || "PATCH".equals(m) || "DELETE".equals(m);
            }
        }
        var request = resolveRequest();
        if (request != null) {
            var m = request.method().toString().toUpperCase();
            return "POST".equals(m) || "PUT".equals(m) || "PATCH".equals(m) || "DELETE".equals(m);
        }
        return false;
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

    private boolean isPrefetch() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            return Boolean.TRUE.equals(ctx.getLocal("inertia-prefetch"));
        }
        return false;
    }

    private Integer pageStatus() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var status = ctx.getLocal("inertia-page-status");
            if (status instanceof Integer i) return i;
        }
        return null;
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
