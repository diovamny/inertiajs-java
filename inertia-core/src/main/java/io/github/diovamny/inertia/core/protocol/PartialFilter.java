package io.github.diovamny.inertia.core.protocol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Partial-reload prop filtering (protocol: partial reloads).
 *
 * <p>Algorithm (shared by both adapters): start with the always props; when
 * {@code only} is present keep just the selected props (dot notation
 * included); then remove {@code except} props. When both lists are absent the
 * full set passes through. A selected prop keeps its value even when
 * {@code null}: {@code null} is a real prop value, not an omission signal.
 * Adapters only extract the header values and the always set; everything else
 * is decided here.</p>
 */
public final class PartialFilter {

    private PartialFilter() {
    }

    /**
     * Whether the request targets the given component for a partial reload.
     *
     * @param requestComponent the {@code X-Inertia-Partial-Component} value, if any
     * @param pageComponent the component being rendered, if any
     * @return {@code true} when both name the same component
     */
    public static boolean matchesComponent(String requestComponent, String pageComponent) {
        return requestComponent != null && pageComponent != null
            && pageComponent.equals(requestComponent);
    }

    /**
     * Parse a comma-separated header value into an ordered, deduplicated set,
     * dropping blank segments.
     *
     * @param raw the raw header value, if any
     * @return the keys, empty when the header is absent
     */
    public static Set<String> parseCsv(String raw) {
        var keys = new LinkedHashSet<String>();
        if (raw == null) {
            return keys;
        }
        for (var part : raw.split(",")) {
            var trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                keys.add(trimmed);
            }
        }
        return keys;
    }

    /**
     * Parse a comma-separated header value into a list, dropping blanks.
     *
     * @param raw the raw header value, if any
     * @return the keys in header order, empty when the header is absent
     */
    public static List<String> parseCsvList(String raw) {
        return new ArrayList<>(parseCsv(raw));
    }

    /**
     * Filter props for a partial reload; always props survive unconditionally.
     *
     * @param props the full prop set
     * @param always keys that must always survive the filter
     * @param only keys requested via {@code X-Inertia-Partial-Data}
     * @param except keys excluded via {@code X-Inertia-Partial-Except}
     * @return the filtered map
     */
    public static Map<String, Object> filter(Map<String, Object> props,
            Map<String, Object> always, Set<String> only, Set<String> except) {
        Map<String, Object> result = new LinkedHashMap<>(always);
        if (!only.isEmpty() || !except.isEmpty()) {
            for (var entry : props.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                if (except.contains(key)) {
                    continue;
                }
                if (!only.isEmpty() && !isSelected(key, only)) {
                    continue;
                }
                var nestedOnly = matchingPrefixes(key, only);
                var nestedExcept = matchingPrefixes(key, except);
                Object kept = value;
                if (!nestedOnly.isEmpty() || !nestedExcept.isEmpty()) {
                    kept = filterNode(value, nestedOnly, nestedExcept);
                }
                result.put(key, kept);
            }
        } else {
            result.putAll(props);
        }
        return result;
    }

    /**
     * Sub-paths of {@code patterns} below {@code key} ({@code key.rest}).
     *
     * @param key the parent prop name
     * @param patterns the dot-notation patterns
     * @return the remainders below the parent, possibly empty
     */
    public static Set<String> matchingPrefixes(String key, Set<String> patterns) {
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

    /**
     * Whether {@code key} is selected by exact or parent match.
     *
     * @param key the prop name
     * @param patterns the dot-notation patterns
     * @return {@code true} on exact match or when a pattern nests below it
     */
    public static boolean isSelected(String key, Set<String> patterns) {
        for (var pattern : patterns) {
            if (pattern.equals(key) || pattern.startsWith(key + ".")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether {@code key} is excluded by exact match.
     *
     * @param key the prop name
     * @param exceptPatterns the exclusion patterns
     * @return {@code true} on exact match
     */
    public static boolean isExcluded(String key, Set<String> exceptPatterns) {
        return exceptPatterns.contains(key);
    }

    @SuppressWarnings("unchecked")
    private static Object filterNode(Object value, Set<String> onlyPaths, Set<String> exceptPaths) {
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
                ? filterNode(childValue, childOnly, childExcept)
                : childValue;
            result.put(childKey, kept);
        }
        return result.isEmpty() ? null : result;
    }
}
