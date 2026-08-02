package com.quarkus.inertia.protocol;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;

@RequestScoped
public class RedirectProcessor {

    private final CurrentVertxRequest currentVertxRequest;

    @Inject
    public RedirectProcessor(CurrentVertxRequest currentVertxRequest) {
        this.currentVertxRequest = currentVertxRequest;
    }

    public Uni<Object> process(String url) {
        var status = isNonGetRequest() ? Response.Status.SEE_OTHER : Response.Status.FOUND;
        return Uni.createFrom().item(
            Response.status(status)
                .header("Location", url)
                .header("Vary", "X-Inertia")
                .build()
        );
    }

    public Uni<Object> external(String url) {
        if (isInertiaRequest() && isExternal(url)) {
            return Uni.createFrom().item(
                Response.status(Response.Status.CONFLICT)
                    .header("X-Inertia-Location", url)
                    .header("Vary", "X-Inertia")
                    .build()
            );
        }
        var status = isNonGetRequest() ? Response.Status.SEE_OTHER : Response.Status.FOUND;
        return Uni.createFrom().item(
            Response.status(status)
                .header("Location", url)
                .header("Vary", "X-Inertia")
                .build()
        );
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

    public Uni<Object> back() {
        return back("/");
    }

    public Uni<Object> back(String fallback) {
        return back0(fallback, -1);
    }

    public Uni<Object> back(int status, String fallback) {
        return back0(fallback, status);
    }

    private Uni<Object> back0(String fallback, int forcedStatus) {
        var referer = getRefererUrl();
        var url = (referer != null && !referer.isBlank())
            ? referer
            : (fallback != null && !fallback.isBlank()) ? fallback : "/";
        if (forcedStatus > 0) {
            return Uni.createFrom().item(
                Response.status(forcedStatus)
                    .header("Location", url)
                    .header("Vary", "X-Inertia")
                    .build()
            );
        }
        return process(url);
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
