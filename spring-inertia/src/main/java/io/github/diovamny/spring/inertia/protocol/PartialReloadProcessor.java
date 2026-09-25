package io.github.diovamny.spring.inertia.protocol;

import java.util.List;
import java.util.Map;

import io.github.diovamny.inertia.core.protocol.PartialFilter;
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
        return PartialFilter.matchesComponent(
            partialComponent == null ? null : String.valueOf(partialComponent), component);
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
        return PartialFilter.parseCsvList(stringAttr(InertiaHeaderExtractor.CONTEXT_PARTIAL_DATA));
    }

    /**
     * The props explicitly excluded via {@code X-Inertia-Partial-Except}.
     *
     * @return the excluded keys, empty when the header is absent
     */
    public List<String> partialExcept() {
        return PartialFilter.parseCsvList(stringAttr(InertiaHeaderExtractor.CONTEXT_PARTIAL_EXCEPT));
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
        // The filtering algorithm lives in inertia-core (PartialFilter);
        // this class only extracts the request-scoped header values.
        var onlyKeys = PartialFilter.parseCsv(stringAttr(InertiaHeaderExtractor.CONTEXT_PARTIAL_DATA));
        var exceptKeys = PartialFilter.parseCsv(stringAttr(InertiaHeaderExtractor.CONTEXT_PARTIAL_EXCEPT));
        return PartialFilter.filter(props, always, onlyKeys, exceptKeys);
    }

    private static String stringAttr(String name) {
        var value = InertiaRequestContext.get(name);
        return value == null ? null : String.valueOf(value);
    }
}
