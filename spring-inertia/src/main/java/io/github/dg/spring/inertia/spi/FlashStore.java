package io.github.dg.spring.inertia.spi;

import java.util.Map;

/**
 * Storage backend for flash data: values that live only for the next
 * request (typically written by {@code InertiaRedirect.with(...)} /
 * {@code inertia.flash(...)} and drained into the page props when the next
 * page is rendered).
 *
 * <p>The default implementation stores flash data in the {@code HttpSession}
 * ({@code SpringFlashStore}); implement this interface as a Spring bean to
 * provide a different backend.</p>
 */
public interface FlashStore {

    /**
     * Store a value under the given key, replacing any existing one.
     *
     * @param key   the key
     * @param value the value; must not be {@code null}
     */
    void put(String key, Object value);

    /**
     * Store all values of the given map, replacing existing entries.
     *
     * @param values the entries; must not be {@code null}
     */
    void putAll(Map<String, Object> values);

    /**
     * Read a value without removing it.
     *
     * @param key          the key
     * @param defaultValue returned when no value is stored
     * @return the stored value or the default
     */
    Object get(String key, Object defaultValue);

    /**
     * Read and remove a value.
     *
     * @param key          the key
     * @param defaultValue returned when no value is stored
     * @return the stored value or the default
     */
    Object pull(String key, Object defaultValue);

    /**
     * Read and remove all stored values.
     *
     * @return all remaining values; never {@code null}
     */
    Map<String, Object> drain();

    /**
     * Whether the store currently holds any value.
     *
     * @return {@code true} when at least one value is stored
     */
    boolean hasData();
}
