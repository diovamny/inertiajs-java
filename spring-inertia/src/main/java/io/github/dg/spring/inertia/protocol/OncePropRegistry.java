package io.github.dg.spring.inertia.protocol;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.context.annotation.RequestScope;

import io.github.dg.spring.inertia.internal.InertiaRequestContext;
import io.github.dg.spring.inertia.model.OnceProp;

/**
 * Request-scoped registry of once-props: props the client renders only once
 * per browser session (e.g. flash notifications).
 *
 * <p>In Inertia v3, the client sends {@code X-Inertia-Except-Once-Props}
 * header with the tracking keys of once-props it has already seen. The server
 * omits the values for those keys but still sends the metadata so the client
 * can track them. This implementation uses the client header instead of
 * server-side session state.</p>
 */
@RequestScope
public class OncePropRegistry {

    private final Map<String, OnceProp> metadata = new LinkedHashMap<>();

    /**
     * Register a once-prop for the current page.
     *
     * @param key   the prop key
     * @param ttl   optional expiry; {@code null} never expires
     * @return the prop key
     */
    public String remember(String key, Duration ttl) {
        return remember(key, null, ttl);
    }

    /**
     * Register a once-prop under a custom client tracking key.
     *
     * <p>The client remembers the prop under {@code customKey}, so several
     * props can share a single "already shown" entry; the metadata is sent
     * under the custom key while the prop value stays under {@code key}.</p>
     *
     * @param key       the prop key
     * @param customKey the tracking key sent to the client; {@code null} uses
     *                  {@code key}
     * @param ttl       optional expiry; {@code null} never expires
     * @return the prop key
     */
    public String remember(String key, String customKey, Duration ttl) {
        var expiresAt = ttl != null ? System.currentTimeMillis() + ttl.toMillis() : null;
        var trackingKey = customKey != null ? customKey : key;
        metadata.put(trackingKey, new OnceProp(key, expiresAt));
        return key;
    }

    /**
     * All once-prop metadata registered for the current page.
     *
     * @return the entries
     */
    public Map<String, OnceProp> metadata() {
        return metadata;
    }

    /**
     * Get the set of once-prop tracking keys that the client has already seen,
     * based on the {@code X-Inertia-Except-Once-Props} header.
     *
     * @return the set of tracking keys the client has seen
     */
    public Set<String> exceptOnceKeys() {
        var header = InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_EXCEPT_ONCE_PROPS);
        if (header == null || header.toString().isBlank()) {
            return Set.of();
        }
        return java.util.Arrays.stream(header.toString().split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }

    /**
     * Check if a tracking key has been seen by the client (based on
     * {@code X-Inertia-Except-Once-Props} header).
     *
     * @param trackingKey the tracking key
     * @return {@code true} when the client has already seen this once-prop
     */
    public boolean alreadyShown(String trackingKey) {
        return exceptOnceKeys().contains(trackingKey);
    }

    /**
     * No-op: the client tracks shown once-props via the
     * {@code X-Inertia-Except-Once-Props} header.
     *
     * @param key the prop key (ignored)
     */
    public void markShown(String key) {
        // No-op: client-side tracking via X-Inertia-Except-Once-Props header
    }
}
