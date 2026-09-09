package io.github.diovamny.spring.inertia.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Server-side cache store for expensive prop computations. When the cache is
 * warm, the resolver block is never evaluated. Mirrors the "cached props"
 * feature of inertia-rails.
 */
public class CachedPropStore {

    private final Map<String, Entry> cache = new ConcurrentHashMap<>();

    /**
     * Resolve a value, reusing the cached one when the key is present and not
     * expired.
     *
     * @param key      the cache key
     * @param ttl      time-to-live; {@code null} never expires
     * @param resolver lazily evaluated on cache miss
     * @return the cached or freshly computed value
     */
    public Object compute(String key, Duration ttl, Supplier<Object> resolver) {
        var now = Instant.now();
        var existing = cache.get(key);
        if (existing != null && !existing.expired(now)) {
            return existing.value();
        }
        try {
            var value = resolver.get();
            cache.put(key, new Entry(value, ttl != null ? now.plus(ttl) : null));
            return value;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Remove all cached values.
     */
    public void flush() {
        cache.clear();
    }

    /**
     * Remove the entry of a single key.
     *
     * @param key the cache key
     */
    public void evict(String key) {
        cache.remove(key);
    }

    /**
     * Whether the key currently holds a non-expired value.
     *
     * @param key the cache key
     * @return {@code true} when present and not expired
     */
    public boolean contains(String key) {
        var entry = cache.get(key);
        return entry != null && !entry.expired(Instant.now());
    }

    private record Entry(Object value, Instant expiresAt) {
        boolean expired(Instant now) {
            return expiresAt != null && expiresAt.isBefore(now);
        }
    }
}
