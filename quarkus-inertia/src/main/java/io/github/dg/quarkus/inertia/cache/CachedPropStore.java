package io.github.dg.quarkus.inertia.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.enterprise.context.ApplicationScoped;
import io.smallrye.mutiny.Uni;

/**
 * Server-side cache store for expensive prop computations. When the cache is
 * warm, the resolver block is never evaluated. Mirrors the "cached props"
 * feature of inertia-rails.
 */
@ApplicationScoped
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
    public Uni<Object> compute(String key, Duration ttl, java.util.function.Supplier<Uni<Object>> resolver) {
        var now = Instant.now();
        var existing = cache.get(key);
        if (existing != null && !existing.expired(now)) {
            return Uni.createFrom().item(existing.value());
        }
        try {
            return Uni.createFrom().deferred(resolver::get)
                .invoke(value -> cache.put(key, new Entry(value, ttl != null ? now.plus(ttl) : null)));
        } catch (Exception e) {
            return Uni.createFrom().failure(e);
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