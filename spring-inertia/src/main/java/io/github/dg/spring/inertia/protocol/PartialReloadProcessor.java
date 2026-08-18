package io.github.dg.spring.inertia.protocol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.dg.spring.inertia.internal.InertiaRequestContext;

/**
 * Computes the prop set of a partial reload from the
 * {@code X-Inertia-Partial-*} headers:
 *
 * <ul>
 *   <li>empty headers - full page, all props</li>
 *   <li>{@code X-Inertia-Partial-Data} - only the listed props</li>
 *   <li>{@code X-Inertia-Partial-Except} - all props except the listed ones</li>
 * </ul>
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
     * @param props  the full prop set
     * @param always keys that must always survive the filter
     * @return the filtered map
     */
    public Map<String, Object> filterProps(Map<String, Object> props, Map<String, Object> always) {
        var partialData = attrList(InertiaHeaderExtractor.CONTEXT_PARTIAL_DATA);
        var partialExcept = attrList(InertiaHeaderExtractor.CONTEXT_PARTIAL_EXCEPT);

        Map<String, Object> result = new LinkedHashMap<>(always);
        if (!partialData.isEmpty()) {
            for (var key : partialData) {
                if (props.containsKey(key)) {
                    result.put(key, props.get(key));
                }
            }
        } else if (!partialExcept.isEmpty()) {
            for (var entry : props.entrySet()) {
                if (!partialExcept.contains(entry.getKey())) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        } else {
            result.putAll(props);
        }
        return result;
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