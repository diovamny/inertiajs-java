package io.github.dg.spring.inertia.api;

import java.util.Set;

/**
 * Contextual information passed to {@link ProvidesInertiaProperties#toInertiaProperties(RenderContext)}.
 *
 * @param component the frontend component name
 * @param url the request URL
 * @param isPartial whether the current request is a partial reload
 * @param partialData the set of requested props on a partial reload, empty if not partial
 * @param partialExcept the set of excluded props on a partial reload, empty if not partial
 */
public record RenderContext(
        String component,
        String url,
        boolean isPartial,
        Set<String> partialData,
        Set<String> partialExcept
) {
    public RenderContext(String component, String url, boolean isPartial) {
        this(component, url, isPartial, Set.of(), Set.of());
    }

    /**
     * Determine if the given prop was specifically requested during a partial reload.
     * Always returns true on full page visits.
     *
     * @param prop the prop key
     * @return true if prop is active in this render
     */
    public boolean isPropRequested(String prop) {
        if (!isPartial) {
            return true;
        }
        if (partialExcept != null && partialExcept.contains(prop)) {
            return false;
        }
        if (partialData != null && !partialData.isEmpty()) {
            return partialData.contains(prop);
        }
        return true;
    }
}
