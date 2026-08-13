package com.quarkus.inertia.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.vertx.core.Vertx;

import com.quarkus.inertia.config.InertiaConfig;
import com.quarkus.inertia.spi.ErrorMapper;
import com.quarkus.inertia.version.VersionProvider;

/**
 * Converts server-side exceptions into Inertia error responses. When an
 * {@link ErrorMapper} is registered for the current request, its response is
 * used; otherwise an Inertia page response is produced (component from
 * {@code inertia.error-component}, default {@code ErrorPage}) carrying the
 * exception status and message as props, so the Inertia client can render an
 * error page component (Laravel parity).
 */
@ApplicationScoped
public class ErrorResponseFactory {

    static final String CONTEXT_KEY = "inertia-error-mapper";

    @Inject
    InertiaConfig config;

    @Inject
    VersionProvider versionProvider;

    /**
     * Produce the error response for a server-side exception using the
     * configured error status.
     *
     * @param error the exception thrown while handling the request
     * @return the response to send
     */
    public Response handle(Throwable error) {
        return handle(error, config.errorStatus());
    }

    /**
     * Produce the error response for a server-side exception with an explicit
     * HTTP status.
     *
     * @param error  the exception thrown while handling the request
     * @param status the HTTP status of the error response
     * @return the response to send
     */
    public Response handle(Throwable error, int status) {
        var mapper = resolveMapper();
        if (mapper != null) {
            return mapper.map(error);
        }
        return errorPageResponse(error, status);
    }

    /**
     * The mapper registered for the current request, if any.
     *
     * @return the mapper, or {@code null}
     */
    public ErrorMapper resolveMapper() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal(CONTEXT_KEY);
            if (val instanceof ErrorMapper mapper) {
                return mapper;
            }
        }
        return null;
    }

    private Response errorPageResponse(Throwable error, int status) {
        var message = error != null && error.getMessage() != null
            ? error.getMessage()
            : (error != null ? error.getClass().getSimpleName() : "Internal Server Error");

        var props = new LinkedHashMap<String, Object>();
        props.put("status", status);
        props.put("message", message);

        var payload = new LinkedHashMap<String, Object>();
        payload.put("component", config.errorComponent());
        payload.put("props", props);
        payload.put("url", resolveUrl());
        payload.put("version", versionProvider.getVersion());

        return Response.status(status)
            .entity(payload)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .header("X-Inertia", "true")
            .header("Vary", "X-Inertia")
            .build();
    }

    private String resolveUrl() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var uri = ctx.getLocal("request-uri");
            if (uri != null) return (String) uri;
        }
        return "/";
    }
}
