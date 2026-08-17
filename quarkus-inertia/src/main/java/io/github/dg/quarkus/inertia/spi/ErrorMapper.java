package io.github.dg.quarkus.inertia.spi;

import jakarta.ws.rs.core.Response;

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
     * @return the response (e.g. {@code inertia.external(uri)} for a 409)
     */
    Response map(Throwable error);
}
