package com.quarkus.inertia.spi;

import jakarta.ws.rs.core.Response;

/**
 * Optional hook to convert a server-side exception into an Inertia error
 * response. Register per request via {@code Inertia.handleErrorUsing(mapper)}.
 */
@FunctionalInterface
public interface ErrorMapper {
    Response map(Throwable error);
}
