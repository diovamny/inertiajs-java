package io.github.diovamny.spring.inertia.renderer;

/**
 * Per-request SSR cache override, set via
 * {@code inertia.render(...).enableSsrCache(ttl)}. When absent the global
 * {@code inertia.ssr-cache-*} settings apply. Registered request-scoped.
 */
public class SsrCachePolicy {

    private Long ttlMillis;

    /**
     * Override the cache TTL for the current render ({@code null} clears).
     */
    public void setTtlMillis(Long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    /**
     * The per-render TTL override in millis, or {@code null} when unset.
     */
    public Long ttlMillis() {
        return ttlMillis;
    }
}
