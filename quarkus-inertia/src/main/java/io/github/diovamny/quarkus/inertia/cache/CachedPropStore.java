package io.github.diovamny.quarkus.inertia.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import jakarta.enterprise.context.ApplicationScoped;
import io.smallrye.mutiny.Uni;

/**
 * Server-side cache store for expensive prop computations. When the cache is
 * warm, the resolver block is never evaluated. Mirrors the "cached props"
 * feature of inertia-rails.
 *
 * <p>Configurable maximum size and TTL prevent unbounded memory growth.
 * Statistics are exposed for observability.</p>
 */
@ApplicationScoped
public class CachedPropStore {

    private final Map<String, Entry> cache = new ConcurrentHashMap<>();
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private final AtomicLong evictions = new AtomicLong();

    // Configurable limits
    private int maxSize = 1000;
    private Duration defaultTtl = Duration.ofMinutes(10);

    /**
     * Configure the maximum cache size.
     *
     * @param maxSize maximum number of entries (default 1000, 0 = unlimited)
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = Math.max(0, maxSize);
        enforceSizeLimit();
    }

    /**
     * Configure the default TTL for entries without explicit TTL.
     *
     * @param defaultTtl the default TTL
     */
    public void setDefaultTtl(Duration defaultTtl) {
        this.defaultTtl = defaultTtl;
    }

    /**
     * Resolve a value, reusing the cached one when the key is present and not
     * expired.
     *
     * @param key      the cache key
     * @param ttl      time-to-live; {@code null} uses default TTL
     * @param resolver lazily evaluated on cache miss
     * @return the cached or freshly computed value
     */
    public Uni<Object> compute(String key, Duration ttl, java.util.function.Supplier<Uni<Object>> resolver) {
        var now = Instant.now();
        var effectiveTtl = ttl != null ? ttl : defaultTtl;
        var existing = cache.get(key);
        if (existing != null && !existing.expired(now)) {
            hits.incrementAndGet();
            return Uni.createFrom().item(existing.value());
        }
        misses.incrementAndGet();
        try {
            return Uni.createFrom().deferred(resolver::get)
                .invoke(value -> {
                    cache.put(key, new Entry(value, effectiveTtl != null ? now.plus(effectiveTtl) : null));
                    enforceSizeLimit();
                });
        } catch (Exception e) {
            return Uni.createFrom().failure(e);
        }
    }

    /**
     * Remove all cached values.
     */
    public void flush() {
        cache.clear();
        hits.set(0);
        misses.set(0);
        evictions.set(0);
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

    /**
     * Current number of entries in the cache.
     *
     * @return the cache size
     */
    public int size() {
        return cache.size();
    }

    /**
     * Cache hit count.
     *
     * @return the number of cache hits
     */
    public long getHits() {
        return hits.get();
    }

    /**
     * Cache miss count.
     *
     * @return the number of cache misses
     */
    public long getMisses() {
        return misses.get();
    }

    /**
     * Cache eviction count (due to size limit).
     *
     * @return the number of evictions
     */
    public long getEvictions() {
        return evictions.get();
    }

    /**
     * Hit rate as a percentage (0-100).
     *
     * @return the hit rate
     */
    public double getHitRate() {
        long total = hits.get() + misses.get();
        return total == 0 ? 0.0 : (100.0 * hits.get() / total);
    }

    private void enforceSizeLimit() {
        if (maxSize <= 0) return;
        while (cache.size() > maxSize) {
            // Remove oldest entry (simple FIFO eviction)
            var oldest = cache.entrySet().iterator().next();
            cache.remove(oldest.getKey());
            evictions.incrementAndGet();
        }
    }

    private record Entry(Object value, Instant expiresAt) {
        boolean expired(Instant now) {
            return expiresAt != null && expiresAt.isBefore(now);
        }
    }
}
