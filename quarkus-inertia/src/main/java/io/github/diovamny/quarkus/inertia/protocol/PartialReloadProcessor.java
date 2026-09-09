package io.github.diovamny.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;

import io.github.diovamny.quarkus.inertia.model.AlwaysProp;
import io.github.diovamny.quarkus.inertia.model.PageObject;

/**
 * Applies a partial reload: keeps only the props requested by the client
 * ({@code X-Inertia-Partial-Data}/{@code Except}/{@code Reset} headers),
 * always retaining {@link AlwaysProp} values.
 *
 * <p>Algorithm (Inertia v3 compliant):
 * <ol>
 *   <li>Start with always props</li>
 *   <li>If only (partialData) is specified, include only those props (respecting dot notation)</li>
 *   <li>Then apply except (partialExcept) to remove excluded props (intersection when both present)</li>
 *   <li>If neither only nor except, include all props</li>
 * </ol>
 * Dot notation is supported for structural nested prop selection: requesting
 * {@code auth.user} copies only the {@code user} branch inside {@code auth},
 * and excluding {@code auth.user} removes only that branch keeping siblings.
 */
@ApplicationScoped
public class PartialReloadProcessor {

    public PageObject apply(PageObject page, PartialReloadContext context) {
        if (context == null || !context.matchesComponent(page.component())) {
            return page;
        }

        Map<String, Object> filteredProps = new HashMap<>();

        // Always props survive the filter unconditionally.
        for (var entry : page.props().entrySet()) {
            var value = entry.getValue();
            if (value instanceof AlwaysProp) {
                filteredProps.put(entry.getKey(), ((AlwaysProp<?>) value).value());
            }
        }

        var onlyKeys = new LinkedHashSet<>(context.data());
        var exceptKeys = new LinkedHashSet<>(context.except());

        // Copy the props that survive only/except, handling nested paths structurally.
        for (var entry : page.props().entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (value instanceof AlwaysProp) {
                continue; // already added
            }
            // Skip a top-level key entirely only when it is excluded exactly.
            if (exceptKeys.contains(key)) {
                continue;
            }
            // For "only" filtering, a top-level key is included when it is
            // selected exactly or when a nested path under it is selected.
            if (!onlyKeys.isEmpty() && !isSelected(key, onlyKeys)) {
                continue;
            }
            // Determine if nested paths apply to this top-level prop.
            var nestedOnly = matchingPrefixes(key, onlyKeys);
            var nestedExcept = matchingPrefixes(key, exceptKeys);
            Object kept = value;
            if (!nestedOnly.isEmpty() || !nestedExcept.isEmpty()) {
                kept = filterNode(value, nestedOnly, nestedExcept, key);
            }
            // A selected prop keeps its value even when it is null: null is
            // a real prop value, not an omission signal (lazy/optional use
            // dedicated marker types resolved upstream).
            filteredProps.put(key, kept);
        }

        // Map.copyOf forbids null values, but null is a legitimate prop value
        // that must survive partial reloads, so copy defensively instead.
        var result = page.withProps(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(filteredProps)));

        if (context.hasReset()) {
            result = stripResetKeys(result, context.reset());
        }

        return result;
    }

    /**
     * Collect all nested sub-paths (relative to the top-level key) that
     * patterns select. E.g. patterns {@code auth.user,auth.permissions}
     * for key {@code auth} returns {@code ["user", "permissions"]}.
     */
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

    /**
     * Recursively filter a nested value according to the requested (only) and
     * excluded (except) sub-paths. Returns the filtered value or {@code null}
     * when nothing survives.
     */
    @SuppressWarnings("unchecked")
    private Object filterNode(Object value, Set<String> onlyPaths, Set<String> exceptPaths, String path) {
        if (!(value instanceof Map<?, ?> map)) {
            return value;
        }
        var result = new LinkedHashMap<String, Object>();
        for (var child : map.entrySet()) {
            var childKey = String.valueOf(child.getKey());
            // Skip excluded children: match exact or descendant paths.
            if (isExcluded(childKey, exceptPaths)) {
                continue;
            }
            // Keep only requested children when only paths are present.
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

    private PageObject stripResetKeys(PageObject page, Set<String> resetKeys) {
        var merge = page.mergeProps() == null ? java.util.List.<String>of() : page.mergeProps();
        var prepend = page.prependProps() == null ? java.util.List.<String>of() : page.prependProps();
        var deepMerge = page.deepMergeProps() == null ? java.util.List.<String>of() : page.deepMergeProps();
        var match = page.matchPropsOn() == null ? java.util.List.<String>of() : page.matchPropsOn();

        var newMerge = merge.stream().filter(k -> !isDescendantOrSelf(k, resetKeys)).toList();
        var newPrepend = prepend.stream().filter(k -> !isDescendantOrSelf(k, resetKeys)).toList();
        var newDeepMerge = deepMerge.stream().filter(k -> !isDescendantOrSelf(k, resetKeys)).toList();
        var newMatch = match.stream().filter(k -> !isDescendantOrSelf(k, resetKeys)).toList();

        return page.withMergeMetadata(newMerge, newPrepend, newDeepMerge, newMatch);
    }

    /**
     * Check if a key is equal to or a descendant of any reset key.
     * E.g. {@code contacts.data.id} is a descendant of {@code contacts}.
     */
    private boolean isDescendantOrSelf(String key, Set<String> resetKeys) {
        for (var resetKey : resetKeys) {
            if (key.equals(resetKey) || key.startsWith(resetKey + ".")) {
                return true;
            }
        }
        return false;
    }

    public record PartialReloadContext(
        String partialComponent,
        Set<String> data,
        Set<String> except,
        Set<String> reset
    ) {
        public boolean matchesComponent(String component) {
            return partialComponent != null && partialComponent.equals(component);
        }

        public boolean hasData() {
            return data != null && !data.isEmpty();
        }

        public boolean hasExcept() {
            return except != null && !except.isEmpty();
        }

        public boolean hasReset() {
            return reset != null && !reset.isEmpty();
        }
    }
}
