package io.github.dg.spring.inertia.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

/**
 * An Inertia JSON response. Extends {@link ResponseEntity} so Spring MVC
 * serves it natively through {@code HttpEntityMethodProcessor} without
 * custom converters.
 */
public class InertiaResponse extends ResponseEntity<String> {

    /**
     * @param status  the HTTP status
     * @param headers the response headers
     * @param body    the page JSON
     */
    public InertiaResponse(HttpStatusCode status, HttpHeaders headers, String body) {
        super(body, headers, status);
    }

    /**
     * Factory for a 200 Inertia JSON response.
     *
     * @param body    the page JSON
     * @param headers the response headers
     * @return the response
     */
    public static InertiaResponse ok(String body, HttpHeaders headers) {
        return new InertiaResponse(HttpStatusCode.valueOf(200), headers, body);
    }
}
