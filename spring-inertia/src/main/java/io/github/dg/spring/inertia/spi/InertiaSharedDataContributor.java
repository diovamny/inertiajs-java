package io.github.dg.spring.inertia.spi;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

/**
 * SPI for contributing global shared props to every Inertia page rendered by
 * the application.
 *
 * <p>Implement as a Spring {@code @Bean} or {@code @Component}; all registered
 * instances are automatically discovered and invoked during the request
 * lifecycle.</p>
 *
 * <p>Equivalent to Laravel's {@code HandleInertiaRequests::share()}.</p>
 */
@FunctionalInterface
public interface InertiaSharedDataContributor {

    /**
     * Contribute shared props for the current request.
     *
     * @param request the current HTTP servlet request
     * @return map of shared props to merge, or {@code null}/empty map
     */
    Map<String, Object> contribute(HttpServletRequest request);
}
