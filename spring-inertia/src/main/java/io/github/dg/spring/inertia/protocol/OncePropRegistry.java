package io.github.dg.spring.inertia.protocol;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.context.annotation.RequestScope;

import io.github.dg.spring.inertia.internal.InertiaRequestContext;
import io.github.dg.spring.inertia.model.OnceProp;

/**
 * Request-scoped registry of once-props: props the client renders only once
 * per browser session (e.g. flash notifications).
 *
 * <p>When a once-prop has already been shown in the client history, the
 * server omits its value; the corresponding {@link OnceProp} metadata is
 * always sent so the client can track it. Shown keys are recorded in the
 * {@code HttpSession}.</p>
 */
@RequestScope
public class OncePropRegistry {

    /** Session prefix for once-prop history entries. */
    public static final String SESSION_PREFIX = "__inertia_once:";

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
     * Whether the client has already seen this once-prop.
     *
     * @param key the prop key
     * @return {@code true} when the key is recorded in the session
     */
    public boolean alreadyShown(String key) {
        var session = InertiaRequestContext.request() != null
            ? InertiaRequestContext.request().getSession(false)
            : null;
        return session != null && session.getAttribute(SESSION_PREFIX + key) != null;
    }

    /**
     * Mark a once-prop as shown so subsequent pages omit it.
     *
     * @param key the prop key
     */
    public void markShown(String key) {
        var session = InertiaRequestContext.request() != null
            ? InertiaRequestContext.request().getSession(true)
            : null;
        if (session != null) {
            session.setAttribute(SESSION_PREFIX + key, Boolean.TRUE);
        }
    }
}