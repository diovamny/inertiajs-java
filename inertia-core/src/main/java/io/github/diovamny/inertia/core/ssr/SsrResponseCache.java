package io.github.diovamny.inertia.core.ssr;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Bounded in-memory cache for SSR sidecar responses (Rails
 * {@code ssr_cache} parity).
 *
 * <p>Keys are the MD5 hex of the page JSON, so identical pages share one
 * entry regardless of URL. Entries expire after their TTL; expired entries
 * are dropped on access and the cache evicts the oldest entries beyond
 * {@code maxEntries}. Thread-safe.</p>
 */
public class SsrResponseCache {

    /**
     * A cached SSR payload.
     *
     * @param body      the rendered app HTML fragment
     * @param head      the rendered head fragments
     * @param expiresAt epoch millis when the entry expires
     */
    public record Entry(String body, java.util.List<String> head, long expiresAt) {
    }

    private final int maxEntries;
    private final Map<String, Entry> entries = new LinkedHashMap<>();

    /**
     * @param maxEntries upper bound of live entries (at least 1)
     */
    public SsrResponseCache(int maxEntries) {
        if (maxEntries < 1) {
            throw new IllegalArgumentException("maxEntries must be at least 1");
        }
        this.maxEntries = maxEntries;
    }

    /**
     * Look up the SSR payload for a page JSON document.
     *
     * @param pageJson the page JSON
     * @return the live entry, or empty on miss/expiry
     */
    public synchronized Optional<Entry> get(String pageJson) {
        if (pageJson == null) {
            return Optional.empty();
        }
        var entry = entries.get(key(pageJson));
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAt() <= System.currentTimeMillis()) {
            entries.remove(key(pageJson));
            return Optional.empty();
        }
        return Optional.of(entry);
    }

    /**
     * Store the SSR payload for a page JSON document.
     *
     * @param pageJson the page JSON
     * @param body     the rendered app HTML fragment
     * @param head     the rendered head fragments
     * @param ttl      how long the entry stays live
     * @param ttlUnit  unit of the TTL
     */
    public synchronized void put(String pageJson, String body, java.util.List<String> head,
            long ttl, java.util.concurrent.TimeUnit ttlUnit) {
        if (pageJson == null) {
            return;
        }
        entries.put(key(pageJson), new Entry(body,
            head != null ? java.util.List.copyOf(head) : java.util.List.of(),
            System.currentTimeMillis() + Math.max(0, ttlUnit.toMillis(ttl))));
        while (entries.size() > maxEntries) {
            var oldest = entries.keySet().iterator().next();
            entries.remove(oldest);
        }
    }

    /**
     * Drop every entry.
     */
    public synchronized void invalidateAll() {
        entries.clear();
    }

    /**
     * Number of live entries (expired ones are purged first).
     */
    public synchronized int size() {
        entries.entrySet().removeIf(entry ->
            entry.getValue().expiresAt() <= System.currentTimeMillis());
        return entries.size();
    }

    /**
     * MD5 hex of the page JSON (the cache key).
     */
    public static String key(String pageJson) {
        try {
            var digest = MessageDigest.getInstance("MD5")
                .digest(pageJson.getBytes(StandardCharsets.UTF_8));
            var hex = new StringBuilder(digest.length * 2);
            for (var b : digest) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("MD5 unavailable", e);
        }
    }
}
