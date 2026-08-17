package io.github.dg.spring.inertia.internal;

import java.util.function.Supplier;

/**
 * Internal lazy value wrapper produced by {@code inertia.cached(...)}: the
 * resolver only runs when the prop actually reaches the page object, so
 * props excluded by partial reloads never trigger the computation.
 */
public record LazyProp(Supplier<Object> resolver) {

    /**
     * Resolve the wrapped value.
     *
     * @return the computed value
     */
    public Object resolve() {
        return resolver.get();
    }
}