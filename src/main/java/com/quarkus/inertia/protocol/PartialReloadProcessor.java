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

        return page.withProps(Map.copyOf(filteredProps));
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
