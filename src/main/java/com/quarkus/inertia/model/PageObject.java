package com.quarkus.inertia.model;

import java.util.List;
import java.util.Map;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PageObject(
    String component,
    Map<String, Object> props,
    String url,
    String version,
    Map<String, List<String>> deferredProps,
    List<String> mergeProps,
    List<String> prependProps,
    List<String> deepMergeProps,
    List<String> matchPropsOn,
    Map<String, String> onceProps,
    Map<String, Map<String, Object>> scrollProps,
    List<String> sharedProps,
    Map<String, Object> rescuedProps,
    Map<String, Object> meta,
    boolean encryptHistory,
    boolean clearHistory,
    boolean preserveFragment
) {
    public PageObject {
        if (component == null || component.isBlank()) {
            throw new IllegalArgumentException("component must not be blank");
        }
        if (props == null) props = Map.of();
    }

    public PageObject(
            String component,
            Map<String, Object> props,
            String url,
            String version) {
        this(component, props, url, version,
            null, null, null, null, null, null, null, null, null, null,
            false, false, false);
    }

    public PageObject withProps(Map<String, Object> newProps) {
        return new PageObject(component, newProps, url, version,
            deferredProps, mergeProps, prependProps, deepMergeProps, matchPropsOn, onceProps,
            scrollProps, sharedProps, rescuedProps, meta,
            encryptHistory, clearHistory, preserveFragment);
    }

    public PageObject withVersion(String newVersion) {
        return new PageObject(component, props, url, newVersion,
            deferredProps, mergeProps, prependProps, deepMergeProps, matchPropsOn, onceProps,
            scrollProps, sharedProps, rescuedProps, meta,
            encryptHistory, clearHistory, preserveFragment);
    }

    public PageObject withUrl(String newUrl) {
        return new PageObject(component, props, newUrl, version,
            deferredProps, mergeProps, prependProps, deepMergeProps, matchPropsOn, onceProps,
            scrollProps, sharedProps, rescuedProps, meta,
            encryptHistory, clearHistory, preserveFragment);
    }

    public boolean hasMetadata() {
        return (deferredProps != null && !deferredProps.isEmpty()) ||
               (mergeProps != null && !mergeProps.isEmpty()) ||
               (prependProps != null && !prependProps.isEmpty()) ||
               (deepMergeProps != null && !deepMergeProps.isEmpty()) ||
               (matchPropsOn != null && !matchPropsOn.isEmpty()) ||
               (onceProps != null && !onceProps.isEmpty()) ||
               (scrollProps != null && !scrollProps.isEmpty()) ||
               (sharedProps != null && !sharedProps.isEmpty()) ||
               (rescuedProps != null && !rescuedProps.isEmpty()) ||
               (meta != null && !meta.isEmpty()) ||
               encryptHistory || clearHistory || preserveFragment;
    }

    public boolean hasDeferredProps() {
        return deferredProps != null && !deferredProps.isEmpty();
    }
}
