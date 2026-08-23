package io.github.dg.quarkus.inertia.protocol;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

import io.github.dg.quarkus.inertia.model.OnceProp;

import io.smallrye.mutiny.Uni;

/**
 * Application-scoped registry of once props: props that are sent to the client
 * only once (e.g. flash notifications). Resolved values are drained into
 * the page props, and metadata of fresh entries informs the client's
 * history tracking.
 *
 * <p>In Inertia v3, the client sends {@code X-Inertia-Except-Once-Props}
 * header with the tracking keys of once-props it has already seen. The server
 * omits the values for those keys but still sends the metadata so the client
 * can track them. This implementation uses the client header instead of
 * server-side session state.</p>
 * Per-request state is stored in the Vert.x {@link RoutingContext}.
 */
@ApplicationScoped
public class OncePropRegistry {

    static final String SESSION_PREFIX = "__inertia_once:";
    private static final String ROUTING_CONTEXT_KEY = "inertia-once-props";

    @Inject
    io.quarkus.vertx.http.runtime.CurrentVertxRequest currentVertxRequest;

    @Inject
    Instance<RoutingContext> routingContext;

    // Fallback for testing/non-request contexts
    private final OnceEntryMap fallback = new OnceEntryMap();

    private OnceEntryMap getCurrent() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var rc = ctx.getLocal("inertia-routing-context");
            if (rc instanceof RoutingContext routingContext) {
                var data = (OnceEntryMap) routingContext.get(ROUTING_CONTEXT_KEY);
                if (data == null) {
                    data = new OnceEntryMap();
                    routingContext.put(ROUTING_CONTEXT_KEY, data);
                }
                return data;
            }
        }
        try {
            var routingContext = currentVertxRequest.getCurrent();
            if (routingContext != null) {
                var data = (OnceEntryMap) routingContext.get(ROUTING_CONTEXT_KEY);
                if (data == null) {
                    data = new OnceEntryMap();
                    routingContext.put(ROUTING_CONTEXT_KEY, data);
                }
                return data;
            }
        } catch (Exception ignored) {
        }
        // Fallback to CDI-injected RoutingContext
        try {
            var rc = routingContext.get();
            if (rc != null) {
                var data = (OnceEntryMap) rc.get(ROUTING_CONTEXT_KEY);
                if (data == null) {
                    data = new OnceEntryMap();
                    rc.put(ROUTING_CONTEXT_KEY, data);
                }
                return data;
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private static class OnceEntryMap {
        final Map<String, OnceEntry> onceProps = new HashMap<>();

        void clear() {
            onceProps.clear();
        }
    }

    public void set(String key, Object value) {
        set(key, value, null, null);
    }

    public void set(String key, Object value, String customKey, Instant expiresAt) {
        var current = getCurrent();
        var onceKey = customKey != null ? customKey : key;
        current.onceProps.put(key, new OnceEntry(value, customKey, expiresAt));
    }

    public void setLazy(String key, Supplier<Uni<Object>> resolver) {
        setLazy(key, resolver, null, null);
    }

    public void setLazy(String key, Supplier<Uni<Object>> resolver, String customKey) {
        setLazy(key, resolver, customKey, null);
    }

    public void setLazy(String key, Supplier<Uni<Object>> resolver, String customKey, Instant expiresAt) {
        var current = getCurrent();
        var onceKey = customKey != null ? customKey : key;
        current.onceProps.put(key, new OnceEntry(wrapDelivered(onceKey, expiresAt, resolver), customKey, expiresAt));
    }

    private Supplier<Uni<Object>> wrapDelivered(String onceKey, Instant expiresAt, Supplier<Uni<Object>> resolver) {
        return () -> resolver.get().map(value -> value);
    }

    public Set<String> propKeys(Set<String> onceKeys) {
        var current = getCurrent();
        if (onceKeys == null || onceKeys.isEmpty()) return Set.of();
        var keys = new HashSet<String>();
        for (var entry : current.onceProps.entrySet()) {
            var value = entry.getValue();
            var onceKey = value.customKey() != null ? value.customKey() : entry.getKey();
            if (onceKeys.contains(onceKey)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    public boolean hasProps() {
        var current = getCurrent();
        purgeExpired(current);
        return !current.onceProps.isEmpty();
    }

    public Map<String, Object> drain() {
        return drain(Set.of());
    }

    public Map<String, Object> drain(Set<String> exceptKeys) {
        var current = getCurrent();
        purgeExpired(current);
        var snapshot = new HashMap<String, Object>();
        for (var entry : current.onceProps.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            var onceKey = value.customKey() != null ? value.customKey() : key;
            if (!exceptKeys.isEmpty() && exceptKeys.contains(onceKey)) {
                continue;
            }
            snapshot.put(key, value.value());
        }
        current.onceProps.clear();
        return Map.copyOf(snapshot);
    }

    public Map<String, OnceProp> metadata() {
        var current = getCurrent();
        purgeExpired(current);
        var snapshot = new HashMap<String, OnceProp>();
        for (var entry : current.onceProps.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            var onceKey = value.customKey() != null ? value.customKey() : key;
            long expiresAt = value.expiresAt() != null ? value.expiresAt().toEpochMilli() : 0L;
            snapshot.put(onceKey, new OnceProp(key, expiresAt == 0L ? null : expiresAt));
        }
        return Map.copyOf(snapshot);
    }

    public boolean isFresh(String key) {
        var current = getCurrent();
        var value = current.onceProps.get(key);
        return value != null && value.fresh();
    }

    public void markFresh(String key) {
        var current = getCurrent();
        var value = current.onceProps.get(key);
        if (value != null) {
            current.onceProps.put(key, new OnceEntry(value.value(), value.customKey(), value.expiresAt(), true));
        }
    }

    public Set<String> resolvedKeys() {
        var current = getCurrent();
        var keys = new HashSet<String>();
        for (var entry : current.onceProps.entrySet()) {
            var value = entry.getValue();
            keys.add(value.customKey() != null ? value.customKey() : entry.getKey());
        }
        return keys;
    }

    /**
     * Get the set of once-prop tracking keys that the client has already seen,
     * based on the {@code X-Inertia-Except-Once-Props} header.
     *
     * @return the set of tracking keys the client has seen
     */
    public Set<String> exceptOnceKeys() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var raw = (String) ctx.getLocal("inertia-except-once-props");
            if (raw != null && !raw.isBlank()) {
                var keys = new HashSet<String>();
                for (var part : raw.split(",")) {
                    var trimmed = part.trim();
                    if (!trimmed.isEmpty()) keys.add(trimmed);
                }
                return keys;
            }
        }
        return Set.of();
    }

    private Session getSession() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var rc = ctx.getLocal("inertia-routing-context");
            if (rc instanceof RoutingContext routingContext) {
                return routingContext.session();
            }
        }
        try {
            var routingContext = currentVertxRequest.getCurrent();
            if (routingContext != null) {
                return routingContext.session();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void purgeExpired(OnceEntryMap current) {
        var now = Instant.now();
        current.onceProps.values().removeIf(e -> e.expiresAt() != null && e.expiresAt().isBefore(now));
    }

    private record OnceEntry(Object value, String customKey, Instant expiresAt, boolean fresh) {
        OnceEntry(Object value, String customKey, Instant expiresAt) {
            this(value, customKey, expiresAt, false);
        }

        OnceEntry(Supplier<Uni<Object>> resolver, String customKey, Instant expiresAt) {
            this(resolver, customKey, expiresAt, false);
        }
    }
}