package com.quarkus.inertia.vertx;

import io.vertx.core.MultiMap;

/**
 * The status/headers/entity triple produced by
 * {@link InertiaResponseDecorator} that is written to the Vert.x response.
 *
 * @param status  the HTTP status
 * @param headers the response headers
 * @param entity  the body, or {@code null} for an empty response
 */
public record DecoratedResponse(int status, MultiMap headers, Object entity) {
}