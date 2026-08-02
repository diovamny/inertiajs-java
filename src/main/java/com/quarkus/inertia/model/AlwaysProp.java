package com.quarkus.inertia.model;

import java.util.Objects;

public record AlwaysProp<T>(T value) {

    public AlwaysProp {
        Objects.requireNonNull(value, "AlwaysProp value must not be null");
    }

    @SuppressWarnings("unchecked")
    public static <T> AlwaysProp<T> of(T value) {
        if (value instanceof AlwaysProp) {
            return (AlwaysProp<T>) value;
        }
        return new AlwaysProp<>(value);
    }
}
