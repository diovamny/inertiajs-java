package io.github.diovamny.quarkus.inertia.protocol;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;

import io.github.diovamny.inertia.core.model.AlwaysProp;
import io.github.diovamny.inertia.core.model.PageObject;
import io.github.diovamny.inertia.core.protocol.MergeLabels;
import io.github.diovamny.inertia.core.protocol.PartialFilter;

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

        // Unwrap AlwaysProp markers up front: the shared filter then sees
        // plain values, and the always set keeps them unconditional.
        Map<String, Object> plainProps = new LinkedHashMap<>();
        Map<String, Object> always = new LinkedHashMap<>();
        for (var entry : page.props().entrySet()) {
            var value = entry.getValue();
            if (value instanceof AlwaysProp) {
                var unwrapped = ((AlwaysProp<?>) value).value();
                plainProps.put(entry.getKey(), unwrapped);
                always.put(entry.getKey(), unwrapped);
            } else {
                plainProps.put(entry.getKey(), value);
            }
        }

        // The filtering algorithm lives in inertia-core (PartialFilter).
        var filteredProps = io.github.diovamny.inertia.core.protocol.PartialFilter.filter(
            plainProps, always,
            context.data() == null ? Set.of() : new LinkedHashSet<>(context.data()),
            context.except() == null ? Set.of() : new LinkedHashSet<>(context.except()));

        // Map.copyOf forbids null values, but null is a legitimate prop value
        // that must survive partial reloads, so copy defensively instead.
        var result = page.withProps(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(filteredProps)));

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

        // Reset pruning lives in inertia-core (MergeLabels).
        return page.withMergeMetadata(
            MergeLabels.pruneReset(new java.util.ArrayList<>(merge), resetKeys),
            MergeLabels.pruneReset(new java.util.ArrayList<>(prepend), resetKeys),
            MergeLabels.pruneReset(new java.util.ArrayList<>(deepMerge), resetKeys),
            MergeLabels.pruneReset(new java.util.ArrayList<>(match), resetKeys));
    }

    public record PartialReloadContext(
        String partialComponent,
        Set<String> data,
        Set<String> except,
        Set<String> reset
    ) {
        public boolean matchesComponent(String component) {
            return PartialFilter.matchesComponent(partialComponent, component);
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
