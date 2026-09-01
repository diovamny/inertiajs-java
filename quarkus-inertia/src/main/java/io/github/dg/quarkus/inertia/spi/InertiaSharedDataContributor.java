package io.github.dg.quarkus.inertia.spi;

import java.util.Map;
import io.vertx.ext.web.RoutingContext;

/**
 * SPI for contributing global shared props to every Inertia page rendered by
 * the Quarkus application.
 *
 * <p>Implement as a CDI {@code @ApplicationScoped} bean; all registered
 * instances are automatically discovered and invoked during page rendering.</p>
 *
 * <p>Equivalent to Laravel's {@code HandleInertiaRequests::share()}.</p>
 */
@FunctionalInterface
public interface InertiaSharedDataContributor {

    /**
     * Contribute shared props for the current request.
     *
     * @param context the current Vert.x routing context (may be null in tests)
     * @return map of shared props to merge, or {@code null}/empty map
     */
    Map<String, Object> contribute(RoutingContext context);
}
