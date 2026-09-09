package io.github.diovamny.quarkus.inertia.api;

import java.util.Map;
import io.github.diovamny.quarkus.inertia.spi.ErrorMapper;

/**
 * Specialized interface for page metadata, history instructions, headers,
 * root templates, error handling and SSR configuration in Quarkus Inertia.js.
 */
public interface InertiaMetadata {

    // ---------------------------------------------------------------------
    // Response Headers
    // ---------------------------------------------------------------------

    /**
     * Set a custom header on every response of the current request.
     *
     * @param name  the header name
     * @param value the header value
     */
    void header(String name, Object value);

    /**
     * Set several custom headers on every response of the current request.
     *
     * @param headers the headers to apply
     */
    void headers(Map<String, Object> headers);

    // ---------------------------------------------------------------------
    // Root Template View Data
    // ---------------------------------------------------------------------

    /**
     * Set data for the root HTML template that is NOT included in the page props.
     * Used for SEO meta tags, analytics tokens, or layout variables only
     * needed server-side in the HTML template.
     *
     * @param key   the view data key
     * @param value the value
     */
    void viewData(String key, Object value);

    /**
     * Set multiple root HTML template data entries at once.
     *
     * @param data map of view data entries
     */
    void viewData(Map<String, Object> data);

    // ---------------------------------------------------------------------
    // Page Metadata & History State
    // ---------------------------------------------------------------------

    /**
     * Add a metadata entry to the page object.
     *
     * @param key   the metadata key
     * @param value the metadata value
     */
    void meta(String key, Object value);

    /**
     * Add several metadata entries to the page object.
     *
     * @param values the metadata entries
     */
    void meta(Map<String, Object> values);

    /**
     * Request the client to encrypt the history state for the current visit.
     *
     * @param encrypt whether the client should encrypt history state
     */
    void encryptHistory(boolean encrypt);

    /**
     * Request the client to clear its history for the current visit.
     *
     * @param clear whether the client should clear its history
     */
    void clearHistory(boolean clear);

    /**
     * Request the client to preserve the URL fragment during the current visit.
     *
     * @param preserve whether the URL fragment should be preserved
     */
    void preserveFragment(boolean preserve);

    // ---------------------------------------------------------------------
    // Error Handling
    // ---------------------------------------------------------------------

    /**
     * Register a prop as "rescued".
     *
     * <p>Rescued props are preserved in the client's history even when the
     * server response fails, allowing the frontend to recover data from a
     * previous visit.</p>
     *
     * @param key the prop name
     */
    void rescue(String key);

    /**
     * Register a custom mapper that builds the Inertia response when a
     * request-scoped exception occurs.
     *
     * @param mapper the error mapper; {@code null} resets to the default
     */
    void handleErrorUsing(ErrorMapper mapper);

    // ---------------------------------------------------------------------
    // Root View & SSR Configuration
    // ---------------------------------------------------------------------

    /**
     * Override the root view (the server-side HTML template) for the current request.
     *
     * @param name the template name, e.g. {@code "app.html"}
     */
    void setRootView(String name);

    /**
     * Disable server-side rendering for the given paths for the current request.
     *
     * @param paths the URL paths excluded from SSR
     */
    void withoutSsr(String... paths);

    /**
     * Disable server-side rendering entirely for the current request.
     */
    void disableSsr();

    // ---------------------------------------------------------------------
    // Versioning
    // ---------------------------------------------------------------------

    /**
     * Return the current asset version used for cache-busting.
     *
     * @return the current version string
     */
    String getVersion();

    /**
     * Override the asset version for the current request.
     *
     * @param version the new version value
     */
    void version(String version);
}
