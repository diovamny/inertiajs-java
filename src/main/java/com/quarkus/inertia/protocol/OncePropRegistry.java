package com.quarkus.inertia.protocol;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import jakarta.enterprise.context.RequestScoped;

import com.quarkus.inertia.model.OnceProp;

@RequestScoped
public class OncePropRegistry {

    private final Map<String, OnceEntry> onceProps = new HashMap<>();

    public void set(String key, Object value) {
        set(key, value, null, null);
    }

    public void set(String key, Object value, String customKey, Instant expiresAt) {
        onceProps.put(key, new OnceEntry(value, customKey, expiresAt));
    }

    public void setLazy(String key, Supplier<io.smallrye.mutiny.Uni<Object>> resolver) {
        onceProps.put(key, new OnceEntry(resolver, null, null));
    }

    public void setLazy(String key, Supplier<io.smallrye.mutiny.Uni<Object>> resolver,
            String customKey) {
        onceProps.put(key, new OnceEntry(resolver, customKey, null));
    }

    public void setLazy(String key, Supplier<io.smallrye.mutiny.Uni<Object>> resolver,
            String customKey, Instant expiresAt) {
        onceProps.put(key, new OnceEntry(resolver, customKey, expiresAt));
    }

    public Set<String> propKeys(Set<String> onceKeys) {
        if (onceKeys == null || onceKeys.isEmpty()) return Set.of();
        purgeExpired();
        var keys = new HashSet<String>();
        for (var entry : onceProps.entrySet()) {
            var value = entry.getValue();
            var onceKey = value.customKey() != null ? value.customKey() : entry.getKey();
            if (onceKeys.contains(onceKey)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    public boolean hasProps() {
        purgeExpired();
        return !onceProps.isEmpty();
    }

    public Map<String, Object> drain() {
        return drain(Set.of());
    }

    public Map<String, Object> drain(Set<String> exceptKeys) {
        purgeExpired();
        var snapshot = new HashMap<String, Object>();
        for (var entry : onceProps.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            var onceKey = value.customKey() != null ? value.customKey() : key;
            if (!exceptKeys.isEmpty() && exceptKeys.contains(onceKey)) {
                continue;
            }
            snapshot.put(key, value.value());
        }
        onceProps.clear();
        return Map.copyOf(snapshot);
    }

    public Map<String, OnceProp> metadata() {
        purgeExpired();
        var snapshot = new HashMap<String, OnceProp>();
        for (var entry : onceProps.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            var onceKey = value.customKey() != null ? value.customKey() : key;
            long expiresAt = value.expiresAt() != null ? value.expiresAt().toEpochMilli() : 0L;
            snapshot.put(onceKey, new OnceProp(key, expiresAt == 0L ? null : expiresAt));
        }
        return Map.copyOf(snapshot);
    }

    public boolean isFresh(String key) {
        var value = onceProps.get(key);
        return value != null && value.fresh();
    }

    public void markFresh(String key) {
        var value = onceProps.get(key);
        if (value != null) {
            onceProps.put(key, new OnceEntry(value.value(), value.customKey(), value.expiresAt(), true));
        }
    }

    public Set<String> resolvedKeys() {
        purgeExpired();
        var keys = new HashSet<String>();
        for (var entry : onceProps.entrySet()) {
            var value = entry.getValue();
            keys.add(value.customKey() != null ? value.customKey() : entry.getKey());
        }
        return keys;
    }

    private void purgeExpired() {
        var now = Instant.now();
        onceProps.values().removeIf(e -> e.expiresAt() != null && e.expiresAt().isBefore(now));
    }

    private record OnceEntry(Object value, String customKey, Instant expiresAt, boolean fresh) {
        OnceEntry(Object value, String customKey, Instant expiresAt) {
            this(value, customKey, expiresAt, false);
        }
    }
}