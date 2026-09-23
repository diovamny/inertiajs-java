package io.github.diovamny.inertia.core.model;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Request-bound fluent builder over {@link MergePlan}.
 *
 * <p>Obtained from the adapter facade ({@code inertia.mergeable(key, value)}):
 * declare per-path operations, then call {@link #value()} to register the
 * plan against the current request and receive the value for the props map:</p>
 *
 * <pre>{@code
 * return inertia.render("Posts/Index", Map.of(
 *     "posts", inertia.mergeable("posts", posts)
 *         .append("data")
 *         .prepend("pinned")
 *         .matchOn("data.id")
 *         .value()));
 * }</pre>
 */
public final class MergeableBuilder {

    private final MergePlan.Builder delegate;
    private final Consumer<MergePlan> onRegister;
    private boolean registered;

    private MergeableBuilder(String propKey, Object value, Consumer<MergePlan> onRegister) {
        this.delegate = MergePlan.builder(propKey, value);
        this.onRegister = Objects.requireNonNull(onRegister, "onRegister");
    }

    /** New bound builder. */
    public static MergeableBuilder of(String propKey, Object value, Consumer<MergePlan> onRegister) {
        return new MergeableBuilder(propKey, value, onRegister);
    }

    /** Declare an append (merge) of a nested path. */
    public MergeableBuilder append(String path) {
        delegate.append(path);
        return this;
    }

    /** Declare a prepend of a nested path. */
    public MergeableBuilder prepend(String path) {
        delegate.prepend(path);
        return this;
    }

    /** Declare a deep-merge of a nested path. */
    public MergeableBuilder deep(String path) {
        delegate.deep(path);
        return this;
    }

    /** List-matching fields (dot-notation, relative to the prop root). */
    public MergeableBuilder matchOn(String... fields) {
        delegate.matchOn(fields);
        return this;
    }

    /** Build without registering (for inspection/testing). */
    public MergePlan plan() {
        return delegate.build();
    }

    /**
     * Build, register against the current request and return the value.
     *
     * @return the prop value for the props map
     */
    public Object value() {
        var plan = delegate.build();
        registered = true;
        onRegister.accept(plan);
        return plan.value();
    }

    /** Whether {@link #value()} was called. */
    public boolean isRegistered() {
        return registered;
    }
}
