package io.github.dg.spring.inertia.protocol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.dg.spring.inertia.internal.InertiaRequestContext;

/**
 * Implements prop merging across partial reloads: mergeable props from the
 * previous visit are merged into the current page object.
 *
 * <p>For regular (non-mergeable) props the previous values are simply
 * overwritten; mergeable props are merged, prepended or deep-merged
 * depending on their registration rule. The last request's props are kept
 * in the {@code HttpSession} between visits.</p>
 */
public class MergePropProcessor {

    /** Session attribute holding the last request's props. */
    public static final String SESSION_LAST_PROPS = "__inertia_last_props";

    /**
     * Merge the props of the previous visit into the current prop set.
     *
     * @param props      the current prop set
     * @param mergeProps keys merged with merge semantics
     * @param prependProps keys prepended with prepend semantics
     * @param deepMergeProps keys merged with deep semantics
     * @param matchPropsOn match-on fields (dot notation)
     * @return the merged map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> mergeProps(Map<String, Object> props, List<String> mergeProps,
            List<String> prependProps, List<String> deepMergeProps, List<String> matchPropsOn) {
        var last = lastProps();
        if (last == null || last.isEmpty()) {
            return props;
        }
        var result = new LinkedHashMap<>(props);
        for (var key : mergeProps) {
            var previous = last.get(key);
            if (previous == null || !props.containsKey(key)) {
                continue;
            }
            var current = props.get(key);
            if (previous instanceof List<?> prevList && current instanceof List<?> curList) {
                var merged = new LinkedHashMap<String, Object>();
                for (var item : prevList) {
                    if (item instanceof Map<?, ?> map) {
                        var match = matchKey(map, matchPropsOn);
                        merged.put(String.valueOf(match), item);
                    }
                }
                for (var item : curList) {
                    if (item instanceof Map<?, ?> map) {
                        var match = matchKey(map, matchPropsOn);
                        merged.put(String.valueOf(match), item);
                    }
                }
                result.put(key, merged.values().stream().toList());
            } else if (previous instanceof Map<?, ?> prevMap && current instanceof Map<?, ?> curMap) {
                var merged = new LinkedHashMap<>((Map<String, Object>) prevMap);
                merged.putAll((Map<String, Object>) curMap);
                result.put(key, merged);
            }
        }
        for (var key : prependProps) {
            var previous = last.get(key);
            var current = props.get(key);
            if (previous instanceof List<?> prevList && current instanceof List<?> curList) {
                var combined = new LinkedHashMap<String, Object>();
                for (var item : prevList) {
                    if (item instanceof Map<?, ?> map) {
                        var match = matchKey(map, matchPropsOn);
                        combined.put(String.valueOf(match), item);
                    }
                }
                for (var item : curList) {
                    if (item instanceof Map<?, ?> map) {
                        var match = matchKey(map, matchPropsOn);
                        combined.putIfAbsent(String.valueOf(match), item);
                    }
                }
                result.put(key, combined.values().stream().toList());
            }
        }
        for (var key : deepMergeProps) {
            var previous = last.get(key);
            var current = props.get(key);
            if (previous instanceof Map<?, ?> prevMap && current instanceof Map<?, ?> curMap) {
                result.put(key, deepMerge((Map<String, Object>) prevMap, (Map<String, Object>) curMap));
            }
        }
        return result;
    }

    /**
     * Store the current props as the "last request" snapshot used by the
     * next merge.
     *
     * @param props the props to persist
     */
    public void propagateProps(Map<String, Object> props) {
        var session = InertiaRequestContext.request() != null
            ? InertiaRequestContext.request().getSession(true)
            : null;
        if (session != null) {
            session.setAttribute(SESSION_LAST_PROPS, props);
        }
    }

    /**
     * Drop the stored last-request props (partial reset).
     */
    public void reset() {
        var session = InertiaRequestContext.request() != null
            ? InertiaRequestContext.request().getSession(false)
            : null;
        if (session != null) {
            session.removeAttribute(SESSION_LAST_PROPS);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> lastProps() {
        var session = InertiaRequestContext.request() != null
            ? InertiaRequestContext.request().getSession(false)
            : null;
        if (session == null) {
            return null;
        }
        var stored = session.getAttribute(SESSION_LAST_PROPS);
        return stored instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    private static Object matchKey(Map<?, ?> map, List<String> matchPropsOn) {
        for (var field : matchPropsOn) {
            var value = map.get(field);
            if (value != null) {
                return value;
            }
        }
        return map.get("id");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> deepMerge(Map<String, Object> base, Map<String, Object> overlay) {
        var result = new LinkedHashMap<>(base);
        for (var entry : overlay.entrySet()) {
            var current = result.get(entry.getKey());
            if (current instanceof Map<?, ?> curMap && entry.getValue() instanceof Map<?, ?> newMap) {
                result.put(entry.getKey(),
                    deepMerge((Map<String, Object>) curMap, (Map<String, Object>) newMap));
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}