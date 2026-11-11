package io.github.diovamny.quarkus.inertia.protocol;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;

import io.github.diovamny.inertia.core.model.LivePropListener;
import io.github.diovamny.inertia.core.model.LivePropMetadata;

/**
 * Request-isolated registry of live props.
 *
 * <p>The JSON wire model ({@link LivePropMetadata}) never contains the
 * resolver; this registry keeps the {@code Supplier<Uni<Object>>} in
 * {@link InertiaContextLocals} so it is discarded when the request ends.
 * Resolution follows the partial-reload contract:</p>
 *
 * <ul>
 *   <li><strong>Full visit</strong>: every non-deferred live prop is resolved
 *   (sequentially, in registration order) and written into the props.</li>
 *   <li><strong>Partial reload</strong>: only live props selected by
 *   {@code X-Inertia-Partial-Data}/{@code X-Inertia-Partial-Except} are
 *   resolved; foreign live props keep their metadata but never run their
 *   resolver.</li>
 *   <li><strong>Deferred composition</strong> ({@code defer + live}): the
 *   deferred pipeline owns the resolver; live resolution only records the key
 *   as already resolved to avoid double evaluation.</li>
 * </ul>
 *
 * <p>The state lives in the Vert.x {@code RoutingContext} instead of the
 * event-loop context, so concurrent reactive-route requests never cross-read
 * each other's resolvers (M7 isolation contract).</p>
 */
public final class LivePropRegistry {

    /** Request-local key holding the live resolvers. */
    public static final String CONTEXT_LIVE_RESOLVERS = "inertia-live-resolvers";

    /** Request-local key holding the wire metadata. */
    public static final String CONTEXT_LIVE_METADATA = "inertia-live-metadata";

    /** Request-local key holding the keys already resolved. */
    public static final String CONTEXT_LIVE_RESOLVED = "inertia-live-resolved";

    private LivePropRegistry() {
    }

    /**
     * Register a live prop for the current request.
     *
     * @param key        the prop name
     * @param resolver   produces the current value asynchronously
     * @param throttleMs minimum interval between updates, or {@code null}
     * @param listeners  channels and events that trigger updates
     */
    public static void register(String key, Supplier<Uni<Object>> resolver, Integer throttleMs,
            LivePropListener... listeners) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("live prop key must not be blank");
        }
        if (resolver == null) {
            throw new IllegalArgumentException("live prop resolver must not be null");
        }
        var resolvers = resolvers();
        resolvers.put(key, resolver);
        InertiaContextLocals.put(Vertx.currentContext(), CONTEXT_LIVE_RESOLVERS, resolvers);

        var metadata = metadata();
        var safeListeners = listeners != null ? List.of(listeners) : List.<LivePropListener>of();
        metadata.put(key, new LivePropMetadata(safeListeners, throttleMs));
        InertiaContextLocals.put(Vertx.currentContext(), CONTEXT_LIVE_METADATA, metadata);
    }

    /**
     * The live props registered for the current request.
     *
     * @return the wire metadata by prop name, empty when none
     */
    public static Map<String, LivePropMetadata> metadata() {
        var stored = InertiaContextLocals.get(Vertx.currentContext(), CONTEXT_LIVE_METADATA);
        if (stored instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            var typed = (Map<String, LivePropMetadata>) map;
            return new LinkedHashMap<>(typed);
        }
        return new LinkedHashMap<>();
    }

    /**
     * The keys of every live prop registered for the current request.
     *
     * @return the registered prop names, in registration order
     */
    public static Set<String> keys() {
        return resolvers().keySet();
    }

    /**
     * Whether a resolver was registered for the given key.
     *
     * @param key the prop name
     * @return {@code true} when a live resolver exists
     */
    public static boolean isRegistered(String key) {
        return resolvers().containsKey(key);
    }

    /**
     * Whether the given key was already resolved during this request.
     *
     * @param key the prop name
     * @return {@code true} when it must not be evaluated again
     */
    public static boolean isResolved(String key) {
        return resolvedKeys().contains(key);
    }

    /**
     * Record a key as resolved so a later stage does not run the resolver a
     * second time.
     *
     * @param key the prop name
     */
    public static void markResolved(String key) {
        var resolved = resolvedKeys();
        resolved.add(key);
        storeResolved(resolved);
    }

    /**
     * Whether a live prop would be resolved by the given partial context.
     *
     * @param key            the prop name
     * @param component      the component of the current page
     * @param partialContext the partial-reload headers, or {@code null}
     * @return {@code true} on a full visit or when the partial selects the key
     */
    public static boolean isSelected(String key, String component,
            PartialReloadProcessor.PartialReloadContext partialContext) {
        if (partialContext == null || !partialContext.matchesComponent(component)) {
            return true;
        }
        if (partialContext.hasData()) {
            return partialContext.data().contains(key);
        }
        if (partialContext.hasExcept()) {
            return !partialContext.except().contains(key);
        }
        return false;
    }

    /**
     * Resolve the selected live props for the current visit.
     *
     * <p>Resolvers run sequentially in registration order, so a resolver may
     * observe the side effects of the previous one (the TCK relies on this
     * for its shared counter). Keys owned by the deferred pipeline are never
     * evaluated here; when the deferred pipeline already produced a value it
     * is only marked as resolved.</p>
     *
     * @param props          the page props, mutated with resolved live values
     * @param component      the page component rendered by this request
     * @param partialContext the partial-reload headers, or {@code null}
     * @param deferredKeys   keys owned by the deferred pipeline
     * @return a {@link Uni} emitting the same props map once resolved
     */
    public static Uni<Map<String, Object>> resolveInto(Map<String, Object> props, String component,
            PartialReloadProcessor.PartialReloadContext partialContext, Set<String> deferredKeys) {
        var resolvers = resolvers();
        if (resolvers.isEmpty()) {
            return Uni.createFrom().item(props);
        }

        var resolved = resolvedKeys();
        var result = Uni.createFrom().item(props);
        for (var entry : resolvers.entrySet()) {
            var key = entry.getKey();
            if (resolved.contains(key)) {
                continue;
            }
            if (deferredKeys.contains(key)) {
                if (props.containsKey(key)) {
                    resolved.add(key);
                }
                continue;
            }
            if (!isSelected(key, component, partialContext)) {
                continue;
            }
            var resolver = entry.getValue();
            result = result.chain(map -> resolver.get().map(value -> {
                map.put(key, value);
                resolved.add(key);
                return map;
            }));
        }
        return result.invoke(() -> storeResolved(resolved));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Supplier<Uni<Object>>> resolvers() {
        var stored = InertiaContextLocals.get(Vertx.currentContext(), CONTEXT_LIVE_RESOLVERS);
        if (stored instanceof Map<?, ?> map) {
            return (Map<String, Supplier<Uni<Object>>>) map;
        }
        return new LinkedHashMap<>();
    }

    private static Set<String> resolvedKeys() {
        var stored = InertiaContextLocals.get(Vertx.currentContext(), CONTEXT_LIVE_RESOLVED);
        if (stored instanceof Set<?> set) {
            @SuppressWarnings("unchecked")
            var typed = (Set<String>) set;
            return typed;
        }
        return new LinkedHashSet<>();
    }

    private static void storeResolved(Set<String> resolved) {
        InertiaContextLocals.put(Vertx.currentContext(), CONTEXT_LIVE_RESOLVED, resolved);
    }
}
