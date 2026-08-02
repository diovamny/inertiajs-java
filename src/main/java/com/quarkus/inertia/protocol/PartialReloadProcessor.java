package com.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jakarta.enterprise.context.RequestScoped;

import com.quarkus.inertia.model.AlwaysProp;
import com.quarkus.inertia.model.PageObject;

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

            boolean isAlways = value instanceof AlwaysProp;

            boolean matchesData = !context.hasData() || context.data().contains(key);
            boolean matchesExcept = context.hasExcept() && context.except().contains(key);

            if (isAlways) {
                filteredProps.put(key, ((AlwaysProp<?>) value).value());
            } else if (matchesData && !matchesExcept) {
                filteredProps.put(key, value);
            }
        }

        var result = page.withProps(Map.copyOf(filteredProps));

        if (context.hasReset()) {
            result = stripResetKeys(result, context.reset());
        }

        return result;
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
