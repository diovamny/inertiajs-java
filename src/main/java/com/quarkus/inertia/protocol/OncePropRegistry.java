package com.quarkus.inertia.protocol;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class OncePropRegistry {

    private final Map<String, OnceEntry> onceProps = new HashMap<>();

    public void set(String key, Object value) {
        set(key, value, null, null);
    }

    public void set(String key, Object value, String customKey, Instant expiresAt) {
        onceProps.put(key, new OnceEntry(value, customKey, expiresAt));
    }

    public Map<String, Object> drain() {
        purgeExpired();
        var snapshot = new HashMap<String, Object>();
        for (var entry : onceProps.entrySet()) {
            var onceKey = entry.getValue().customKey != null
                ? entry.getValue().customKey : entry.getKey();
            snapshot.put(onceKey, entry.getValue().value);
        }
        onceProps.clear();
        return Map.copyOf(snapshot);
    }

    public boolean hasProps() {
        purgeExpired();
        return !onceProps.isEmpty();
    }

    private void purgeExpired() {
        var now = Instant.now();
        onceProps.values().removeIf(e -> e.expiresAt != null && e.expiresAt.isBefore(now));
    }

    private record OnceEntry(Object value, String customKey, Instant expiresAt) {}
}
