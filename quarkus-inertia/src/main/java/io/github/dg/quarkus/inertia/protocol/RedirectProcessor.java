package io.github.dg.quarkus.inertia.protocol;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;

import io.github.dg.quarkus.inertia.spi.FlashStore;
import io.github.dg.quarkus.inertia.vertx.ReactiveResponseWriter;

import java.util.Map;

/**
 * Builds the redirect responses for Inertia visits: plain 302 redirects for
 * GET requests, 303 for non-GET, and 409 conflict responses
 * ({@code X-Inertia-Location}) when an external/full-page redirect is
 * requested from an Inertia client.
 *
 * <p>Precognition validate-only requests never receive a redirect: when the
 * request carries the {@code Precognition-Validate-Only} header (and the
 * {@code Precognition} header), every redirect the controller produces is
 * replaced by a 204 response with {@code Precognition-Success: true}, and
 * any flash data written by the controller is drained so it does not leak
 * into the next visit (Laravel's {@code HandlePrecognitionRequests}
 * middleware parity).</p>
 */
@RequestScoped
public class RedirectProcessor {

    private final CurrentVertxRequest currentVertxRequest;
    private final FlashStore flashStore;
    private final ReactiveResponseWriter reactiveWriter;

    @Inject
    public RedirectProcessor(CurrentVertxRequest currentVertxRequest, FlashStore flashStore,
                             ReactiveResponseWriter reactiveWriter) {
        this.currentVertxRequest = currentVertxRequest;
        this.flashStore = flashStore;
        this.reactiveWriter = reactiveWriter;
    }

    public RedirectProcessor(CurrentVertxRequest currentVertxRequest, FlashStore flashStore) {
        this(currentVertxRequest, flashStore, new ReactiveResponseWriter());
    }

    /**
     * Redirect to a URL (plain 302 for GET, 303 otherwise).
     *
     * @param url the target URL
     * @return the redirect response as a Uni
     */
    public Uni<Object> process(String url) {
        return process(url, false);
    }

    /**
     * Redirect to a URL, optionally forcing a full page visit.
     *
     * @param url      the target URL
     * @param fullPage when {@code true} and the request is an Inertia
     *                 request, a 409 conflict response is returned
     * @return the redirect or conflict response as a Uni
     */
    public Uni<Object> process(String url, boolean fullPage) {
        return Uni.createFrom().deferred(() -> {
            if (fullPage && isInertiaRequest()) {
                return reactiveWriter.write(buildConflict(url, java.util.Map.of()));
            }
            return reactiveWriter.write(buildRedirect(url, isNonGetRequest(), java.util.Map.of()));
        });
    }

    /**
     * Redirect to a URL with extra headers; external URLs from an Inertia
     * request produce a 409 conflict response.
     *
     * @param url     the target URL
     * @param headers additional response headers
     * @return the redirect or conflict response as a Uni
     */
    public Uni<Object> process(String url, Map<String, String> headers) {
        return Uni.createFrom().deferred(() -> {
            if (isInertiaRequest() && isExternal(url)) {
                return reactiveWriter.write(buildConflict(url, headers));
            }
            return reactiveWriter.write(buildRedirect(url, isNonGetRequest(), headers));
        });
    }

    /**
     * Redirect outside of the Inertia application (full page navigation).
     *
     * @param url the external target URL
     * @return the redirect response as a Uni
     */
    public Uni<Object> external(String url) {
        return Uni.createFrom().deferred(() -> {
            if (isInertiaRequest() && isExternal(url)) {
                return reactiveWriter.write(buildConflict(url, java.util.Map.of()));
            }
            return reactiveWriter.write(buildRedirect(url, isNonGetRequest(), java.util.Map.of()));
        });
    }

    private jakarta.ws.rs.core.Response buildRedirect(String url, boolean nonGet, Map<String, String> headers) {
        if (isPrecognitionValidateOnly()) {
            flashStore.drain();
            return Response.status(Response.Status.NO_CONTENT)
                .header("Precognition", "true")
                .header("Precognition-Success", "true")
                .build();
        }
        var status = nonGet ? Response.Status.SEE_OTHER : Response.Status.FOUND;
        var builder = Response.status(status)
            .header("Location", url)
            .header("Vary", "X-Inertia");
        applyHeaders(builder, headers);
        return builder.build();
    }

    private boolean isPrecognitionValidateOnly() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var precognition = ctx.getLocal("inertia-precognition");
            var fields = ctx.getLocal("inertia-precognition-validate-fields");
            if (precognition != null || fields != null) {
                return Boolean.TRUE.equals(precognition) && fields != null;
            }
        }
        var request = resolveRequest();
        if (request == null) return false;
        return "true".equalsIgnoreCase(request.getHeader("Precognition"))
            && request.getHeader("Precognition-Validate-Only") != null;
    }

    private jakarta.ws.rs.core.Response buildConflict(String url, Map<String, String> headers) {
        var builder = Response.status(Response.Status.CONFLICT)
            .header("X-Inertia-Location", url)
            .header("Vary", "X-Inertia");
        applyHeaders(builder, headers);
        return builder.build();
    }

    private void applyHeaders(Response.ResponseBuilder builder, Map<String, String> headers) {
        if (headers == null) return;
        for (var entry : headers.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            builder.header(entry.getKey(), entry.getValue());
        }
    }

    private boolean isExternal(String url) {
        if (url == null || url.isBlank()) return false;
        if (url.startsWith("/")) return false;
        try {
            var redirectUri = java.net.URI.create(url);
            if (!redirectUri.isAbsolute()) return false;
            var request = resolveRequest();
            if (request == null) return true;
            var requestScheme = request.scheme();
            var requestAuthority = request.host();
            return !requestScheme.equals(redirectUri.getScheme())
                || !java.util.Objects.equals(requestAuthority, redirectUri.getAuthority());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Redirect back to the previous page (from the {@code Referer}
     * header), falling back to {@code /} when unknown.
     *
     * @return the redirect response as a Uni
     */
    public Uni<Object> back() {
        return back("/");
    }

    /**
     * Redirect back to the previous page with a custom fallback.
     *
     * @param fallback URL used when no referer is available
     * @return the redirect response as a Uni
     */
    public Uni<Object> back(String fallback) {
        return back0(fallback, -1, java.util.Map.of());
    }

    /**
     * Redirect back with a forced HTTP status.
     *
     * @param status   the response status to force
     * @param fallback URL used when no referer is available
     * @return the redirect response as a Uni
     */
    public Uni<Object> back(int status, String fallback) {
        return back0(fallback, status, java.util.Map.of());
    }

    /**
     * Redirect back with extra response headers.
     *
     * @param status   the response status to force
     * @param headers  additional response headers
     * @return the redirect response as a Uni
     */
    public Uni<Object> back(int status, Map<String, String> headers) {
        return back0("/", status, headers);
    }

    /**
     * Redirect back with a forced status, extra headers and a fallback.
     *
     * @param status   the response status to force
     * @param headers  additional response headers
     * @param fallback URL used when no referer is available
     * @return the redirect response as a Uni
     */
    public Uni<Object> back(int status, Map<String, String> headers, String fallback) {
        return back0(fallback, status, headers);
    }

    private Uni<Object> back0(String fallback, int forcedStatus, Map<String, String> headers) {
        var referer = getRefererUrl();
        var url = (referer != null && !referer.isBlank())
            ? referer
            : (fallback != null && !fallback.isBlank()) ? fallback : "/";
        if (forcedStatus > 0) {
            return Uni.createFrom().deferred(() -> {
                var builder = Response.status(forcedStatus)
                    .header("Location", url)
                    .header("Vary", "X-Inertia");
                applyHeaders(builder, headers);
                return reactiveWriter.write(builder.build());
            });
        }
        return process(url, headers);
    }

    private boolean isInertiaRequest() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal("inertia-request");
            if (val != null) return Boolean.TRUE.equals(val);
        }
        var request = resolveRequest();
        if (request != null) {
            var header = request.getHeader("X-Inertia");
            return "true".equalsIgnoreCase(header) || Boolean.parseBoolean(header);
        }
        return false;
    }

    private boolean isNonGetRequest() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var method = ctx.getLocal("request-method");
            if (method != null) {
                var m = (String) method;
                return "POST".equalsIgnoreCase(m) ||
                       "PUT".equalsIgnoreCase(m) ||
                       "PATCH".equalsIgnoreCase(m) ||
                       "DELETE".equalsIgnoreCase(m);
            }
        }
        var request = resolveRequest();
        if (request != null) {
            var m = request.method().toString();
            return "POST".equalsIgnoreCase(m) ||
                   "PUT".equalsIgnoreCase(m) ||
                   "PATCH".equalsIgnoreCase(m) ||
                   "DELETE".equalsIgnoreCase(m);
        }
        return false;
    }

    private String getRefererUrl() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var referer = ctx.getLocal("referer-url");
            if (referer != null) return (String) referer;
        }
        var request = resolveRequest();
        if (request != null) {
            return request.getHeader("Referer");
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
