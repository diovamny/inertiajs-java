package io.github.diovamny.spring.inertia.spi;

import org.springframework.http.ResponseEntity;

/**
 * Optional hook to convert a server-side exception into an Inertia error
 * response. Register per request via {@code Inertia.handleErrorUsing(mapper)}.
 */
@FunctionalInterface
public interface ErrorMapper {

    /**
     * Convert an exception into the Inertia error response to send.
     *
     * @param error the server-side exception
     * @return the response (e.g. a 409 with {@code X-Inertia-Location})
     */
    ResponseEntity<?> map(Throwable error);
}
