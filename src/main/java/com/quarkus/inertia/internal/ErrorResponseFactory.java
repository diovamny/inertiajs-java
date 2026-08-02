package com.quarkus.inertia.internal;

import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.vertx.core.Vertx;

import com.quarkus.inertia.spi.ErrorMapper;

/**
 * Converts server-side exceptions into Inertia error responses. When an
 * {@link ErrorMapper} is registered for the current request, its response is
 * used; otherwise a 530 response carrying an {@code error} payload is
 * returned so the Inertia client can render its error modal.
 */
@ApplicationScoped
public class ErrorResponseFactory {

    static final String CONTEXT_KEY = "inertia-error-mapper";

    public Response handle(Throwable error) {
        var mapper = resolveMapper();
        if (mapper != null) {
            return mapper.map(error);
        }
        return defaultErrorResponse(error);
    }

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

    private Response defaultErrorResponse(Throwable error) {
        var message = error != null && error.getMessage() != null
            ? error.getMessage()
            : (error != null ? error.getClass().getSimpleName() : "Internal Server Error");
        var payload = Map.of("error", Map.of(
            "message", message,
            "exception", error != null ? error.getClass().getName() : "UnknownException"
        ));
        return Response.status(530)
            .entity(payload)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .header("X-Inertia", "true")
            .build();
    }
}
