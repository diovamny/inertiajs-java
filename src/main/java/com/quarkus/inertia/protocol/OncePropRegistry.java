package com.quarkus.inertia.protocol;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import io.vertx.ext.web.RoutingContext;

import com.quarkus.inertia.model.OnceProp;

/**
 * Request-scoped registry of once props: props that are sent to the client
 * only once (e.g. flash notifications). Resolved values are drained into
 * the page props, and metadata of fresh entries informs the client's
 * history tracking.
 *
 * <p>When a Vert.x session is available, delivered once props are remembered
 * in the session (under {@value #SESSION_PREFIX}): a later request that
 * registers the same once prop does not resolve it again, so the client
 * keeps the value from its history — mirroring Laravel's
 * {@code Inertia::once()} semantics.</p>
 */
@RequestScoped
public class OncePropRegistry {

    static final String SESSION_PREFIX = "__inertia_once:";

    @Inject
    Instance<RoutingContext> routingContext;

    private final Map<String, OnceEntry> onceProps = new HashMap<>();

    public void set(String key, Object value) {
        set(key, value, null, null);
    }

    public void set(String key, Object value, String customKey, Instant expiresAt) {
        var onceKey = customKey != null ? customKey : key;
        if (alreadyDelivered(onceKey, expiresAt)) return;
        onceProps.put(key, new OnceEntry(value, customKey, expiresAt));
        markDelivered(onceKey, expiresAt);
    }

    public void setLazy(String key, Supplier<io.smallrye.mutiny.Uni<Object>> resolver) {
        setLazy(key, resolver, null, null);
    }

    public void setLazy(String key, Supplier<io.smallrye.mutiny.Uni<Object>> resolver,
            String customKey) {
        setLazy(key, resolver, customKey, null);
    }

    public void setLazy(String key, Supplier<io.smallrye.mutiny.Uni<Object>> resolver,
            String customKey, Instant expiresAt) {
        var onceKey = customKey != null ? customKey : key;
        if (alreadyDelivered(onceKey, expiresAt)) return;
        onceProps.put(key, new OnceEntry(wrapDelivered(onceKey, expiresAt, resolver), customKey, expiresAt));
        markDelivered(onceKey, expiresAt);
    }

    private Supplier<io.smallrye.mutiny.Uni<Object>> wrapDelivered(String onceKey,
            Instant expiresAt, Supplier<io.smallrye.mutiny.Uni<Object>> resolver) {
        return () -> resolver.get().map(value -> {
            markDelivered(onceKey, expiresAt);
            return value;
        });
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

    private io.vertx.ext.web.Session getSession() {
        if (routingContext == null) return null;
        try {
            var rc = routingContext.get();
            return rc != null ? rc.session() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean alreadyDelivered(String onceKey, Instant expiresAt) {
        var session = getSession();
        if (session == null) return false;
        Object raw = session.get(SESSION_PREFIX + onceKey);
        if (!(raw instanceof Map<?, ?> stored)) return false;
        Object expires = stored.get("expiresAt");
        if (expires instanceof Number n && n.longValue() > 0L) {
            var expiry = Instant.ofEpochMilli(n.longValue());
            if (expiry.isBefore(Instant.now())) {
                session.remove(SESSION_PREFIX + onceKey);
                return false;
            }
        }
        if (expires == null) return false;
        return true;
    }

    private void markDelivered(String onceKey, Instant expiresAt) {
        var session = getSession();
        if (session == null) return;
        var stored = new HashMap<String, Object>();
        stored.put("expiresAt", expiresAt != null ? expiresAt.toEpochMilli() : 0L);
        session.put(SESSION_PREFIX + onceKey, stored);
    }

    private record OnceEntry(Object value, String customKey, Instant expiresAt, boolean fresh) {
        OnceEntry(Object value, String customKey, Instant expiresAt) {
            this(value, customKey, expiresAt, false);
        }
    }
}