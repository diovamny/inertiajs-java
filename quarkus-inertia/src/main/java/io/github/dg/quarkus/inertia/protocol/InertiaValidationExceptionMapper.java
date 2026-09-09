package io.github.dg.quarkus.inertia.protocol;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import io.vertx.core.Vertx;

import io.github.dg.quarkus.inertia.internal.ErrorResponseFactory;

/**
 * Maps a business {@link ValidationException} (e.g. a violated domain rule
 * thrown by application code) into an Inertia error response: an Inertia page
 * (component {@code ErrorPage} by default, status {@code 422}) for Inertia
 * requests, or a plain {@code 422} for regular requests — Laravel's
 * {@code abort(422)} behavior.
 *
 * <p>{@link ConstraintViolationException} (form validation) is deliberately
 * left to {@link PrecognitionExceptionMapper} and the Quarkus validation
 * machinery: the more specific type always wins, so this mapper never
 * receives it.</p>
 */
@Provider
@Priority(Priorities.USER - 100)
public class InertiaValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    @Inject
    ErrorResponseFactory errorResponseFactory;

    @Override
    public Response toResponse(ValidationException exception) {
        if (exception instanceof ConstraintViolationException) {
            throw exception;
        }
        var status = errorResponseFactory.statusFor(exception);
        if (isInertiaRequest()) {
            return errorResponseFactory.handle(exception, status);
        }
        var message = exception.getMessage() != null ? exception.getMessage() : "Validation failed";
        return Response.status(status).entity(message).build();
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
