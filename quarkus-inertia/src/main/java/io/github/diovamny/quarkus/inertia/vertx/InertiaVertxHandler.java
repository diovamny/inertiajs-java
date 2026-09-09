package io.github.diovamny.quarkus.inertia.vertx;

import java.util.HashMap;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import io.github.diovamny.quarkus.inertia.api.Inertia;
import io.github.diovamny.quarkus.inertia.internal.ErrorResponseFactory;
import io.github.diovamny.quarkus.inertia.protocol.InertiaHeaderExtractor;
import io.github.diovamny.quarkus.inertia.security.InertiaCsrfService;

/**
 * Vert.x router pre-handler and failure handler that make reactive routes
 * ({@code @Route}) work with the Inertia protocol:
 *
 * <ul>
 * <li>extracts the Inertia headers into the Vert.x context and records the
 * routing context / request context for the reactive response writer;</li>
 * <li>validates the CSRF token when a session is present (the JAX-RS filter
 * remains the fallback when the session is created later);</li>
 * <li>the failure handler swallows the {@code NullPointerException} that the
 * generated reactive-routes handler raises for a {@code null} item (the
 * response has already been written), forwards JAX-RS failures, and renders
 * Inertia error responses for reactive routes.</li>
 * </ul>
 */
@ApplicationScoped
public class InertiaVertxHandler {

    @Inject
    InertiaHeaderExtractor headerExtractor;

    @Inject
    InertiaCsrfService csrfService;

    @Inject
    ReactiveResponseWriter responseWriter;

    @Inject
    ErrorResponseFactory errorResponseFactory;

    @Inject
    Inertia inertia;

    void setup(@Observes Router router) {
        router.route().order(-1).handler(this::handle).failureHandler(this::handleFailure);
    }

    private void handle(RoutingContext rc) {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal(ReactiveResponseWriter.ROUTING_CONTEXT_KEY, rc);
            ctx.putLocal(ReactiveResponseWriter.REQUEST_CONTEXT_KEY, ctx);
            ctx.putLocal(ReactiveResponseWriter.JAXRS_KEY, Boolean.FALSE);
            ctx.putLocal("inertia-headers-extracted", Boolean.TRUE);

            rc.put(ReactiveResponseWriter.ROUTING_CONTEXT_KEY, rc);
            rc.put(ReactiveResponseWriter.REQUEST_CONTEXT_KEY, ctx);

            headerExtractor.extract(ctx, rc.request().method().name(),
                rc.request().uri(), name -> rc.request().getHeader(name));
        }

        if (handleCsrf(rc)) {
            return;
        }

        rc.next();
    }

    /**
     * Validate the CSRF token when the session is already available. Returns
     * {@code true} when the request was rejected and must not continue.
     */
    private boolean handleCsrf(RoutingContext rc) {
        if (!csrfService.enabled()) return false;
        var session = rc.session();
        if (session == null) return false;

        var token = csrfService.getOrCreateToken(rc);
        var ctx = Vertx.currentContext();
        if (token != null && ctx != null) {
            ctx.putLocal(InertiaCsrfService.CONTEXT_TOKEN, token);
            ctx.putLocal(InertiaCsrfService.CONTEXT_HANDLED, Boolean.TRUE);
        }

        var method = rc.request().method().name();
        if (!csrfService.isStateChanging(method)) return false;

        if (!csrfService.matches(token, csrfService.resolveProvidedToken(rc))) {
            responseWriter.write(rc, Response.status(419).entity("CSRF token mismatch").build());
            return true;
        }
        return false;
    }

    private void handleFailure(RoutingContext rc) {
        if (rc.response().ended()) {
            return;
        }

        var ctx = Vertx.currentContext();
        if (ctx != null && Boolean.TRUE.equals(ctx.getLocal(ReactiveResponseWriter.JAXRS_KEY))) {
            rc.next();
            return;
        }

        responseWriter.write(rc, buildFailureResponse(rc, rc.failure()));
    }

    private Response buildFailureResponse(RoutingContext rc, Throwable failure) {
        var ctx = Vertx.currentContext();

        if (failure instanceof ConstraintViolationException cve) {
            return handleConstraintViolation(ctx, cve);
        }

        if (failure instanceof WebApplicationException wae) {
            if (isInertia(ctx)) {
                return errorResponseFactory.handle(failure, wae.getResponse().getStatus());
            }
            return wae.getResponse();
        }

        if (failure instanceof ValidationException ve) {
            var status = errorResponseFactory.statusFor(failure);
            if (isInertia(ctx)) {
                return errorResponseFactory.handle(failure, status);
            }
            var message = ve.getMessage() != null ? ve.getMessage() : "Validation failed";
            return Response.status(status).entity(message).build();
        }

        return errorResponseFactory.handle(failure);
    }

    private Response handleConstraintViolation(io.vertx.core.Context ctx,
            ConstraintViolationException exception) {
        var validateFields = ctx != null
            ? (String) ctx.getLocal("inertia-precognition-validate-fields")
            : null;

        var errors = new HashMap<String, String>();
        for (var violation : exception.getConstraintViolations()) {
            var propertyPath = violation.getPropertyPath().toString();
            var field = propertyPath.contains(".")
                ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1)
                : propertyPath;
            if (validateFields != null && !validateFields.isBlank()
                    && !matchesValidateOnly(validateFields, field)) {
                continue;
            }
            errors.put(field, violation.getMessage());
        }

        var isPrecognition = ctx != null && Boolean.TRUE.equals(ctx.getLocal("inertia-precognition"));
        if (isPrecognition) {
            var errorBag = (String) ctx.getLocal("inertia-error-bag");
            var body = errorBag != null && !errorBag.isBlank()
                ? Map.of(errorBag, errors)
                : errors;
            return Response.status(422)
                .entity(Map.of("errors", body))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header("Precognition", "true")
                .header("Vary", "Precognition")
                .build();
        }

        if (!isInertia(ctx)) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(errors)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
        }

        var errorBag = (String) ctx.getLocal("inertia-error-bag");
        var errorsToFlash = errorBag != null && !errorBag.isBlank()
            ? Map.of(errorBag, errors)
            : errors;

        inertia.flash("errors", errorsToFlash);
        inertia.flash("_validation", true);

        var referer = (String) ctx.getLocal("referer-url");
        var location = (referer != null && !referer.isBlank()) ? referer : "/";
        var method = (String) ctx.getLocal("request-method");
        var nonGet = "POST".equalsIgnoreCase(method)
            || "PUT".equalsIgnoreCase(method)
            || "PATCH".equalsIgnoreCase(method)
            || "DELETE".equalsIgnoreCase(method);
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

    private boolean isInertia(io.vertx.core.Context ctx) {
        return ctx != null && Boolean.TRUE.equals(ctx.getLocal("inertia-request"));
    }
}
