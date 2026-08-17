package io.github.dg.quarkus.inertia.model;

import java.util.function.Supplier;
import io.smallrye.mutiny.Uni;

/**
 * Registration of a deferred prop: a named prop inside a group, resolved
 * lazily when the client performs a partial reload of that group.
 *
 * @param <T>      the resolved value type
 * @param group    the deferred group name; the client requests groups via
 *                 {@code X-Inertia-Partial-Data} using the group member names
 * @param name     the prop name
 * @param resolver supplier producing the prop value
 */
public record DeferredProp<T>(
    String group,
    String name,
    Supplier<Uni<T>> resolver
) {

    /**
     * Resolve the prop value.
     *
     * @return the current value produced by the resolver
     */
    public Uni<T> resolve() {
        return resolver.get();
    }
}