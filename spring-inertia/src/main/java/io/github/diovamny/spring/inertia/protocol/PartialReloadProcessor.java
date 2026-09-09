package io.github.diovamny.spring.inertia.protocol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.diovamny.spring.inertia.internal.InertiaRequestContext;

/**
 * Computes the prop set of a partial reload from the
 * {@code X-Inertia-Partial-*} headers:
 *
 * <ul>
 *   <li>empty headers - full page, all props</li>
 *   <li>{@code X-Inertia-Partial-Data} (only) - only the listed props</li>
 *   <li>{@code X-Inertia-Partial-Except} - all props except the listed ones</li>
 * </ul>
 *
 * <p>When both {@code only} and {@code except} are present, the intersection
 * is applied: only props in {@code only} that are not in {@code except} are
 * included. Dot notation is supported for nested prop selection.</p>
 */
public class PartialReloadProcessor {

    /**
     * Whether the current request is a partial reload of the given component.
     *
     * @param component the page component
     * @return {@code true} when the partial component matches
     */
    public boolean isPartialReload(String component) {
        var partialComponent = InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_PARTIAL_COMPONENT);
        return partialComponent != null && component != null
            && component.equals(String.valueOf(partialComponent));
    }

    /**
     * Whether the current request carries {@code X-Inertia-Partial-Reset}.
     *
     * @return {@code true} when the reset header is present
     */
    public boolean isPartialReset() {
        return InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_PARTIAL_RESET) != null;
    }

    /**
     * The props explicitly requested via {@code X-Inertia-Partial-Data}.
     *
     * @return the requested keys, empty when the header is absent
     */
    public List<String> partialData() {
        return attrList(InertiaHeaderExtractor.CONTEXT_PARTIAL_DATA);
    }

    /**
     * The props explicitly excluded via {@code X-Inertia-Partial-Except}.
     *
     * @return the excluded keys, empty when the header is absent
     */
    public List<String> partialExcept() {
        return attrList(InertiaHeaderExtractor.CONTEXT_PARTIAL_EXCEPT);
    }

    /**
     * Filter the props for the partial reload. "Always" props survive the
     * filter unconditionally.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Start with always props</li>
     *   <li>If only (partialData) is specified, include only those props (respecting dot notation)</li>
     *   <li>Then apply except (partialExcept) to remove excluded props</li>
     *   <li>If neither only nor except, include all props</li>
     * </ol>
     *
     * @param props  the full prop set
     * @param always keys that must always survive the filter
     * @return the filtered map
     */
    public Map<String, Object> filterProps(Map<String, Object> props, Map<String, Object> always) {
        var onlyKeys = new LinkedHashSet<>(attrList(InertiaHeaderExtractor.CONTEXT_PARTIAL_DATA));
        var exceptKeys = new LinkedHashSet<>(attrList(InertiaHeaderExtractor.CONTEXT_PARTIAL_EXCEPT));

        Map<String, Object> result = new LinkedHashMap<>(always);

        if (!onlyKeys.isEmpty() || !exceptKeys.isEmpty()) {
            for (var entry : props.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                if (exceptKeys.contains(key)) {
                    continue;
                }
                if (!onlyKeys.isEmpty() && !isSelected(key, onlyKeys)) {
                    continue;
                }
                var nestedOnly = matchingPrefixes(key, onlyKeys);
                var nestedExcept = matchingPrefixes(key, exceptKeys);
                Object kept = value;
                if (!nestedOnly.isEmpty() || !nestedExcept.isEmpty()) {
                    kept = filterNode(value, nestedOnly, nestedExcept, key);
                }
                // A selected prop keeps its value even when it is null: null is
                // a real prop value, not an omission signal (lazy/optional use
                // dedicated marker types resolved upstream).
                result.put(key, kept);
            }
        } else {
            result.putAll(props);
        }
        return result;
    }

    private Set<String> matchingPrefixes(String key, Set<String> patterns) {
        var result = new LinkedHashSet<String>();
        for (var pattern : patterns) {
            if (pattern.startsWith(key + ".")) {
                var rest = pattern.substring(key.length() + 1);
                if (!rest.isEmpty()) {
                    result.add(rest);
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object filterNode(Object value, Set<String> onlyPaths, Set<String> exceptPaths, String path) {
        if (!(value instanceof Map<?, ?> map)) {
            return value;
        }
        var result = new LinkedHashMap<String, Object>();
        for (var child : map.entrySet()) {
            var childKey = String.valueOf(child.getKey());
            if (isExcluded(childKey, exceptPaths)) {
                continue;
            }
            if (!onlyPaths.isEmpty() && !isSelected(childKey, onlyPaths)) {
                continue;
            }
            var childValue = child.getValue();
            var childOnly = matchingPrefixes(childKey, onlyPaths);
            var childExcept = matchingPrefixes(childKey, exceptPaths);
            var kept = (!childOnly.isEmpty() || !childExcept.isEmpty()) && childValue instanceof Map
                ? filterNode(childValue, childOnly, childExcept, path + "." + childKey)
                : childValue;
            result.put(childKey, kept);
        }
        return result.isEmpty() ? null : result;
    }

    private boolean isSelected(String key, Set<String> patterns) {
        for (var pattern : patterns) {
            if (pattern.equals(key) || pattern.startsWith(key + ".")) {
                return true;
            }
        }
        return false;
    }

    private boolean isExcluded(String key, Set<String> exceptPatterns) {
        return exceptPatterns.contains(key);
    }

    private static List<String> attrList(String name) {
        var value = InertiaRequestContext.get(name);
        if (value == null) {
            return List.of();
        }
        var list = new ArrayList<String>();
        for (var part : String.valueOf(value).split(",")) {
            var trimmed = part.trim();
            if (!trimmed.isBlank()) {
                list.add(trimmed);
            }
        }
        return list;
    }
}
