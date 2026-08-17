package io.github.dg.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Deep-merge utility for incoming prop values over existing ones; nested
 * maps are merged recursively, other values are replaced. Used for
 * mergeable and deep-mergeable props.
 */
@ApplicationScoped
public class MergePropProcessor {

    @SuppressWarnings("unchecked")
    public Map<String, Object> merge(Map<String, Object> existing, Map<String, Object> incoming) {
        var result = new HashMap<>(existing);
        for (var entry : incoming.entrySet()) {
            var key = entry.getKey();
            var incomingValue = entry.getValue();
            if (existing.containsKey(key) && existing.get(key) instanceof Map && incomingValue instanceof Map) {
                result.put(key, merge(
                    (Map<String, Object>) existing.get(key),
                    (Map<String, Object>) incomingValue
                ));
            } else {
                result.put(key, incomingValue);
            }
        }
        return result;
    }
}
