package io.github.diovamny.spring.inertia.protocol;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import io.github.diovamny.inertia.core.model.LivePropListener;
import io.github.diovamny.inertia.core.model.LivePropMetadata;
import io.github.diovamny.inertia.core.protocol.PartialFilter;
import io.github.diovamny.spring.inertia.internal.InertiaRequestContext;

/**
 * Request-scoped registry of live props.
 *
 * <p>The JSON wire model ({@link LivePropMetadata}) never contains the
 * resolver; this registry keeps the {@link Supplier} in request attributes so
 * it is discarded when the request ends. Resolution follows the partial-reload
 * contract:</p>
 *
 * <ul>
 *   <li><strong>Full visit</strong>: every non-deferred live prop is resolved
 *   and written into the props.</li>
 *   <li><strong>Partial reload</strong>: only live props selected by
 *   {@code X-Inertia-Partial-Data}/{@code X-Inertia-Partial-Except} are
 *   resolved; foreign live props keep their metadata but never run their
 *   resolver.</li>
 *   <li><strong>Deferred composition</strong> ({@code defer + live}): the
 *   deferred pipeline owns the resolver; live resolution only records the key
 *   as already resolved to avoid double evaluation.</li>
 * </ul>
 */
public final class LivePropRegistry {

    public static final String CONTEXT_LIVE_RESOLVERS = "inertia-live-resolvers";
    public static final String CONTEXT_LIVE_METADATA = "inertia-live-metadata";
    public static final String CONTEXT_LIVE_RESOLVED = "inertia-live-resolved";

    private LivePropRegistry() {
    }

    /**
     * Register a live prop for the current request.
     *
     * @param key        the prop name
     * @param resolver   produces the current value
     * @param throttleMs minimum interval between updates, or {@code null}
     * @param listeners  channels and events that trigger updates
     */
    public static void register(String key, Supplier<Object> resolver, Integer throttleMs,
            LivePropListener... listeners) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("live prop key must not be blank");
        }
        if (resolver == null) {
            throw new IllegalArgumentException("live prop resolver must not be null");
        }
        var resolvers = resolvers();
        resolvers.put(key, resolver);
        InertiaRequestContext.set(CONTEXT_LIVE_RESOLVERS, resolvers);

        var metadata = metadata();
        var safeListeners = listeners != null ? List.of(listeners) : List.<LivePropListener>of();
        metadata.put(key, new LivePropMetadata(safeListeners, throttleMs));
        InertiaRequestContext.set(CONTEXT_LIVE_METADATA, metadata);
    }

    /**
     * The live props registered for the current request.
     *
     * @return the wire metadata by prop name, empty when none
     */
    public static Map<String, LivePropMetadata> metadata() {
        var stored = InertiaRequestContext.get(CONTEXT_LIVE_METADATA);
        if (stored instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            var typed = (Map<String, LivePropMetadata>) map;
            return typed;
        }
        return new LinkedHashMap<>();
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
     * Record a key as resolved so a later chained call does not run the
     * resolver a second time.
     *
     * @param key the prop name
     */
    public static void markResolved(String key) {
        var resolved = resolvedKeys();
        resolved.add(key);
        InertiaRequestContext.set(CONTEXT_LIVE_RESOLVED, resolved);
    }

    /**
     * Whether the given key was registered as a deferred prop.
     *
     * @param key the prop name
     * @return {@code true} when the deferred pipeline owns its resolution
     */
    public static boolean isDeferred(String key) {
        var stored = InertiaRequestContext.get(PageObjectBuilder.CONTEXT_DEFERRED);
        return stored instanceof Map<?, ?> map && map.containsKey(key);
    }

    /**
     * Resolve the live props for the current visit and return their wire
     * metadata.
     *
     * <p>Selection honours the partial-reload headers for {@code component};
     * non-selected resolvers are never invoked.</p>
     *
     * @param props     the page props, mutated with resolved live values
     * @param component the page component rendered by this request
     * @return the live-prop metadata ({@code key -> metadata}) for the wire
     */
    public static Map<String, LivePropMetadata> resolveInto(Map<String, Object> props, String component) {
        var resolvers = resolvers();
        var metadata = metadata();
        if (resolvers.isEmpty()) {
            return Map.of();
        }

        var partialComponent = stringAttr(InertiaHeaderExtractor.CONTEXT_PARTIAL_COMPONENT);
        var partial = PartialFilter.matchesComponent(partialComponent, component);
        var only = PartialFilter.parseCsv(stringAttr(InertiaHeaderExtractor.CONTEXT_PARTIAL_DATA));
        var except = PartialFilter.parseCsv(stringAttr(InertiaHeaderExtractor.CONTEXT_PARTIAL_EXCEPT));
        var resolved = resolvedKeys();

        for (var entry : resolvers.entrySet()) {
            var key = entry.getKey();
            if (resolved.contains(key)) {
                continue;
            }
            if (isDeferred(key)) {
                if (props.containsKey(key)) {
                    resolved.add(key);
                }
                continue;
            }
            if (!partial || isSelected(key, only, except)) {
                props.put(key, entry.getValue().get());
                resolved.add(key);
            }
        }
        InertiaRequestContext.set(CONTEXT_LIVE_RESOLVED, resolved);
        return new LinkedHashMap<>(metadata);
    }

    private static boolean isSelected(String key, Set<String> only, Set<String> except) {
        if (PartialFilter.isExcluded(key, except)) {
            return false;
        }
        return only.isEmpty() || PartialFilter.isSelected(key, only);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Supplier<Object>> resolvers() {
        var stored = InertiaRequestContext.get(CONTEXT_LIVE_RESOLVERS);
        if (stored instanceof Map<?, ?> map) {
            return (Map<String, Supplier<Object>>) map;
        }
        return new LinkedHashMap<>();
    }

    private static Set<String> resolvedKeys() {
        var stored = InertiaRequestContext.get(CONTEXT_LIVE_RESOLVED);
        if (stored instanceof Set<?> set) {
            @SuppressWarnings("unchecked")
            var typed = (Set<String>) set;
            return typed;
        }
        return new LinkedHashSet<>();
    }

    private static String stringAttr(String name) {
        var value = InertiaRequestContext.get(name);
        return value != null ? String.valueOf(value) : null;
    }
}
