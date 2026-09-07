package io.github.dg.spring.inertia.api;

import java.util.Map;

/**
 * Allows DTOs, domain models, records or entities to dynamically provide
 * properties to Inertia during page rendering and shared data resolution.
 */
@FunctionalInterface
public interface ProvidesInertiaProperties {

    /**
     * Get the properties to be provided to Inertia.
     *
     * @param context contextual information about the current render operation
     * @return map of properties to merge into the Inertia props
     */
    Map<String, Object> toInertiaProperties(RenderContext context);
}
