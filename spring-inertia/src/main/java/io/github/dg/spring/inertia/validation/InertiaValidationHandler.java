package io.github.dg.spring.inertia.validation;

import jakarta.validation.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.dg.spring.inertia.internal.ErrorResponseFactory;

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOther(Exception ex) {
        if (errorResponseFactory.isErrorResponseExpected()) {
            return errorResponseFactory.map(ex);
        }
        throw new UnhandledException(ex);
    }

    private static final class UnhandledException extends RuntimeException {
        UnhandledException(Throwable cause) {
            super(cause);
        }
    }
}