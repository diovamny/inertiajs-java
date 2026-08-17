package io.github.dg.spring.inertia.validation;

import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.dg.spring.inertia.internal.InertiaRequestContext;
import io.github.dg.spring.inertia.protocol.InertiaHeaderExtractor;
import io.github.dg.spring.inertia.protocol.PageObjectBuilder;
import io.github.dg.spring.inertia.spi.FlashStore;
import io.github.dg.spring.inertia.spi.JsonProvider;

/**
 * Handles request validation failures:
 *
 * <ul>
 *   <li>precognition visits - 422 with the field errors in JSON</li>
 *   <li>regular Inertia form submits - errors are flashed and the visit is
 *       redirected back so the page re-renders with the errors</li>
 * </ul>
 *
 * The errors are always exposed through the request context, so a page
 * rendered in the same visit carries them under the
 * {@code errors}/{@code errors.<bag>} prop.
 */
@RestControllerAdvice
public class PrecognitionHandler {

    private final JsonProvider jsonProvider;
    private final FlashStore flashStore;

    public PrecognitionHandler(JsonProvider jsonProvider, FlashStore flashStore) {
        this.jsonProvider = jsonProvider;
        this.flashStore = flashStore;
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class, ConstraintViolationException.class })
    public ResponseEntity<?> handleValidation(Exception ex) {
        var errors = extractErrors(ex);
        InertiaRequestContext.set(PageObjectBuilder.CONTEXT_ERRORS, errors);

        if (InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_PRECOGNITION) != null) {
            var headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Inertia", "true");
            headers.set("Vary", "X-Inertia");
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).headers(headers)
                .body(jsonProvider.toJson(Map.of("errors", errors)));
        }

        InertiaRequestContext.set(InertiaHeaderExtractor.CONTEXT_ERROR_BAG, null);
        flashStore.put("errors", errors);
        return redirectBackWithErrors(errors);
    }

    private static Map<String, String> extractErrors(Exception ex) {
        var errors = new LinkedHashMap<String, String>();
        if (ex instanceof MethodArgumentNotValidException valid) {
            for (FieldError fieldError : valid.getBindingResult().getFieldErrors()) {
                errors.putIfAbsent(fieldError.getField(),
                    fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value");
            }
        } else if (ex instanceof ConstraintViolationException violations) {
            for (var violation : violations.getConstraintViolations()) {
                errors.putIfAbsent(String.valueOf(violation.getPropertyPath()),
                    violation.getMessage());
            }
        }
        return errors;
    }

    private ResponseEntity<?> redirectBackWithErrors(Map<String, String> errors) {
        var referer = InertiaRequestContext.header("Referer");
        var target = referer != null && !referer.isBlank() ? referer : "/";
        var status = "GET".equalsIgnoreCase(InertiaRequestContext.method())
            ? HttpStatus.FOUND : HttpStatus.SEE_OTHER;
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("Location", target);
        return ResponseEntity.status(status).headers(headers).build();
    }
}