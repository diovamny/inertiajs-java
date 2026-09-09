package io.github.diovamny.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import io.smallrye.mutiny.Uni;
import io.github.diovamny.quarkus.inertia.api.ProvidesInertiaProperties;

/**
 * Application-scoped accumulator for everything the current page carries
 * besides the controller props: shared props (including
 * no-track/flash/always variants), deferred and optional props, once,
 * merge/prepend/deep-merge/match props, scroll props, metadata and
 * rescued props. Consumed by {@link PageObjectBuilder}.
 * Per-request state is stored in the Vert.x {@link RoutingContext}.
 */
@ApplicationScoped
public class SharedDataRegistry {

    private static final String ROUTING_CONTEXT_KEY = "inertia-shared-data";

    @Inject
    io.quarkus.vertx.http.runtime.CurrentVertxRequest currentVertxRequest;

    @Inject
    Instance<RoutingContext> routingContext;

    private SharedDataRegistry getCurrent() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var rc = ctx.getLocal("inertia-routing-context");
            if (rc instanceof RoutingContext routingContext) {
                var data = (SharedDataRegistry) routingContext.get(ROUTING_CONTEXT_KEY);
                if (data == null) {
                    data = new SharedDataRegistry();
                    routingContext.put(ROUTING_CONTEXT_KEY, data);
                }
                return data;
            }
        }
        try {
            var routingContext = currentVertxRequest.getCurrent();
            if (routingContext != null) {
                var data = (SharedDataRegistry) routingContext.get(ROUTING_CONTEXT_KEY);
                if (data == null) {
                    data = new SharedDataRegistry();
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
                var data = (SharedDataRegistry) rc.get(ROUTING_CONTEXT_KEY);
                if (data == null) {
                    data = new SharedDataRegistry();
                    rc.put(ROUTING_CONTEXT_KEY, data);
                }
                return data;
            }
        } catch (Exception ignored) {
        }
        // Fallback for testing/non-request contexts
        return this;
    }

    // Per-request state fields (only used when not in RoutingContext)
    private final Map<String, Object> data = new HashMap<>();
    private final Map<String, Object> flashData = new HashMap<>();
    private final Map<String, List<String>> deferredPropGroups = new HashMap<>();
    private final Map<String, String> oncePropKeys = new HashMap<>();
    private final Map<String, Supplier<Uni<Object>>> optionalProps = new HashMap<>();
    private final List<String> mergePropKeys = new java.util.ArrayList<>();
    private final List<String> prependPropKeys = new java.util.ArrayList<>();
    private final List<String> deepMergePropKeys = new java.util.ArrayList<>();
    private final List<String> matchPropKeys = new java.util.ArrayList<>();
    private final Set<String> sharedKeys = new LinkedHashSet<>();
    private final List<ProvidesInertiaProperties> sharedProviders = new java.util.ArrayList<>();
    private final Map<String, ScrollSpec> scrollProps = new HashMap<>();
    private final Map<String, Object> meta = new HashMap<>();
    private final List<String> rescuedProps = new java.util.ArrayList<>();
    private final List<String> actuallyRescuedProps = new java.util.ArrayList<>();

    /**
     * Specification of a scroll prop: its value, the DOM element the client
     * should scroll to, and additional metadata.
     *
     * @param value    the scroll value
     * @param wrapper  the DOM selector of the scroll container
     * @param metadata extra scroll metadata
     */
    public record ScrollSpec(Object value, String wrapper, Map<String, Object> metadata) {

        public static ScrollSpec metadataOnly(Map<String, Object> metadata) {
            return new ScrollSpec(null, null, new HashMap<>(metadata));
        }
    }

    public void set(String key, Object value) {
        getCurrent().data.put(key, value);
        getCurrent().sharedKeys.add(key);
    }

    public void setAll(Map<String, Object> values) {
        var current = getCurrent();
        current.data.putAll(values);
        current.sharedKeys.addAll(values.keySet());
    }

    public void setWithNoTrack(String key, Object value) {
        getCurrent().data.put(key, value);
    }

    public void setFlash(String key, Object value) {
        getCurrent().flashData.put(key, value);
    }

    public Object get(String key) {
        return getCurrent().data.get(key);
    }

    public Map<String, Object> getAll() {
        var current = getCurrent();
        var result = new HashMap<>(current.data);
        result.putAll(current.flashData);
        return Map.copyOf(result);
    }

    public Map<String, Object> drainFlash() {
        var current = getCurrent();
        var snapshot = Map.copyOf(current.flashData);
        current.flashData.clear();
        return snapshot;
    }

    public boolean hasFlash() {
        return !getCurrent().flashData.isEmpty();
    }

    public boolean isEmpty() {
        var current = getCurrent();
        return current.data.isEmpty() && current.flashData.isEmpty() && current.sharedProviders.isEmpty();
    }

    public void clear() {
        var current = getCurrent();
        current.data.clear();
        current.flashData.clear();
        current.deferredPropGroups.clear();
        current.oncePropKeys.clear();
        current.optionalProps.clear();
        current.mergePropKeys.clear();
        current.prependPropKeys.clear();
        current.deepMergePropKeys.clear();
        current.matchPropKeys.clear();
        current.sharedKeys.clear();
        current.sharedProviders.clear();
        current.scrollProps.clear();
        current.meta.clear();
        current.rescuedProps.clear();
        current.actuallyRescuedProps.clear();
    }

    public void addSharedProvider(ProvidesInertiaProperties provider) {
        if (provider != null) {
            getCurrent().sharedProviders.add(provider);
        }
    }

    public List<ProvidesInertiaProperties> getSharedProviders() {
        return List.copyOf(getCurrent().sharedProviders);
    }

    public void addDeferredPropGroup(String group, List<String> keys) {
        getCurrent().deferredPropGroups.put(group, List.copyOf(keys));
    }

    public Map<String, List<String>> getDeferredPropGroups() {
        return Map.copyOf(getCurrent().deferredPropGroups);
    }

    public void addOptionalProp(String key, Supplier<Uni<Object>> resolver) {
        getCurrent().optionalProps.put(key, resolver);
    }

    public Map<String, Supplier<Uni<Object>>> getOptionalProps() {
        return Map.copyOf(getCurrent().optionalProps);
    }

    public boolean hasOptionalProps() {
        return !getCurrent().optionalProps.isEmpty();
    }

    public void addOncePropKey(String key, String customKey) {
        getCurrent().oncePropKeys.put(key, customKey);
    }

    public Map<String, String> getOncePropKeys() {
        return Map.copyOf(getCurrent().oncePropKeys);
    }

    public void addMergePropKey(String key) {
        getCurrent().mergePropKeys.add(key);
    }

    public void merge(String key, Object value, boolean deep, String... matchOn) {
        var current = getCurrent();
        current.mergePropKeys.add(key);
        if (deep) {
            current.deepMergePropKeys.add(key);
        }
        for (var field : matchOn) {
            if (field != null && !field.isBlank()) {
                current.matchPropKeys.add(key + "." + field);
            }
        }
        current.set(key, value);
    }

    public List<String> getMergePropKeys() {
        return List.copyOf(getCurrent().mergePropKeys);
    }

    public void addPrependPropKey(String key) {
        getCurrent().prependPropKeys.add(key);
    }

    public List<String> getPrependPropKeys() {
        return List.copyOf(getCurrent().prependPropKeys);
    }

    public void addDeepMergePropKey(String key) {
        getCurrent().deepMergePropKeys.add(key);
    }

    public List<String> getDeepMergePropKeys() {
        return List.copyOf(getCurrent().deepMergePropKeys);
    }

    public void addMatchPropKey(String key) {
        getCurrent().matchPropKeys.add(key);
    }

    public List<String> getMatchPropKeys() {
        return List.copyOf(getCurrent().matchPropKeys);
    }

    public List<String> getSharedKeys() {
        return List.copyOf(getCurrent().sharedKeys);
    }

    public Map<String, Object> getShared() {
        var current = getCurrent();
        var result = new HashMap<String, Object>();
        for (var key : current.sharedKeys) {
            if (current.data.containsKey(key)) {
                result.put(key, current.data.get(key));
            }
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    public Object getShared(String key, Object defaultValue) {
        var current = getCurrent();
        if (current.sharedKeys.contains(key) && current.data.containsKey(key)) {
            return current.data.get(key);
        }
        return defaultValue;
    }

    public void flushShared() {
        var current = getCurrent();
        for (var key : current.sharedKeys) {
            current.data.remove(key);
        }
        current.sharedKeys.clear();
    }

    public void addScrollProp(String key, Map<String, Object> metadata) {
        getCurrent().scrollProps.put(key, ScrollSpec.metadataOnly(metadata));
    }

    public void addScrollProp(String key, Object value, String wrapper, Map<String, Object> metadata) {
        getCurrent().scrollProps.put(key, new ScrollSpec(value, wrapper != null ? wrapper : "data", new HashMap<>(metadata)));
    }

    public Map<String, ScrollSpec> getScrollSpecs() {
        var current = getCurrent();
        var result = new HashMap<String, ScrollSpec>();
        for (var entry : current.scrollProps.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    public Map<String, Map<String, Object>> getScrollProps() {
        var current = getCurrent();
        var result = new HashMap<String, Map<String, Object>>();
        for (var entry : current.scrollProps.entrySet()) {
            result.put(entry.getKey(), java.util.Collections.unmodifiableMap(entry.getValue().metadata()));
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    public boolean hasScrollProps() {
        return !getCurrent().scrollProps.isEmpty();
    }

    public void addMeta(String key, Object value) {
        getCurrent().meta.put(key, value);
    }

    public void addMeta(Map<String, Object> values) {
        getCurrent().meta.putAll(values);
    }

    public Map<String, Object> getMeta() {
        return Map.copyOf(getCurrent().meta);
    }

    public boolean hasMeta() {
        return !getCurrent().meta.isEmpty();
    }

    public void addRescuedProp(String key) {
        var current = getCurrent();
        if (!current.rescuedProps.contains(key)) {
            current.rescuedProps.add(key);
        }
    }

    public void addRescuedProps(java.util.Collection<String> keys) {
        var current = getCurrent();
        for (var key : keys) {
            current.addRescuedProp(key);
        }
    }

    public List<String> getRescuedProps() {
        return List.copyOf(getCurrent().rescuedProps);
    }

    public boolean hasRescuedProps() {
        return !getCurrent().rescuedProps.isEmpty();
    }

    /**
     * Record a prop that was actually rescued during this build: it was
     * resolved, failed (or produced {@code null}) and was removed from the
     * page props. Only these keys are emitted as the page's
     * {@code rescuedProps}.
     *
     * @param key the rescued prop name
     */
    public void markActuallyRescued(String key) {
        var current = getCurrent();
        if (!current.actuallyRescuedProps.contains(key)) {
            current.actuallyRescuedProps.add(key);
        }
    }

    public List<String> getActuallyRescuedProps() {
        return List.copyOf(getCurrent().actuallyRescuedProps);
    }
}
