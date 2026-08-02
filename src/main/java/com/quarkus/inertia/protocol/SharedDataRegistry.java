package com.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import jakarta.enterprise.context.RequestScoped;

import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.model.AlwaysProp;

@RequestScoped
public class SharedDataRegistry {

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
    private final Map<String, Map<String, Object>> scrollProps = new HashMap<>();
    private final Map<String, Object> meta = new HashMap<>();
    private final List<String> rescuedProps = new java.util.ArrayList<>();

    public void set(String key, Object value) {
        data.put(key, value);
        sharedKeys.add(key);
    }

    public void setAll(Map<String, Object> values) {
        data.putAll(values);
        sharedKeys.addAll(values.keySet());
    }

    public void setWithNoTrack(String key, Object value) {
        data.put(key, value);
    }

    public void setFlash(String key, Object value) {
        flashData.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public Map<String, Object> getAll() {
        var result = new HashMap<>(data);
        result.putAll(flashData);
        return Map.copyOf(result);
    }

    public Map<String, Object> drainFlash() {
        var snapshot = Map.copyOf(flashData);
        flashData.clear();
        return snapshot;
    }

    public boolean hasFlash() {
        return !flashData.isEmpty();
    }

    public boolean isEmpty() {
        return data.isEmpty() && flashData.isEmpty();
    }

    public void clear() {
        data.clear();
        flashData.clear();
        deferredPropGroups.clear();
        oncePropKeys.clear();
        optionalProps.clear();
        mergePropKeys.clear();
        prependPropKeys.clear();
        deepMergePropKeys.clear();
        matchPropKeys.clear();
        sharedKeys.clear();
        scrollProps.clear();
        meta.clear();
        rescuedProps.clear();
    }

    public void addDeferredPropGroup(String group, List<String> keys) {
        deferredPropGroups.put(group, List.copyOf(keys));
    }

    public Map<String, List<String>> getDeferredPropGroups() {
        return Map.copyOf(deferredPropGroups);
    }

    public void addOptionalProp(String key, Supplier<Uni<Object>> resolver) {
        optionalProps.put(key, resolver);
    }

    public Map<String, Supplier<Uni<Object>>> getOptionalProps() {
        return Map.copyOf(optionalProps);
    }

    public boolean hasOptionalProps() {
        return !optionalProps.isEmpty();
    }

    public void addOncePropKey(String key, String customKey) {
        oncePropKeys.put(key, customKey);
    }

    public Map<String, String> getOncePropKeys() {
        return Map.copyOf(oncePropKeys);
    }

    public void addMergePropKey(String key) {
        mergePropKeys.add(key);
    }

    public void merge(String key, Object value, boolean deep, String... matchOn) {
        mergePropKeys.add(key);
        if (deep) {
            deepMergePropKeys.add(key);
        }
        for (var field : matchOn) {
            if (field != null && !field.isBlank()) {
                matchPropKeys.add(key + "." + field);
            }
        }
        set(key, value);
    }

    public List<String> getMergePropKeys() {
        return List.copyOf(mergePropKeys);
    }

    public void addPrependPropKey(String key) {
        prependPropKeys.add(key);
    }

    public List<String> getPrependPropKeys() {
        return List.copyOf(prependPropKeys);
    }

    public void addDeepMergePropKey(String key) {
        deepMergePropKeys.add(key);
    }

    public List<String> getDeepMergePropKeys() {
        return List.copyOf(deepMergePropKeys);
    }

    public void addMatchPropKey(String key) {
        matchPropKeys.add(key);
    }

    public List<String> getMatchPropKeys() {
        return List.copyOf(matchPropKeys);
    }

    public List<String> getSharedKeys() {
        return List.copyOf(sharedKeys);
    }

    public Map<String, Object> getShared() {
        var result = new HashMap<String, Object>();
        for (var key : sharedKeys) {
            if (data.containsKey(key)) {
                result.put(key, data.get(key));
            }
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    public Object getShared(String key, Object defaultValue) {
        if (sharedKeys.contains(key) && data.containsKey(key)) {
            return data.get(key);
        }
        return defaultValue;
    }

    public void flushShared() {
        for (var key : sharedKeys) {
            data.remove(key);
        }
        sharedKeys.clear();
    }

    public void addScrollProp(String key, Map<String, Object> metadata) {
        scrollProps.put(key, new HashMap<>(metadata));
    }

    public Map<String, Map<String, Object>> getScrollProps() {
        var result = new HashMap<String, Map<String, Object>>();
        for (var entry : scrollProps.entrySet()) {
            result.put(entry.getKey(), java.util.Collections.unmodifiableMap(entry.getValue()));
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    public boolean hasScrollProps() {
        return !scrollProps.isEmpty();
    }

    public void addMeta(String key, Object value) {
        meta.put(key, value);
    }

    public void addMeta(Map<String, Object> values) {
        meta.putAll(values);
    }

    public Map<String, Object> getMeta() {
        return Map.copyOf(meta);
    }

    public boolean hasMeta() {
        return !meta.isEmpty();
    }

    public void addRescuedProp(String key) {
        if (!rescuedProps.contains(key)) {
            rescuedProps.add(key);
        }
    }

    public void addRescuedProps(java.util.Collection<String> keys) {
        for (var key : keys) {
            addRescuedProp(key);
        }
    }

    public List<String> getRescuedProps() {
        return List.copyOf(rescuedProps);
    }

    public boolean hasRescuedProps() {
        return !rescuedProps.isEmpty();
    }
}
