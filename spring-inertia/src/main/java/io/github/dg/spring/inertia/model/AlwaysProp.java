package io.github.dg.spring.inertia.model;

import java.util.Objects;

/**
 * Marker wrapper for a prop that must always be included in every page
 * object, even during partial reloads and even after shared props are
 * flushed.
 *
 * <p>Register one through {@code inertia.always(key, value)}; it is the
 * Spring equivalent of Laravel's {@code Inertia::always()}.</p>
 *
 * @param <T> the wrapped value type
 */
public record AlwaysProp<T>(T value) {

    /**
     * Compact constructor rejecting {@code null} values.
     *
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public AlwaysProp {
        Objects.requireNonNull(value, "AlwaysProp value must not be null");
    }

    /**
     * Wrap a value as an {@link AlwaysProp}, or return the existing wrapper
     * when the value already is one.
     *
     * @param value the value to wrap; must not be {@code null}
     * @param <T>   the value type
     * @return the wrapper
     */
    @SuppressWarnings("unchecked")
    public static <T> AlwaysProp<T> of(T value) {
        if (value instanceof AlwaysProp) {
            return (AlwaysProp<T>) value;
        }
        return new AlwaysProp<>(value);
    }
}