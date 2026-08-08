package com.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import jakarta.enterprise.context.RequestScoped;

import com.quarkus.inertia.model.AlwaysProp;
import com.quarkus.inertia.model.PageObject;

/**
 * Applies a partial reload: keeps only the props requested by the client
 * ({@code X-Inertia-Partial-Data}/{@code Except}/{@code Reset} headers),
 * always retaining {@link AlwaysProp} values.
 */
@RequestScoped
public class PartialReloadProcessor {

    public PageObject apply(PageObject page, PartialReloadContext context) {
        if (context == null || !context.matchesComponent(page.component())) {
            return page;
        }

        Map<String, Object> filteredProps = new HashMap<>();

        for (var entry : page.props().entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (value instanceof AlwaysProp) {
                filteredProps.put(key, ((AlwaysProp<?>) value).value());
            } else {
                var kept = filterNode(key, value, context);
                if (kept != null) {
                    filteredProps.put(key, kept);
                }
            }
        }

        var result = page.withProps(Map.copyOf(filteredProps));

        if (context.hasReset()) {
            result = stripResetKeys(result, context.reset());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private Object filterNode(String path, Object value, PartialReloadContext context) {
        if (context.hasData() && !isRequested(path, context.data())) {
            return null;
        }
        if (context.hasExcept() && isExceptExcluded(path, context.except())) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            var filtered = new LinkedHashMap<String, Object>();
            for (var entry : map.entrySet()) {
                if (entry.getKey() == null) continue;
                var childPath = path + "." + entry.getKey();
                var kept = filterNode(childPath, entry.getValue(), context);
                if (kept != null) {
                    filtered.put((String) entry.getKey(), kept);
                }
            }
            if (filtered.isEmpty()) return null;
            return Map.copyOf(filtered);
        }
        return value;
    }

    private boolean isRequested(String path, Set<String> keys) {
        for (var key : keys) {
            if (path.equals(key)
                    || path.startsWith(key + ".")
                    || key.startsWith(path + ".")) {
                return true;
            }
        }
        return false;
    }

    private boolean isExceptExcluded(String path, Set<String> exceptKeys) {
        for (var key : exceptKeys) {
            if (path.equals(key) || path.startsWith(key + ".")) {
                return true;
            }
        }
        return false;
    }

    private PageObject stripResetKeys(PageObject page, Set<String> resetKeys) {
        var merge = page.mergeProps() == null ? java.util.List.<String>of() : page.mergeProps();
        var prepend = page.prependProps() == null ? java.util.List.<String>of() : page.prependProps();
        var deepMerge = page.deepMergeProps() == null ? java.util.List.<String>of() : page.deepMergeProps();
        var match = page.matchPropsOn() == null ? java.util.List.<String>of() : page.matchPropsOn();

        var newMerge = merge.stream().filter(k -> !resetKeys.contains(k)).toList();
        var newPrepend = prepend.stream().filter(k -> !resetKeys.contains(k)).toList();
        var newDeepMerge = deepMerge.stream().filter(k -> !resetKeys.contains(k)).toList();
        var newMatch = match.stream()
            .filter(k -> !resetKeys.stream().anyMatch(k::equals))
            .toList();

        return page.withMergeMetadata(newMerge, newPrepend, newDeepMerge, newMatch);
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
