package io.github.diovamny.quarkus.inertia.protocol;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;

/**
 * Read-only enforcement hook for precognition requests (Rails precognition
 * {@code prevent_writes} parity).
 *
 * <p>A precognition validate-only request ({@code Precognition: true} plus
 * {@code Precognition-Validate-Only} or its {@code X-Inertia-*} alias) is a
 * dry run: the client only asks which fields would fail validation. The
 * adapter itself never persists during such requests, but application
 * controllers might. Call {@link #assertMutationsAllowed()} at the top of
 * write paths (or from a service guard) to fail fast instead of leaking a
 * validation probe into a database mutation.</p>
 */
@ApplicationScoped
public class PrecognitionGuard {

    @Inject
    CurrentVertxRequest currentVertxRequest;

    /**
     * Whether the current request is a precognition request.
     */
    public boolean isPrecognitionRequest() {
        var rc = currentRequest();
        if (rc == null) {
            return false;
        }
        return isPrecognition(rc.request().getHeader("Precognition"))
            || isPrecognition(rc.request().getHeader("X-Inertia-Precognition"));
    }

    /**
     * Whether the current request is a validate-only dry run.
     */
    public boolean isValidateOnlyRequest() {
        var rc = currentRequest();
        if (rc == null) {
            return false;
        }
        var fields = rc.request().getHeader("Precognition-Validate-Only");
        if (fields == null || fields.isBlank()) {
            fields = rc.request().getHeader("X-Inertia-Precognition-Validate-Fields");
        }
        return isValidateOnly(fields);
    }

    /**
     * Fail fast when the current request is a validate-only dry run.
     *
     * @throws PrecognitionWriteBlockedException on validate-only requests
     */
    public void assertMutationsAllowed() {
        if (isValidateOnlyRequest()) {
            throw new PrecognitionWriteBlockedException(
                "Persistence writes are blocked during precognition validate-only requests");
        }
    }

    static boolean isPrecognition(String header) {
        return "true".equalsIgnoreCase(header);
    }

    static boolean isValidateOnly(String fieldsHeader) {
        return fieldsHeader != null && !fieldsHeader.isBlank();
    }

    private io.vertx.ext.web.RoutingContext currentRequest() {
        try {
            return currentVertxRequest.getCurrent();
        } catch (Exception e) {
            return null;
        }
    }
}
