package io.github.diovamny.spring.inertia.validation;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.diovamny.spring.inertia.internal.InertiaRequestContext;
import io.github.diovamny.spring.inertia.protocol.InertiaHeaderExtractor;
import io.github.diovamny.spring.inertia.protocol.PageObjectBuilder;
import io.github.diovamny.inertia.core.spi.FlashStore;
import io.github.diovamny.inertia.core.spi.JsonProvider;

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
    private io.github.diovamny.spring.inertia.config.InertiaProperties properties =
        new io.github.diovamny.spring.inertia.config.InertiaProperties();

    public PrecognitionHandler(JsonProvider jsonProvider, FlashStore flashStore) {
        this.jsonProvider = jsonProvider;
        this.flashStore = flashStore;
    }

    /**
     * Optional wiring for {@code inertia.validation.all-errors}: Spring calls
     * this setter when the {@code InertiaProperties} bean exists (always in a
     * Boot app). Kept as a setter (instead of a 3-arg constructor) so the
     * {@code @Bean} method signature stays binary-compatible with 0.0.4.
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setProperties(
            io.github.diovamny.spring.inertia.config.InertiaProperties properties) {
        if (properties != null) {
            this.properties = properties;
        }
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class, ConstraintViolationException.class })
    public ResponseEntity<?> handleValidation(Exception ex) {
        var bag = extractErrorBag(ex);
        var allErrors = properties != null && properties.getValidation() != null
            && properties.getValidation().isAllErrors();
        var errors = bag.toWireMap(allErrors);
        InertiaRequestContext.set(PageObjectBuilder.CONTEXT_ERRORS, errors);

        if (InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_PRECOGNITION) != null) {
            var filtered = filterToValidateOnly(errors);
            var headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Precognition", "true");
            io.github.diovamny.spring.inertia.util.VaryHeaderUtil.addTo(headers, "Precognition");
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).headers(headers)
                .body(jsonProvider.toJson(Map.of("errors", filtered)));
        }

        var errorBag = InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_ERROR_BAG);
        var errorsToFlash = errorBag != null ? Map.of(String.valueOf(errorBag), errors) : errors;
        flashStore.put("errors", errorsToFlash);
        return redirectBackWithErrors(errors);
    }

    private static io.github.diovamny.inertia.core.model.ValidationErrors extractErrorBag(Exception ex) {
        var builder = io.github.diovamny.inertia.core.model.ValidationErrors.builder();
        if (ex instanceof MethodArgumentNotValidException valid) {
            for (FieldError fieldError : valid.getBindingResult().getFieldErrors()) {
                builder.add(fieldError.getField(),
                    fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value");
            }
        } else if (ex instanceof ConstraintViolationException violations) {
            for (var violation : violations.getConstraintViolations()) {
                builder.add(String.valueOf(violation.getPropertyPath()), violation.getMessage());
            }
        }
        return builder.build();
    }

    /**
     * Keep only the errors for the fields listed in
     * {@code Precognition-Validate-Only}, so a precognition visit that
     * validates a single field never surfaces sibling errors. A blank or
     * absent header keeps every error.
     *
     * @param errors the full field error map
     * @return the filtered map
     */
    private static Map<String, Object> filterToValidateOnly(Map<String, Object> errors) {
        var raw = InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_PRECOGNITION_VALIDATE_FIELDS);
        if (raw == null) {
            return errors;
        }
        var fields = new LinkedHashSet<String>();
        for (var part : String.valueOf(raw).split(",")) {
            var field = part.trim();
            if (!field.isEmpty()) {
                fields.add(field);
            }
        }
        if (fields.isEmpty()) {
            return errors;
        }
        var filtered = new LinkedHashMap<String, Object>();
        for (var entry : errors.entrySet()) {
            if (fields.contains(entry.getKey())) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    private ResponseEntity<?> redirectBackWithErrors(Map<String, Object> errors) {
        var referer = InertiaRequestContext.header("Referer");
        var target = referer != null && !referer.isBlank() ? referer : "/";
        var status = "GET".equalsIgnoreCase(InertiaRequestContext.method())
            ? HttpStatus.FOUND : HttpStatus.SEE_OTHER;
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("Location", target);
        return ResponseEntity.status(status).headers(headers).build();
    }
}
