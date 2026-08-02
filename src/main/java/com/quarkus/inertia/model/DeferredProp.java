package com.quarkus.inertia.model;

import java.util.function.Supplier;
import io.smallrye.mutiny.Uni;

public record DeferredProp<T>(
    String group,
    String name,
    Supplier<Uni<T>> resolver
) {
    public Uni<T> resolve() {
        return resolver.get();
    }
}
