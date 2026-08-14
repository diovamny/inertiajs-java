package com.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import io.vertx.core.Vertx;

import com.quarkus.inertia.api.Inertia;
import com.quarkus.inertia.spi.JsonProvider;

/**
 * Maps a {@link ConstraintViolationException} (thrown by CDI bean
 * validation) into an Inertia precognition response: 422 with the
 * per-field errors wrapped as {@code {"errors": {...}}} and the
 * {@code Precognition: true} header, so the client only sees errors for
 * the fields it asked to validate (see {@code Precognition-Validate-Only}).
 * For regular form submissions the errors are flashed and the client is
 * redirected back to the form, mimicking the Laravel-inertia demo
 * behavior. Only applies to Inertia requests.
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR + 10)
public class PrecognitionExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Inject
    Inertia inertia;

    @Inject
    JsonProvider jsonProvider;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        var ctx = Vertx.currentContext();

        var validateFields = ctx != null
            ? (String) ctx.getLocal("inertia-precognition-validate-fields")
            : null;

        var errors = new HashMap<String, String>();
        for (var violation : exception.getConstraintViolations()) {
            var propertyPath = violation.getPropertyPath().toString();
            var field = propertyPath.contains(".") ?
                propertyPath.substring(propertyPath.lastIndexOf('.') + 1) : propertyPath;
            if (validateFields != null && !validateFields.isBlank()
                    && !matchesValidateOnly(validateFields, field)) {
                continue;
            }
            errors.put(field, violation.getMessage());
        }

        if (ctx == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(errors)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
        }

        var isInertia = Boolean.TRUE.equals(ctx.getLocal("inertia-request"));
        if (!isInertia) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(errors)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
        }

        var isPrecognition = Boolean.TRUE.equals(ctx.getLocal("inertia-precognition"));
        if (isPrecognition) {
            var errorBag = (String) ctx.getLocal("inertia-error-bag");
            var body = errorBag != null && !errorBag.isBlank()
                ? Map.of(errorBag, errors)
                : errors;
            var payload = Map.of("errors", body);
            try {
                var json = jsonProvider.toJson(payload);
                return Response.status(422)
                    .entity(json)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .header("X-Inertia", "true")
                    .header("Precognition", "true")
                    .header("Vary", "Precognition")
                    .build();
            } catch (Exception e) {
                return Response.status(422)
                    .entity(payload)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .header("X-Inertia", "true")
                    .header("Precognition", "true")
                    .header("Vary", "Precognition")
                    .build();
            }
        }

        var errorBag = (String) ctx.getLocal("inertia-error-bag");
        var errorsToFlash = errorBag != null && !errorBag.isBlank()
            ? Map.of(errorBag, errors)
            : errors;

        inertia.flash("errors", errorsToFlash);
        inertia.flash("_validation", true);

        var referer = (String) ctx.getLocal("referer-url");
        var location = (referer != null && !referer.isBlank()) ? referer : "/";
        var nonGet = "POST".equalsIgnoreCase((String) ctx.getLocal("request-method"))
            || "PUT".equalsIgnoreCase((String) ctx.getLocal("request-method"))
            || "PATCH".equalsIgnoreCase((String) ctx.getLocal("request-method"))
            || "DELETE".equalsIgnoreCase((String) ctx.getLocal("request-method"));
        var status = nonGet ? Response.Status.SEE_OTHER : Response.Status.FOUND;
        return Response.status(status)
            .header("Location", location)
            .header("Vary", "Accept")
            .build();
    }

    private boolean matchesValidateOnly(String validateFields, String field) {
        for (var candidate : validateFields.split(",")) {
            if (candidate.trim().equals(field)) return true;
        }
        return false;
    }
}
