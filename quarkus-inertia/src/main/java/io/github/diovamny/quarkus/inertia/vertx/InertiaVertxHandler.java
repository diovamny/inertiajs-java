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

    @Inject
    io.github.diovamny.quarkus.inertia.config.InertiaConfig config;

    void setup(@Observes Router router) {
        router.route().order(-1).handler(this::handle).failureHandler(this::handleFailure);
    }

    private void handle(RoutingContext rc) {
        var ctx = Vertx.currentContext();
        // Bind the routing context BEFORE extracting headers so every
        // InertiaContextLocals.put() mirrors into rc (request-scoped) instead
        // of leaking into the shared event-loop context (G-17).
        if (ctx != null) {
            ctx.putLocal(ReactiveResponseWriter.ROUTING_CONTEXT_KEY, rc);
            ctx.putLocal(io.github.diovamny.quarkus.inertia.protocol.InertiaContextLocals.ROUTING_CONTEXT_KEY, rc);
            ctx.putLocal(ReactiveResponseWriter.REQUEST_CONTEXT_KEY, ctx);
            ctx.putLocal(ReactiveResponseWriter.JAXRS_KEY, Boolean.FALSE);

            rc.put(ReactiveResponseWriter.ROUTING_CONTEXT_KEY, rc);
            rc.put(io.github.diovamny.quarkus.inertia.protocol.InertiaContextLocals.ROUTING_CONTEXT_KEY, rc);
            rc.put(ReactiveResponseWriter.REQUEST_CONTEXT_KEY, ctx);

            headerExtractor.extract(rc, ctx, rc.request().method().name(),
                rc.request().uri(), name -> rc.request().getHeader(name));
            io.github.diovamny.quarkus.inertia.protocol.InertiaContextLocals.put(
                ctx, "inertia-headers-extracted", Boolean.TRUE);
            rc.put("inertia-headers-extracted", Boolean.TRUE);
            rc.addEndHandler(v -> clearRequestState(ctx));
        } else {
            // No Vert.x context (worker thread edge case): still bind rc.
            rc.put(ReactiveResponseWriter.ROUTING_CONTEXT_KEY, rc);
            rc.put(io.github.diovamny.quarkus.inertia.protocol.InertiaContextLocals.ROUTING_CONTEXT_KEY, rc);
            headerExtractor.extract(rc, null, rc.request().method().name(),
                rc.request().uri(), name -> rc.request().getHeader(name));
            rc.put("inertia-headers-extracted", Boolean.TRUE);
        }

        if (handleCsrf(rc)) {
            return;
        }

        rc.next();
    }

    /**
     * Validate the CSRF token when the session is already available. Returns
     * {@code true} when the request was rejected and must not continue.
     *
     * <p>Failed Inertia visits are answered with {@code 303} back to the
     * same-origin referer (or the configured fallback) carrying a generic
     * flash message; other requests keep the legacy {@code 419}.</p>
     */
    private boolean handleCsrf(RoutingContext rc) {
        // Adapter-owned validation only: in framework mode quarkus-rest-csrf
        // owns JAX-RS and InertiaReactiveCsrfFilter owns reactive routes, so
        // this pre-handler (which cannot tell transports apart yet) stands
        // down to avoid double validation with a different token.
        if (!csrfService.enabled()) return false;
        var session = rc.session();
        if (session == null) return false;

        var token = csrfService.getOrCreateToken(rc);
        var ctx = Vertx.currentContext();
        if (token != null) {
            rc.put(InertiaCsrfService.CONTEXT_TOKEN, token);
            rc.put(InertiaCsrfService.CONTEXT_HANDLED, Boolean.TRUE);
            if (ctx != null) {
                ctx.putLocal(InertiaCsrfService.CONTEXT_TOKEN, token);
                ctx.putLocal(InertiaCsrfService.CONTEXT_HANDLED, Boolean.TRUE);
            }
        }

        var method = rc.request().method().name();
        if (!csrfService.isStateChanging(method)) return false;

        if (!csrfService.matches(token, csrfService.resolveProvidedToken(rc))) {
            if (isInertiaVisit(rc)) {
                var config = csrfConfig();
                inertia.flash(config.securityCsrfFlashKey(), config.securityCsrfFlashMessage());
                var target = io.github.diovamny.quarkus.inertia.security.InertiaSecurityModes
                    .safeFailureTarget(rc.request().getHeader("Referer"),
                        rc.request().scheme(), rc.request().authority().toString(),
                        config.securityCsrfFailurePath());
                responseWriter.write(rc, Response.status(Response.Status.SEE_OTHER)
                    .header("Location", target).build());
            } else {
                responseWriter.write(rc, Response.status(419).entity("CSRF token mismatch").build());
            }
            return true;
        }
        return false;
    }

    private boolean isInertiaVisit(RoutingContext rc) {
        var header = rc.request().getHeader("X-Inertia");
        return header != null && ("true".equalsIgnoreCase(header) || Boolean.parseBoolean(header));
    }



    private io.github.diovamny.quarkus.inertia.config.InertiaConfig csrfConfig() {
        return config;
    }

    private void handleFailure(RoutingContext rc) {
        if (rc.response().ended()) {
            return;
        }

        var ctx = Vertx.currentContext();
        Object jaxrsFlag = rc.get(ReactiveResponseWriter.JAXRS_KEY);
        if (jaxrsFlag == null && ctx != null) {
            jaxrsFlag = ctx.getLocal(ReactiveResponseWriter.JAXRS_KEY);
        }
        if (Boolean.TRUE.equals(jaxrsFlag)) {
            rc.next();
            return;
        }

        responseWriter.write(rc, buildFailureResponse(rc, rc.failure()));
    }

    private Response buildFailureResponse(RoutingContext rc, Throwable failure) {
        var ctx = Vertx.currentContext();

        if (failure instanceof ConstraintViolationException cve) {
            return handleConstraintViolation(rc, ctx, cve);
        }

        if (failure instanceof WebApplicationException wae) {
            if (isInertia(rc, ctx)) {
                return errorResponseFactory.handle(failure, wae.getResponse().getStatus());
            }
            return wae.getResponse();
        }

        if (failure instanceof ValidationException ve) {
            var status = errorResponseFactory.statusFor(failure);
            if (isInertia(rc, ctx)) {
                return errorResponseFactory.handle(failure, status);
            }
            var message = ve.getMessage() != null ? ve.getMessage() : "Validation failed";
            return Response.status(status).entity(message).build();
        }

        return errorResponseFactory.handle(failure);
    }

    private Response handleConstraintViolation(RoutingContext rc, io.vertx.core.Context ctx,
            ConstraintViolationException exception) {
        var validateFields = rc.get("inertia-precognition-validate-fields") instanceof String s
            ? s
            : ctx != null
                ? (String) io.github.diovamny.quarkus.inertia.protocol.InertiaContextLocals.get(
                    ctx, "inertia-precognition-validate-fields")
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

        var isPrecognition = isTrue(rc, ctx, "inertia-precognition");
        if (isPrecognition) {
            var errorBag = getString(rc, ctx, "inertia-error-bag");
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

        if (!isInertia(rc, ctx)) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(errors)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
        }

        var errorBag = getString(rc, ctx, "inertia-error-bag");
        var errorsToFlash = errorBag != null && !errorBag.isBlank()
            ? Map.of(errorBag, errors)
            : errors;

        inertia.flash("errors", errorsToFlash);
        inertia.flash("_validation", true);

        var referer = getString(rc, ctx, "referer-url");
        var location = (referer != null && !referer.isBlank()) ? referer : "/";
        var method = getString(rc, ctx, "request-method");
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
        return ctx != null
            && io.github.diovamny.quarkus.inertia.protocol.InertiaContextLocals.isTrue(ctx, "inertia-request");
    }

    private boolean isInertia(RoutingContext rc, io.vertx.core.Context ctx) {
        if (rc != null && Boolean.TRUE.equals(rc.get("inertia-request"))) {
            return true;
        }
        return isInertia(ctx);
    }

    private static String getString(RoutingContext rc, io.vertx.core.Context ctx, String key) {
        if (rc != null && rc.get(key) instanceof String s) {
            return s;
        }
        if (ctx == null) {
            return null;
        }
        Object value = io.github.diovamny.quarkus.inertia.protocol.InertiaContextLocals.get(ctx, key);
        return value != null ? String.valueOf(value) : null;
    }

    private static boolean isTrue(RoutingContext rc, io.vertx.core.Context ctx, String key) {
        if (rc != null && Boolean.TRUE.equals(rc.get(key))) {
            return true;
        }
        return ctx != null
            && io.github.diovamny.quarkus.inertia.protocol.InertiaContextLocals.isTrue(ctx, key);
    }

    /**
     * Remove per-request keys from the shared event-loop context when the
     * request ends, so pipelined requests on the same loop never inherit
     * stale Inertia headers (G-17 defense in depth; readers already prefer
     * the routing context).
     *
     * @param ctx event-loop context at request start
     */
    private static void clearRequestState(io.vertx.core.Context ctx) {
        if (ctx == null) {
            return;
        }
        String[] keys = {
            "inertia-request", "request-method", "request-uri", "inertia-version",
            "inertia-partial-component", "inertia-partial-data", "inertia-partial-except",
            "inertia-reset", "inertia-except-once-props", "inertia-error-bag",
            "inertia-scroll-merge-intent", "inertia-precognition",
            "inertia-precognition-validate-fields", "inertia-prefetch", "referer-url",
            "inertia-headers-extracted"
        };
        for (String key : keys) {
            try {
                ctx.removeLocal(key);
            } catch (Exception ignored) {
                // Fallback for Vert.x versions without removeLocal(String).
                try {
                    ctx.putLocal(key, null);
                } catch (Exception alsoIgnored) {
                    // Best effort only.
                }
            }
        }
    }
}
