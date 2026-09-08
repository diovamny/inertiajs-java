package io.github.dg.spring.inertia.internal;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import io.github.dg.spring.inertia.config.InertiaProperties;
import io.github.dg.spring.inertia.model.PageObject;
import io.github.dg.spring.inertia.protocol.InertiaHeaderExtractor;
import io.github.dg.spring.inertia.spi.ErrorMapper;
import io.github.dg.spring.inertia.spi.JsonProvider;

/**
 * Produces the default error response for exceptions that reach the
 * validation/error handlers: a 500 with the given component and error
 * metadata, or the response produced by a registered
 * {@link ErrorMapper}.
 *
 * <p>In production mode, error messages are replaced with a generic message
 * to avoid leaking internal details. Set {@code inertia.error-details-enabled}
 * to {@code true} to include exception messages in error responses.</p>
 */
public class ErrorResponseFactory {

    /** Request attribute holding the {@link ErrorMapper} of the visit. */
    public static final String CONTEXT_KEY = "inertia-error-mapper";

    private final JsonProvider jsonProvider;
    private final String component;
    private final int status;
    private final boolean errorDetailsEnabled;

    public ErrorResponseFactory(JsonProvider jsonProvider, InertiaProperties properties) {
        this.jsonProvider = jsonProvider;
        this.component = properties.getErrorComponent();
        this.status = properties.getErrorStatus();
        this.errorDetailsEnabled = properties.isErrorDetailsEnabled();
    }

    /**
     * Build the error response for the given exception.
     *
     * @param error the server-side exception
     * @return a 500 (or mapped) {@link ResponseEntity} carrying the page JSON
     */
    public ResponseEntity<String> map(Throwable error) {
        var custom = InertiaRequestContext.get(CONTEXT_KEY);
        if (custom instanceof ErrorMapper mapper) {
            var mapped = mapper.map(error);
            if (mapped != null) {
                return ResponseEntity.status(mapped.getStatusCode())
                    .headers(mapped.getHeaders())
                    .body(mapped.getBody() != null ? String.valueOf(mapped.getBody()) : null);
            }
        }
        return ResponseEntity.status(status).body(createErrorPage(error));
    }

    /**
     * Build the 404 response for a missing route or static resource: silent
     * for plain visits, an Inertia error page with status 404 for X-Inertia
     * visits. A registered {@link ErrorMapper} wins over both defaults.
     *
     * @param error the missing-resource exception
     * @return the 404 response
     */
    public ResponseEntity<String> notFound(NoResourceFoundException error) {
        var custom = InertiaRequestContext.get(CONTEXT_KEY);
        if (custom instanceof ErrorMapper mapper) {
            var mapped = mapper.map(error);
            if (mapped != null) {
                return ResponseEntity.status(mapped.getStatusCode())
                    .headers(mapped.getHeaders())
                    .body(mapped.getBody() != null ? String.valueOf(mapped.getBody()) : null);
            }
        }
        if (!isErrorResponseExpected()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(createErrorPage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase()));
    }

    /**
     * Build an error page object for a server-side exception.
     *
     * @param error the server-side exception
     * @return the JSON page payload
     */
    public String createErrorPage(Throwable error) {
        var message = errorDetailsEnabled ? String.valueOf(error) : "Internal Server Error";
        return createErrorPage(status, message);
    }

    /**
     * Build an error page object with the given status and message.
     *
     * @param status  the HTTP status of the error
     * @param message the error message
     * @return the JSON page payload
     */
    public String createErrorPage(int status, String message) {
        var uri = InertiaRequestContext.uri();
        var version = InertiaRequestContext.get("inertia-version");
        var page = new PageObject(component, Map.of(
            "status", status,
            "message", message
        ), uri, version != null ? String.valueOf(version) : null);
        return jsonProvider.toJson(page);
    }

    /**
     * Whether the current request expects a JSON error payload (i.e. an
     * Inertia request).
     *
     * @return {@code true} for Inertia visits
     */
    public boolean isErrorResponseExpected() {
        var inertia = InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_INERTIA);
        return inertia != null && ("true".equalsIgnoreCase(String.valueOf(inertia))
            || Boolean.parseBoolean(String.valueOf(inertia)));
    }
}
