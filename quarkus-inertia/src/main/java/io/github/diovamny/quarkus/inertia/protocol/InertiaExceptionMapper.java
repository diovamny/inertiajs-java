package io.github.diovamny.quarkus.inertia.protocol;

import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import io.vertx.core.Vertx;

import io.github.diovamny.quarkus.inertia.internal.ErrorResponseFactory;

/**
 * Catch-all mapper that turns server-side exceptions into Inertia error
 * responses: an Inertia page (component {@code ErrorPage} by default, status
 * from the exception) for Inertia requests, or the response produced by a
 * user-provided {@link io.github.diovamny.quarkus.inertia.spi.ErrorMapper} registered via
 * {@code Inertia.handleErrorUsing(mapper)}. Non-Inertia requests keep default
 * behavior (plain 500 or the {@code WebApplicationException} response).
 */
@Provider
@Priority(Priorities.USER)
public class InertiaExceptionMapper implements ExceptionMapper<Throwable> {

    @Inject
    ErrorResponseFactory errorResponseFactory;

    private static final Logger LOG = Logger.getLogger(InertiaExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException wae && wae.getResponse() != null) {
            return handleWebApplicationException(wae);
        }
        if (exception != null) {
            var cause = exception.getCause();
            if (cause instanceof WebApplicationException wae && wae.getResponse() != null) {
                return handleWebApplicationException(wae);
            }
        }

        var hasMapper = errorResponseFactory.resolveMapper() != null;
        if (!hasMapper && !isInertiaRequest()) {
            var status = errorResponseFactory.statusFor(exception);
            var message = exception != null && exception.getMessage() != null
                ? exception.getMessage()
                : "Internal Server Error";
            return Response.status(status)
                .entity(message)
                .build();
        }

        LOG.error("InertiaExceptionMapper caught exception during request", exception);
        return errorResponseFactory.handle(exception);
    }

    private Response handleWebApplicationException(WebApplicationException wae) {
        if (isInertiaRequest()) {
            return errorResponseFactory.handle(wae, wae.getResponse().getStatus());
        }
        return wae.getResponse();
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
