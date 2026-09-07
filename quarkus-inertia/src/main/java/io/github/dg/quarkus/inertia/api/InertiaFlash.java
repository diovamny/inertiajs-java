package io.github.dg.quarkus.inertia.api;

import java.util.Map;

/**
 * Specialized interface for managing session flash data in Quarkus Inertia.js.
 *
 * <p>Flash data is persisted in the session for exactly one subsequent request,
 * then automatically cleared. Commonly used for transient success messages,
 * alerts, and validation error bags.</p>
 */
public interface InertiaFlash {

    /**
     * Flash a value that is persisted in the session and automatically merged
     * into the {@code props} of the next rendered page, then consumed.
     *
     * @param key   the flash key (e.g. {@code "success"}, {@code "errors"})
     * @param value the flash value
     */
    void flash(String key, Object value);

    /**
     * Flash several values at once.
     *
     * @param values the flash entries
     */
    void flash(Map<String, Object> values);

    /**
     * Read a flashed value without consuming it.
     *
     * @param key          the flash key
     * @param defaultValue returned when the key is not present
     * @return the flashed value or {@code defaultValue}
     */
    Object getFlash(String key, Object defaultValue);

    /**
     * Read and consume a flashed value.
     *
     * @param key          the flash key
     * @param defaultValue returned when the key is not present
     * @return the flashed value or {@code defaultValue}
     */
    Object pullFlash(String key, Object defaultValue);
}