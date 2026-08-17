package io.github.dg.spring.inertia.api;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import io.github.dg.spring.inertia.model.DeferredProp;
import io.github.dg.spring.inertia.model.PageObject;
import io.github.dg.spring.inertia.spi.ErrorMapper;

/**
 * The Inertia facade: the entry point injected into controllers.
 *
 * <p>Synchronous counterpart of the Quarkus adapter ({@code io.github.dg.
 * quarkus.inertia.api.Inertia}): every method returns plain values and the
 * resolvers are plain {@link Supplier}s, so Spring MVC controllers can
 * {@code return inertia.render(...)} directly.</p>
 *
 * <p>All methods are bound to the current request; the implementation is a
 * request-scoped proxy, so {@code Inertia} can be injected as a singleton
 * field of any controller.</p>
 */
public interface Inertia {

    /**
     * Render an Inertia page.
     *
     * @param component the frontend component name
     * @param props     the page props (may be {@code null})
     * @return the JSON response, or the HTML document for non-Inertia visits
     */
    Object render(String component, Map<String, Object> props);

    /**
     * Render an Inertia page with additional metadata.
     *
     * @param component the frontend component name
     * @param props     the page props (may be {@code null})
     * @param meta      extra page metadata
     * @return the response
     */
    Object render(String component, Map<String, Object> props, Map<String, Object> meta);

    /**
     * Build a custom response for an already-assembled page object.
     *
     * @param page   the page object
     * @param status the HTTP status of the response
     * @return the JSON response
     */
    InertiaResponse with(PageObject page, int status);

    /**
     * Redirect to a URL.
     *
     * @param url the target URL
     * @return the redirect response
     */
    InertiaRedirect redirect(String url);

    /**
     * Redirect to a URL, optionally forcing a full page load.
     *
     * @param url      the target URL
     * @param fullPage force a full page load (no client-side navigation)
     * @return the redirect response
     */
    InertiaRedirect redirect(String url, boolean fullPage);

    /**
     * Redirect back to the referer, defaulting to {@code /}.
     *
     * @return the redirect response
     */
    InertiaRedirect back();

    /**
     * Redirect back to the referer with a fallback URL.
     *
     * @param fallback used when no referer is present
     * @return the redirect response
     */
    InertiaRedirect back(String fallback);

    /**
     * Redirect back to the referer with the given status and headers.
     *
     * @param status  the HTTP status
     * @param headers extra response headers
     * @return the redirect response
     */
    InertiaRedirect back(int status, Map<String, String> headers);

    /**
     * Redirect back to the referer (or fallback) with the given status and
     * headers.
     *
     * @param status   the HTTP status
     * @param headers  extra response headers
     * @param fallback used when no referer is present
     * @return the redirect response
     */
    InertiaRedirect back(int status, Map<String, String> headers, String fallback);

    /**
     * Force a full page load to the given URL.
     *
     * @param url the target URL
     * @return the redirect response
     */
    InertiaRedirect location(String url);

    /**
     * Flash a value into the next page.
     *
     * @param key   the flash key
     * @param value the value
     */
    void flash(String key, Object value);

    /**
     * Set a shared prop: injected into every page of the visit.
     *
     * @param key   the prop key
     * @param value the value
     * @return the value, so it can be returned from helpers
     */
    Object share(String key, Object value);

    /**
     * Set several shared props at once.
     *
     * @param values the entries
     * @return the values map
     */
    Object share(Map<String, Object> values);

    /**
     * Read a shared prop.
     *
     * @param key the prop key
     * @return the value or {@code null}
     */
    Object shared(String key);

    /**
     * Register an "always" prop: included in every page, immune to partial
     * reloads and shared-prop flushes.
     *
     * @param key   the prop key
     * @param value the value
     * @return the value, so it can be returned from helpers
     */
    Object always(String key, Object value);

    /**
     * Register several "always" props at once.
     *
     * @param values the entries
     * @return the values map
     */
    Object always(Map<String, Object> values);

    /**
     * Register a deferred prop: excluded from the page until the client
     * performs a partial reload of its group.
     *
     * @param group    the deferred group name
     * @param name     the prop name
     * @param resolver produces the prop value on partial reload
     * @return the prop name
     */
    String deferred(String group, String name, Supplier<Object> resolver);

    /**
     * Register several deferred props at once.
     *
     * @param props name-to-prop registrations
     * @return the props map
     */
    Object deferred(Map<String, DeferredProp<Object>> props);

    /**
     * Register a once-prop: rendered by the client only once per session.
     *
     * @param key   the prop key
     * @param value the value
     * @return the value, so it can be returned from helpers
     */
    Object once(String key, Object value);

    /**
     * Register a once-prop with an expiry.
     *
     * @param key   the prop key
     * @param value the value
     * @param ttl   how long the client may display it
     * @return the value, so it can be returned from helpers
     */
    Object once(String key, Object value, Duration ttl);

    /**
     * Register a mergeable prop.
     *
     * @param key   the prop key
     * @param value the value
     * @return the value, so it can be returned from helpers
     */
    Object merge(String key, Object value);

    /**
     * Register a mergeable prop with a merge rule.
     *
     * @param key   the prop key
     * @param value the value
     * @param rule  {@code merge}, {@code prepend} or {@code deepMerge}
     * @return the value, so it can be returned from helpers
     */
    Object merge(String key, Object value, MergeRule rule);

    /**
     * Register several mergeable props with a shared rule.
     *
     * @param values the entries
     * @param rule   the merge rule
     * @return the values map
     */
    Object merge(Map<String, Object> values, MergeRule rule);

    /**
     * Wrap a raw JSON document to be embedded verbatim in the page props.
     *
     * @param json the JSON document
     * @return the wrapper
     */
    Object rawJson(String json);

    /**
     * Resolve a prop lazily; during partial reloads the callback is skipped
     * and the prop is omitted.
     *
     * @param callback the resolver
     * @return the resolved value (or a skip marker)
     */
    Object optional(Supplier<Object> callback);

    /**
     * Resolve a prop lazily with a fallback used during partial reloads.
     *
     * @param callback the resolver
     * @param fallback the value used when the prop is partial-reloaded
     * @return the resolved or fallback value
     */
    Object optional(Supplier<Object> callback, Optional<Object> fallback);

    /**
     * Cache a prop computation server-side.
     *
     * @param key      the cache key
     * @param resolver lazily evaluated on cache miss
     * @return a lazy wrapper resolving through the cache
     */
    Object cached(String key, Supplier<Object> resolver);

    /**
     * Cache a prop computation server-side with a TTL.
     *
     * @param key      the cache key
     * @param ttl      time-to-live
     * @param resolver lazily evaluated on cache miss
     * @return a lazy wrapper resolving through the cache
     */
    Object cached(String key, Duration ttl, Supplier<Object> resolver);

    /**
     * Override the asset version for the current visit.
     *
     * @param version the version string
     */
    void setVersion(String version);

    /**
     * Enable history-state encryption for the current visit.
     *
     * @param encrypt {@code true} to encrypt history state
     */
    void setEncryptHistory(boolean encrypt);

    /**
     * Enable camelCase-to-snake_case prop conversion for the current visit.
     *
     * @param camelize {@code true} to convert the prop keys
     */
    void setCamelizeProps(boolean camelize);

    /**
     * Attach an error mapper for exceptions raised later in the visit.
     *
     * @param mapper the mapper
     */
    void handleErrorUsing(ErrorMapper mapper);

    /**
     * Remember a scroll prop: its position is preserved between visits.
     *
     * @param key   the prop key
     * @param value the value
     * @return the value, so it can be returned from helpers
     */
    Object rememberScrollProp(String key, Object value);

    /**
     * Merge rules for {@link #merge(String, Object, MergeRule)}.
     */
    enum MergeRule {
        /** Merge previous and current values (lists and maps). */
        MERGE,
        /** Prepend previous values (lists). */
        PREPEND,
        /** Deep-merge previous and current maps. */
        DEEP_MERGE
    }
}