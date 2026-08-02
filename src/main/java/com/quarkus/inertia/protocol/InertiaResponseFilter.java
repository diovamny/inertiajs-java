package com.quarkus.inertia.protocol;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import io.vertx.core.Vertx;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.quarkus.inertia.config.InertiaConfig;

@ApplicationScoped
@Provider
@Priority(Priorities.HEADER_DECORATOR + 20)
public class InertiaResponseFilter implements ContainerResponseFilter {

    @Inject
    InertiaConfig config;

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        var ctx = Vertx.currentContext();
        if (ctx == null) return;

        var isInertia = Boolean.TRUE.equals(ctx.getLocal("inertia-request"));
        int status = response.getStatus();

        if (isInertia) {
            response.getHeaders().add("Vary", "X-Inertia");
        } else {
            return;
        }

        var isPrecognition = Boolean.TRUE.equals(ctx.getLocal("inertia-precognition"));
        if (isPrecognition) return;

        if (isRedirect(status)) {
            handleRedirect(request, response, ctx);
            return;
        }

        if (status == 200) {
            Object entity = safeEntity(response);
            if (isEmpty(entity)) {
                handleEmptyResponse(response, ctx);
            } else {
                handleConditionalRequest(request, response, ctx, entity);
            }
        }
    }

    private Object safeEntity(ContainerResponseContext response) {
        try {
            return response.getEntity();
        } catch (Exception e) {
            return null;
        }
    }

    private void handleConditionalRequest(ContainerRequestContext request, ContainerResponseContext response,
                                          io.vertx.core.Context ctx, Object entity) {
        if (!config.lazyEtagEnabled()) return;

        String etag;
        try {
            etag = computeEtag(entity, ctx);
        } catch (Exception e) {
            return;
        }
        if (etag == null) return;

        response.getHeaders().putSingle("ETag", etag);

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            var ifNoneMatch = request.getHeaderString("If-None-Match");
            if (ifNoneMatch != null && ifNoneMatch.replace("W/", "").trim().equals(etag)) {
                response.setStatus(304);
                response.setEntity(null, null, null);
            }
        }
    }

    private String computeEtag(Object entity, io.vertx.core.Context ctx) {
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

    private void handleRedirect(ContainerRequestContext request, ContainerResponseContext response,
                                io.vertx.core.Context ctx) {
        if (response.getHeaders().getFirst("X-Inertia-Location") != null) return;

        String location = getLocation(response);
        if (location == null) return;

        var isPrefetch = Boolean.TRUE.equals(ctx.getLocal("inertia-prefetch"));

        if (location.contains("#") && !isPrefetch) {
            response.setStatus(409);
            response.getHeaders().putSingle("X-Inertia-Redirect", location);
            response.setEntity(null, null, null);
            return;
        }

        var method = (String) ctx.getLocal("request-method");
        int currStatus = response.getStatus();

        if (isExternalRedirect(request, location) && !isPrefetch
                && "GET".equalsIgnoreCase(method) && currStatus == 302) {
            response.setStatus(409);
            response.getHeaders().putSingle("X-Inertia-Location", location);
            response.setEntity(null, null, null);
            return;
        }

        if (method != null && isNonGet(method) && currStatus != 303 && currStatus != 302) {
            response.setStatus(303);
        }
    }

    private void handleEmptyResponse(ContainerResponseContext response, io.vertx.core.Context ctx) {
        String referer = (String) ctx.getLocal("referer-url");
        if (referer != null && !referer.isBlank()) {
            response.setStatus(302);
            response.getHeaders().putSingle("Location", referer);
        }
    }

    private boolean isRedirect(int status) {
        return status == 301 || status == 302 || status == 303 || status == 307 || status == 308;
    }

    private boolean isNonGet(String method) {
        return "POST".equalsIgnoreCase(method) ||
               "PUT".equalsIgnoreCase(method) ||
               "PATCH".equalsIgnoreCase(method) ||
               "DELETE".equalsIgnoreCase(method);
    }

    private boolean isExternalRedirect(ContainerRequestContext request, String location) {
        if (location.startsWith("/")) return false;
        try {
            var requestUri = request.getUriInfo().getRequestUri();
            var redirectUri = URI.create(location);
            if (!redirectUri.isAbsolute()) return false;
            return !requestUri.getAuthority().equals(redirectUri.getAuthority())
                || !requestUri.getScheme().equals(redirectUri.getScheme());
        } catch (Exception e) {
            return true;
        }
    }

    private String getLocation(ContainerResponseContext response) {
        var location = response.getHeaders().getFirst("Location");
        if (location instanceof String s) return s;
        if (location instanceof URI uri) return uri.toString();
        return null;
    }

    private boolean isEmpty(Object entity) {
        if (entity == null) return true;
        if (entity instanceof String s && s.isBlank()) return true;
        return false;
    }
}
