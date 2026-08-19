package io.github.dg.quarkus.inertia.vertx;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import io.vertx.core.Context;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;

import io.github.dg.quarkus.inertia.config.InertiaConfig;
import io.github.dg.quarkus.inertia.security.InertiaCsrfService;

/**
 * Reactive counterpart of the JAX-RS {@code InertiaResponseFilter}: decorates
 * the response built by the processors before it is written to the Vert.x
 * response — custom headers, the {@code Vary: X-Inertia} header, redirect
 * rewriting (409 conflicts for {@code #} and external URLs, 302→303 for
 * PUT/PATCH/DELETE), the lazy ETag/304 optimization and the CSRF cookie.
 */
@ApplicationScoped
public class InertiaResponseDecorator {

    @Inject
    InertiaConfig config;

    @Inject
    InertiaCsrfService csrfService;

    /**
     * Decorate the given response for the reactive routes transport.
     *
     * @param rc       the routing context
     * @param response the response built by the processors
     * @return the decorated status/headers/entity triple
     */
    public DecoratedResponse decorate(RoutingContext rc, Response response) {
        var headers = MultiMap.caseInsensitiveMultiMap();
        response.getHeaders().forEach((k, vs) -> vs.forEach(v -> headers.add(k, String.valueOf(v))));
        int status = response.getStatus();
        Object entity = safeEntity(response);

        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            applyCustomHeaders(headers, ctx);
            applyCsrfCookie(headers, ctx);
        }

        var isPrecognition = ctx != null && Boolean.TRUE.equals(ctx.getLocal("inertia-precognition"));
        if (isPrecognition) {
            if (status >= 400) {
                return new DecoratedResponse(status, headers, entity);
            }
            headers.set("Precognition", "true");
            headers.set("Precognition-Success", "true");
            headers.set("Vary", "Precognition");
            return new DecoratedResponse(Response.Status.NO_CONTENT.getStatusCode(), headers, null);
        }

        var isInertia = ctx != null && Boolean.TRUE.equals(ctx.getLocal("inertia-request"));
        if (!isInertia) {
            return new DecoratedResponse(status, headers, entity);
        }

        headers.add("Vary", "X-Inertia");

        if (isRedirect(status)) {
            return handleRedirect(rc, ctx, status, headers, entity);
        }

        if (status == 200) {
            if (isEmpty(entity)) {
                return handleEmptyResponse(ctx, headers);
            }
            return handleConditionalRequest(rc, ctx, headers, entity);
        }

        return new DecoratedResponse(status, headers, entity);
    }

    private void applyCustomHeaders(MultiMap headers, Context ctx) {
        @SuppressWarnings("unchecked")
        var custom = (Map<String, Object>) ctx.getLocal("inertia-response-headers");
        if (custom == null || custom.isEmpty()) return;
        for (var entry : custom.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            headers.set(entry.getKey(), String.valueOf(entry.getValue()));
        }
    }

    private void applyCsrfCookie(MultiMap headers, Context ctx) {
        if (!Boolean.TRUE.equals(ctx.getLocal(InertiaCsrfService.CONTEXT_HANDLED))) return;
        var token = (String) ctx.getLocal(InertiaCsrfService.CONTEXT_TOKEN);
        if (token != null) {
            headers.add("Set-Cookie", csrfService.cookieHeader(token));
        }
    }

    private DecoratedResponse handleRedirect(RoutingContext rc, Context ctx, int status,
            MultiMap headers, Object entity) {
        if (headers.get("X-Inertia-Location") != null) {
            return new DecoratedResponse(status, headers, entity);
        }

        String location = headers.get("Location");
        if (location == null) {
            return new DecoratedResponse(status, headers, entity);
        }

        var isPrefetch = Boolean.TRUE.equals(ctx.getLocal("inertia-prefetch"));

        if (location.contains("#") && !isPrefetch) {
            headers.set("X-Inertia-Redirect", location);
            return new DecoratedResponse(Response.Status.CONFLICT.getStatusCode(), headers, null);
        }

        var method = (String) ctx.getLocal("request-method");

        if (isExternalRedirect(rc, location) && !isPrefetch
                && "GET".equalsIgnoreCase(method) && status == 302) {
            headers.set("X-Inertia-Location", location);
            return new DecoratedResponse(Response.Status.CONFLICT.getStatusCode(), headers, null);
        }

        if (method != null && isPutPatchDelete(method) && status == 302) {
            return new DecoratedResponse(Response.Status.SEE_OTHER.getStatusCode(), headers, entity);
        }

        return new DecoratedResponse(status, headers, entity);
    }

    private DecoratedResponse handleEmptyResponse(Context ctx, MultiMap headers) {
        String referer = (String) ctx.getLocal("referer-url");
        if (referer != null && !referer.isBlank()) {
            headers.set("Location", referer);
            return new DecoratedResponse(Response.Status.FOUND.getStatusCode(), headers, null);
        }
        return new DecoratedResponse(Response.Status.OK.getStatusCode(), headers, null);
    }

    private DecoratedResponse handleConditionalRequest(RoutingContext rc, Context ctx,
            MultiMap headers, Object entity) {
        if (!config.lazyEtagEnabled()) {
            return new DecoratedResponse(Response.Status.OK.getStatusCode(), headers, entity);
        }

        String etag;
        try {
            etag = computeEtag(entity, ctx);
        } catch (Exception e) {
            return new DecoratedResponse(Response.Status.OK.getStatusCode(), headers, entity);
        }
        if (etag == null) {
            return new DecoratedResponse(Response.Status.OK.getStatusCode(), headers, entity);
        }

        headers.set("ETag", etag);

        if ("GET".equalsIgnoreCase(rc.request().method().name())) {
            var ifNoneMatch = rc.request().getHeader("If-None-Match");
            if (ifNoneMatch != null && ifNoneMatch.replace("W/", "").trim().equals(etag)) {
                return new DecoratedResponse(Response.Status.NOT_MODIFIED.getStatusCode(), headers, null);
            }
        }

        return new DecoratedResponse(Response.Status.OK.getStatusCode(), headers, entity);
    }

    private String computeEtag(Object entity, Context ctx) {
        if (entity == null || entity instanceof String s && s.isBlank()) return null;

        var clientVersion = ctx.getLocal("inertia-version");
        var partialComponent = ctx.getLocal("inertia-partial-component");
        var partialData = ctx.getLocal("inertia-partial-data");
        var partialExcept = ctx.getLocal("inertia-partial-except");

        var representation = new StringBuilder();
        representation.append("inertia=true;");
        representation.append("version=").append(String.valueOf(clientVersion)).append(';');
        if (partialComponent != null) {
            representation.append("partial-component=").append(String.valueOf(partialComponent)).append(';');
            representation.append("partial-data=").append(String.valueOf(partialData)).append(';');
            representation.append("partial-except=").append(String.valueOf(partialExcept)).append(';');
        }
        representation.append("body=").append(entityHash(entity));

        try {
            var digest = MessageDigest.getInstance("SHA-256")
                .digest(representation.toString().getBytes(StandardCharsets.UTF_8));
            return hex(digest);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(representation.toString().hashCode());
        }
    }

    private String entityHash(Object entity) {
        var string = String.valueOf(entity);
        try {
            var digest = MessageDigest.getInstance("SHA-256")
                .digest(string.getBytes(StandardCharsets.UTF_8));
            return hex(digest);
        } catch (NoSuchAlgorithmException e) {
            return string;
        }
    }

    private String hex(byte[] bytes) {
        var sb = new StringBuilder(bytes.length * 2);
        for (var b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private boolean isExternalRedirect(RoutingContext rc, String location) {
        if (location.startsWith("/")) return false;
        try {
            var redirectUri = URI.create(location);
            if (!redirectUri.isAbsolute()) return false;
            var request = rc.request();
            return !request.scheme().equals(redirectUri.getScheme())
                || !Objects.equals(request.host(), redirectUri.getAuthority());
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isRedirect(int status) {
        return status == 301 || status == 302 || status == 303 || status == 307 || status == 308;
    }

    static boolean isPutPatchDelete(String method) {
        return "PUT".equalsIgnoreCase(method) ||
               "PATCH".equalsIgnoreCase(method) ||
               "DELETE".equalsIgnoreCase(method);
    }

    private Object safeEntity(Response response) {
        try {
            return response.getEntity();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isEmpty(Object entity) {
        if (entity == null) return true;
        if (entity instanceof String s && s.isBlank()) return true;
        return false;
    }
}