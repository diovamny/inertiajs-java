package com.quarkus.inertia.cache;

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

    public void flush() {
        cache.clear();
    }

    public void evict(String key) {
        cache.remove(key);
    }

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