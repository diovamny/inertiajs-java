package io.github.dg.spring.inertia.internal;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.github.dg.spring.inertia.model.PageObject;
import io.github.dg.spring.inertia.protocol.InertiaHeaderExtractor;
import io.github.dg.spring.inertia.spi.ErrorMapper;
import io.github.dg.spring.inertia.spi.JsonProvider;

/**
 * Produces the default error response for exceptions that reach the
 * validation/error handlers: a 500 with the given component and error
 * metadata, or the response produced by a registered
 * {@link ErrorMapper}.
 */
public class ErrorResponseFactory {

    /** Request attribute holding the {@link ErrorMapper} of the visit. */
    public static final String CONTEXT_KEY = "inertia-error-mapper";

    private final JsonProvider jsonProvider;
    private final String component;
    private final int status;

    public ErrorResponseFactory(JsonProvider jsonProvider, String component, int status) {
        this.jsonProvider = jsonProvider;
        this.component = component;
        this.status = status;
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
     * Build an error page object for a server-side exception.
     *
     * @param error the server-side exception
     * @return the JSON page payload
     */
    public String createErrorPage(Throwable error) {
        var uri = InertiaRequestContext.uri();
        var version = InertiaRequestContext.get("inertia-version");
        var page = new PageObject(component, Map.of(
            "status", status,
            "message", String.valueOf(error)
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