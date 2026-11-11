package io.github.diovamny.quarkus.inertia.protocol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;

/**
 * Request-isolated registry for {@code defer(...)} calls chained after
 * {@code Inertia::render(...)}, mirroring Laravel's
 * {@code Inertia::defer(...)} fluent API.
 *
 * <p>The page assembly of the reactive pipeline is lazy, so registrations made
 * between {@code render()} and the subscription are visible to
 * {@link PageObjectBuilder} when it assembles the page. This enables the
 * {@code defer().live()} composition without re-serializing an eager page.</p>
 */
public final class DeferredPropRegistry {

    /** Request-local key holding the chained deferred registrations. */
    public static final String CONTEXT_CHAINED_DEFERRED = "inertia-chained-deferred";

    private DeferredPropRegistry() {
    }

    /**
     * A deferred prop registered after the render call.
     *
     * @param group    the deferred group name
     * @param key      the prop name
     * @param resolver produces the prop value on partial reload
     */
    public record DeferredSpec(String group, String key, Supplier<Uni<Object>> resolver) {
    }

    /**
     * Register a deferred prop for the current request.
     *
     * @param group    the deferred group name
     * @param key      the prop name
     * @param resolver produces the prop value on partial reload
     */
    public static void register(String group, String key, Supplier<Uni<Object>> resolver) {
        if (group == null || group.isBlank()) {
            throw new IllegalArgumentException("deferred group must not be blank");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("deferred key must not be blank");
        }
        if (resolver == null) {
            throw new IllegalArgumentException("deferred resolver must not be null");
        }
        var entries = entries();
        entries.put(key, new DeferredSpec(group, key, resolver));
        InertiaContextLocals.put(Vertx.currentContext(), CONTEXT_CHAINED_DEFERRED, entries);
    }

    /**
     * The deferred props registered after render for the current request.
     *
     * @return the registrations by prop name, in registration order
     */
    public static Map<String, DeferredSpec> entries() {
        var stored = InertiaContextLocals.get(Vertx.currentContext(), CONTEXT_CHAINED_DEFERRED);
        if (stored instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            var typed = (Map<String, DeferredSpec>) map;
            return new LinkedHashMap<>(typed);
        }
        return new LinkedHashMap<>();
    }

    /**
     * Group the chained deferred props by their deferred group.
     *
     * @return the groups to merge into the page's {@code deferredProps}
     */
    public static Map<String, List<String>> groups() {
        var groups = new LinkedHashMap<String, List<String>>();
        for (var spec : entries().values()) {
            var members = groups.computeIfAbsent(spec.group(), group -> new ArrayList<>());
            if (!members.contains(spec.key())) {
                members.add(spec.key());
            }
        }
        return groups;
    }
}
