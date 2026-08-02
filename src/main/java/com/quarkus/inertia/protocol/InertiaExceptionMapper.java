package com.quarkus.inertia.protocol;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import io.vertx.core.Vertx;

import com.quarkus.inertia.internal.ErrorResponseFactory;

/**
 * Catch-all mapper that turns server-side exceptions into Inertia error
 * responses (530 with an {@code error} payload, or the response produced by
 * a user-provided {@link com.quarkus.inertia.spi.ErrorMapper} registered via
 * {@code Inertia.handleErrorUsing(mapper)}). Only applies to Inertia
 * requests or when a custom mapper is registered; other errors fall back to
 * a plain 500 so non-Inertia clients keep default behavior.
 */
@Provider
@Priority(Priorities.USER)
public class InertiaExceptionMapper implements ExceptionMapper<Throwable> {

    @Inject
    ErrorResponseFactory errorResponseFactory;

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException wae && wae.getResponse() != null) {
            return wae.getResponse();
        }
        if (exception != null) {
            var cause = exception.getCause();
            if (cause instanceof WebApplicationException wae && wae.getResponse() != null) {
                return wae.getResponse();
            }
        }

        var hasMapper = errorResponseFactory.resolveMapper() != null;
        if (!hasMapper && !isInertiaRequest()) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal Server Error")
                .build();
        }

        return errorResponseFactory.handle(exception);
    }

    private boolean isInertiaRequest() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal("inertia-request");
            if (val != null) return Boolean.TRUE.equals(val);
        }
        return false;
    }
}
