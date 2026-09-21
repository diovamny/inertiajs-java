package io.github.diovamny.spring.inertia.validation;

import io.github.diovamny.spring.inertia.internal.InertiaRequestContext;

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
 * validation probe into a database mutation. Outside a request all answers
 * are negative.</p>
 */
public final class PrecognitionGuard {

    private PrecognitionGuard() {
    }

    /**
     * Whether the current request is a precognition request.
     */
    public static boolean isPrecognitionRequest() {
        return isPrecognition(header("Precognition"))
            || isPrecognition(header("X-Inertia-Precognition"));
    }

    /**
     * Whether the current request is a validate-only dry run.
     */
    public static boolean isValidateOnlyRequest() {
        var fields = header("Precognition-Validate-Only");
        if (fields == null || fields.isBlank()) {
            fields = header("X-Inertia-Precognition-Validate-Fields");
        }
        return isValidateOnly(fields);
    }

    /**
     * Fail fast when the current request is a validate-only dry run.
     *
     * @throws PrecognitionWriteBlockedException on validate-only requests
     */
    public static void assertMutationsAllowed() {
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

    private static String header(String name) {
        try {
            return InertiaRequestContext.header(name);
        } catch (Exception e) {
            return null;
        }
    }
}
