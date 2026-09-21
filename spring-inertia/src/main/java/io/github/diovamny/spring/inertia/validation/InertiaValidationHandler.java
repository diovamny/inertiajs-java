package io.github.diovamny.spring.inertia.validation;

import jakarta.validation.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import io.github.diovamny.spring.inertia.internal.ErrorResponseFactory;

/**
 * Converts server-side exceptions into Inertia error pages: business
 * {@link ValidationException}s always, any other exception on Inertia
 * visits. Errors registered with
 * {@code inertia.handleErrorUsing(mapper)} take precedence.
 */
@RestControllerAdvice
public class InertiaValidationHandler {

    private final ErrorResponseFactory errorResponseFactory;

    public InertiaValidationHandler(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<String> handleBusinessValidation(ValidationException ex) {
        return errorResponseFactory.map(ex);
    }

    /**
     * Missing routes/static resources are ordinary 404s: silent for plain
     * visits (e.g. browser probes like {@code /.well-known/...}) and an
     * Inertia error page with status 404 for X-Inertia visits.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleMissingResource(NoResourceFoundException ex) {
        return errorResponseFactory.notFound(ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOther(Exception ex) {
        if (isSpringSecurityException(ex)) {
            // Authorization/authentication failures belong to the Spring
            // Security chain (entry point / denied handler). Mapping them
            // here would swallow the 401/403 handling, including the Inertia
            // bridge responses, so they always propagate untouched.
            throw sneakyThrow(ex);
        }
        if (errorResponseFactory.isErrorResponseExpected()) {
            return errorResponseFactory.map(ex);
        }
        throw new UnhandledException(ex);
    }

    /**
     * Whether the exception is a Spring Security authorization or
     * authentication failure (resolved by class name so this module keeps no
     * compile-time dependency on Spring Security).
     *
     * @param ex the exception to inspect
     * @return {@code true} for Spring Security failures
     */
    static boolean isSpringSecurityException(Throwable ex) {
        if (!org.springframework.util.ClassUtils.isPresent(
                "org.springframework.security.access.AccessDeniedException",
                InertiaValidationHandler.class.getClassLoader())) {
            return false;
        }
        for (Class<?> type = ex.getClass(); type != null; type = type.getSuperclass()) {
            var name = type.getName();
            if (name.equals("org.springframework.security.access.AccessDeniedException")
                    || name.equals("org.springframework.security.core.AuthenticationException")) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException sneakyThrow(Throwable throwable) throws T {
        throw (T) throwable;
    }

    private static final class UnhandledException extends RuntimeException {
        UnhandledException(Throwable cause) {
            super(cause);
        }
    }
}
