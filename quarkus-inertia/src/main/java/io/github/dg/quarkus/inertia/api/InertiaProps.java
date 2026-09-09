package io.github.dg.quarkus.inertia.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;
import io.smallrye.mutiny.Uni;
import io.github.dg.quarkus.inertia.model.RawJson;

/**
 * Specialized interface for property management in Quarkus Inertia.js responses.
 *
 * <p>Encompasses shared data, always-props, deferred props, once-props,
 * merge/prepend props, scroll props, and server-side cached computations.</p>
 */
public interface InertiaProps {

    // ---------------------------------------------------------------------
    // Shared Props
    // ---------------------------------------------------------------------

    /**
     * Share a value with every page rendered in the current request.
     *
     * <p>Shared props are merged into every {@code render()} call and
     * typically carry global data such as the authenticated user, flash
     * notifications or CSRF tokens. They are removed by
     * {@link #flushShared()}.</p>
     *
     * @param key   the prop name
     * @param value the prop value
     */
    void share(String key, Object value);

    /**
     * Share several values at once.
     *
     * @param values the props to share
     */
    void share(Map<String, Object> values);

    /**
     * Share all properties from a {@link ProvidesInertiaProperties} instance
     * across all pages rendered in the current request.
     *
     * @param provider the provider whose inertia properties will be shared
     */
    void share(ProvidesInertiaProperties provider);

    /**
     * Expose all public getters of the given instance as page props when
     * {@code render(String)} is called without explicit props.
     *
     * @param instance the bean whose getters become props
     */
    void shareInstanceProps(Object instance);

    /**
     * Share a prop that is always included in every response of the current
     * request, including partial reloads and after {@link #flushShared()}.
     *
     * @param key   the prop name
     * @param value the prop value
     */
    void always(String key, Object value);

    /**
     * Wrap a raw JSON string as a prop value that is embedded verbatim in the
     * page object, without being re-serialized.
     *
     * @param json the raw JSON document
     * @return a wrapper usable as a prop value
     */
    RawJson rawJson(String json);

    /**
     * Return all currently shared props.
     *
     * @return an unmodifiable map of the shared props
     */
    Map<String, Object> getShared();

    /**
     * Return a single shared prop, or a default value.
     *
     * @param key          the prop name
     * @param defaultValue returned when the prop is not shared
     * @return the shared value or {@code defaultValue}
     */
    Object getShared(String key, Object defaultValue);

    /**
     * Remove all currently shared props.
     *
     * <p>Props registered with {@link #always(String, Object)} survive the
     * flush; props registered with {@code share(...)} are removed.</p>
     */
    void flushShared();

    // ---------------------------------------------------------------------
    // Deferred / Optional / Cached Props
    // ---------------------------------------------------------------------

    /**
     * Register a prop that is resolved asynchronously during a partial reload.
     *
     * @param group    the deferred group name (e.g. {@code "default"})
     * @param name     the prop name
     * @param resolver a supplier resolving the prop value
     */
    void deferred(String group, String name, Supplier<Uni<Object>> resolver);

    /**
     * Register a deferred prop in the {@code "default"} group.
     *
     * @param name     the prop name
     * @param resolver a supplier resolving the prop value
     */
    void deferred(String name, Supplier<Uni<Object>> resolver);

    /**
     * Register a deferred prop whose resolved value is cached.
     *
     * @param group    the deferred group name
     * @param name     the prop name
     * @param resolver a supplier resolving the prop value
     * @param cacheKey the cache key
     */
    void deferred(String group, String name, Supplier<Uni<Object>> resolver, String cacheKey);

    /**
     * Register a deferred prop whose resolved value is cached for a TTL.
     *
     * @param group    the deferred group name
     * @param name     the prop name
     * @param resolver a supplier resolving the prop value
     * @param cacheKey the cache key
     * @param cacheTtl how long the cached value is considered fresh
     */
    void deferred(String group, String name, Supplier<Uni<Object>> resolver, String cacheKey, Duration cacheTtl);

    /**
     * Register a prop that is only included on partial reloads when the client explicitly requests it.
     *
     * @param key      the prop name
     * @param resolver a supplier resolving the prop value
     */
    void optional(String key, Supplier<Uni<Object>> resolver);

    /**
     * Register an optional prop with a cached resolver.
     *
     * @param key      the prop name
     * @param resolver a supplier resolving the prop value
     * @param cacheKey the cache key
     */
    void optional(String key, Supplier<Uni<Object>> resolver, String cacheKey);

    /**
     * Register an optional prop with a cached resolver and TTL.
     *
     * @param key      the prop name
     * @param resolver a supplier resolving the prop value
     * @param cacheKey the cache key
     * @param cacheTtl how long the cached value is considered fresh
     */
    void optional(String key, Supplier<Uni<Object>> resolver, String cacheKey, Duration cacheTtl);

    /**
     * Resolve a prop value through a server cache, without sharing semantics.
     *
     * @param key      the prop name
     * @param resolver a supplier resolving the prop value
     */
    void cache(String key, Supplier<Uni<Object>> resolver);

    /**
     * Resolve a prop value through a server cache with an explicit TTL.
     *
     * @param key      the prop name
     * @param ttl      how long the cached value is considered fresh
     * @param resolver a supplier resolving the prop value
     */
    void cache(String key, Duration ttl, Supplier<Uni<Object>> resolver);

    // ---------------------------------------------------------------------
    // Once Props
    // ---------------------------------------------------------------------

    /**
     * Include a prop in the page object only if the client does not already have it.
     *
     * @param key   the prop name
     * @param value the prop value
     */
    void once(String key, Object value);

    /**
     * Include a once prop under a client-tracking key that differs from the prop name.
     *
     * @param key       the prop name
     * @param value     the prop value
     * @param customKey the key used to track the prop client-side
     */
    void once(String key, Object value, String customKey);

    /**
     * Register a lazily resolved once prop.
     *
     * @param key      the prop name
     * @param resolver a supplier resolving the prop value
     */
    void once(String key, Supplier<Uni<Object>> resolver);

    /**
     * Register a lazily resolved once prop with a custom tracking key.
     *
     * @param key       the prop name
     * @param resolver  a supplier resolving the prop value
     * @param customKey the key used to track the prop client-side
     */
    void once(String key, Supplier<Uni<Object>> resolver, String customKey);

    /**
     * Register a lazily resolved once prop that expires at a given instant.
     *
     * @param key       the prop name
     * @param resolver  a supplier resolving the prop value
     * @param customKey the key used to track the prop client-side
     * @param expiresAt the instant after which the client no longer reports the prop as present
     */
    void once(String key, Supplier<Uni<Object>> resolver, String customKey, Instant expiresAt);

    /**
     * Share a value and also register it as a once prop.
     *
     * @param key   the prop name
     * @param value the prop value
     */
    void shareOnce(String key, Object value);

    /**
     * Share a lazily resolved value and register it as a once prop.
     *
     * @param key      the prop name
     * @param resolver a supplier resolving the prop value
     */
    void shareOnce(String key, Supplier<Uni<Object>> resolver);

    // ---------------------------------------------------------------------
    // Merge / Prepend Props
    // ---------------------------------------------------------------------

    /**
     * Mark a prop as mergeable and set its value.
     *
     * @param key   the prop name
     * @param value the prop value
     */
    void merge(String key, Object value);

    /**
     * Mark a prop as mergeable, optionally with deep merge semantics.
     *
     * @param key   the prop name
     * @param value the prop value
     * @param deep  whether nested objects are merged recursively
     */
    void merge(String key, Object value, boolean deep);

    /**
     * Mark a prop as mergeable with deep merge semantics and list-based matching.
     *
     * @param key     the prop name
     * @param value   the prop value
     * @param deep    whether nested objects are merged recursively
     * @param matchOn field paths used to match elements of arrays/lists
     */
    void merge(String key, Object value, boolean deep, String... matchOn);

    /**
     * Mark a prop as prepended on partial reloads and set its value.
     *
     * @param key   the prop name
     * @param value the prop value
     */
    void prepend(String key, Object value);

    // ---------------------------------------------------------------------
    // Scroll Props
    // ---------------------------------------------------------------------

    /**
     * Register a prop as a "scroll" prop with only metadata.
     *
     * @param key      the prop name
     * @param metadata the scroll metadata
     */
    void scroll(String key, Map<String, Object> metadata);

    /**
     * Register a scroll prop with a value and metadata.
     *
     * @param key      the prop name
     * @param value    the prop value
     * @param metadata the scroll metadata
     */
    void scroll(String key, Object value, Map<String, Object> metadata);

    /**
     * Register a scroll prop with a value, metadata and an explicit wrapper element name.
     *
     * @param key      the prop name
     * @param value    the prop value
     * @param metadata the scroll metadata
     * @param wrapper  the element (default {@code "data"}) that wraps the prop
     */
    void scroll(String key, Object value, Map<String, Object> metadata, String wrapper);
}
