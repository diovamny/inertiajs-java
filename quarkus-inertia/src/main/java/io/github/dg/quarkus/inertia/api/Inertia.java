package io.github.dg.quarkus.inertia.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

import io.github.dg.quarkus.inertia.model.RawJson;
import io.github.dg.quarkus.inertia.spi.ErrorMapper;

/**
 * The Inertia server-side entry point, modeled after the official
 * {@code inertia-laravel} / {@code inertia-rails} adapters.
 *
 * <p>Inject it into any CDI/JAX-RS component and return the {@link Uni} it
 * produces from your endpoint. The request/response handling (JSON page vs.
 * HTML, redirects, partial reloads, deferred props, validation errors) is
 * resolved automatically based on the {@code X-Inertia} headers.</p>
 *
 * <p>Basic usage:</p>
 * <pre>{@code
 * @Path("/contacts")
 * public class ContactsController {
 *
 *     @Inject
 *     Inertia inertia;
 *
 *     @GET
 *     public Uni<Object> index() {
 *         return inertia.render("Contacts/Index",
 *             Map.of("contacts", contactRepository.listAll()));
 *     }
 *
 *     @PUT
 *     @Path("{id}")
 *     @Consumes(MediaType.APPLICATION_JSON)
 *     public Uni<Object> update(@PathParam("id") long id, ContactForm form) {
 *         contacts.update(id, form);
 *         return inertia.back().with("success", "Contact updated.");
 *     }
 * }
 * }</pre>
 *
 * <p>Every method is request-scoped: shared data, flash data and headers are
 * applied to the current request's response and never leak across requests.</p>
 *
 * <p>See the equivalent Laravel API for reference:
 * {@code Inertia::render()}, {@code Inertia::share()}, {@code Inertia::defer()},
 * {@code Inertia::optional()}, {@code Inertia::once()}, {@code Inertia::merge()},
 * {@code Inertia::always()}.</p>
 */
public interface Inertia {

    // ---------------------------------------------------------------------
    // Rendering
    // ---------------------------------------------------------------------

    /**
     * Render an Inertia page for the given component with the given props.
     *
     * <p>For Inertia requests the response is the JSON page object
     * ({@code {component, props, url, version, ...}}); for regular browser
     * requests it is the full HTML document that bootstraps the frontend with
     * the same page data embedded.</p>
     *
     * @param component the frontend component name, e.g. {@code "Contacts/Index"}
     * @param props     the page props; merged with shared and flash data
     * @return a Uni resolving to the response object (JSON page or HTML)
     * @see #render(String)
     */
    Uni<Object> render(String component, Map<String, Object> props);

    /**
     * Render an Inertia page with no props.
     *
     * @param component the frontend component name
     * @return a Uni resolving to the response object
     * @see #render(String, Map)
     */
    Uni<Object> render(String component);

    /**
     * Render an Inertia page whose component name comes from an enum constant.
     *
     * @param component the enum constant; its {@link Enum#name() name} is used
     *                  as the component name
     * @return a Uni resolving to the response object
     */
    Uni<Object> render(Enum<?> component);

    /**
     * Render an Inertia page whose component name comes from an enum constant,
     * with the given props.
     *
     * @param component the enum constant; its {@link Enum#name() name} is used
     *                  as the component name
     * @param props     the page props
     * @return a Uni resolving to the response object
     */
    Uni<Object> render(Enum<?> component, Map<String, Object> props);

    /**
     * Render an Inertia page with the given props and an explicit HTTP
     * status code.
     *
     * <p>The status is applied to both the JSON page response (Inertia
     * requests) and the HTML document (regular browser requests), enabling
     * error pages such as {@code 403}/{@code 404}/{@code 500} that render a
     * frontend component (Laravel's {@code Inertia::render(..., status)}
     * parity).</p>
     *
     * @param component the frontend component name
     * @param props     the page props
     * @param status    the HTTP status of the response
     * @return a Uni resolving to the response object
     * @see #render(String, Map)
     */
    Uni<Object> render(String component, Map<String, Object> props, int status);

    /**
     * Render an Inertia page with no props and an explicit HTTP status.
     *
     * @param component the frontend component name
     * @param status    the HTTP status of the response
     * @return a Uni resolving to the response object
     * @see #render(String, Map, int)
     */
    Uni<Object> render(String component, int status);

    /**
     * Render an Inertia page whose component name comes from an enum
     * constant, with an explicit HTTP status.
     *
     * @param component the enum constant; its {@link Enum#name() name} is used
     *                  as the component name
     * @param status    the HTTP status of the response
     * @return a Uni resolving to the response object
     * @see #render(String, int)
     */
    Uni<Object> render(Enum<?> component, int status);

    /**
     * Render an Inertia page whose component name comes from an enum
     * constant, with the given props and an explicit HTTP status.
     *
     * @param component the enum constant; its {@link Enum#name() name} is used
     *                  as the component name
     * @param props     the page props
     * @param status    the HTTP status of the response
     * @return a Uni resolving to the response object
     * @see #render(String, Map, int)
     */
    Uni<Object> render(Enum<?> component, Map<String, Object> props, int status);

    // ---------------------------------------------------------------------
    // Synchronous JAX-RS Response API
    // ---------------------------------------------------------------------

    /**
     * Render an Inertia page and return a synchronous JAX-RS {@link Response}.
     * Useful for non-reactive JAX-RS endpoints.
     *
     * @param component the frontend component name
     * @param props     the page props
     * @return a JAX-RS Response (JSON page or HTML document)
     */
    jakarta.ws.rs.core.Response renderSync(String component, Map<String, Object> props);

    /**
     * Render an Inertia page with no props and return a synchronous JAX-RS Response.
     */
    jakarta.ws.rs.core.Response renderSync(String component);

    /**
     * Render an Inertia page whose component name comes from an enum constant.
     */
    jakarta.ws.rs.core.Response renderSync(Enum<?> component);

    /**
     * Render an Inertia page whose component name comes from an enum constant,
     * with the given props.
     */
    jakarta.ws.rs.core.Response renderSync(Enum<?> component, Map<String, Object> props);

    /**
     * Render an Inertia page with the given props and an explicit HTTP
     * status code, returning a synchronous JAX-RS Response.
     */
    jakarta.ws.rs.core.Response renderSync(String component, Map<String, Object> props, int status);

    /**
     * Render an Inertia page with no props and an explicit HTTP status.
     */
    jakarta.ws.rs.core.Response renderSync(String component, int status);

    /**
     * Render an Inertia page whose component name comes from an enum
     * constant, with an explicit HTTP status.
     */
    jakarta.ws.rs.core.Response renderSync(Enum<?> component, int status);

    /**
     * Render an Inertia page whose component name comes from an enum
     * constant, with the given props and an explicit HTTP status.
     */
    jakarta.ws.rs.core.Response renderSync(Enum<?> component, Map<String, Object> props, int status);

    /**
     * Redirect to an internal URL and return a synchronous JAX-RS Response.
     */
    jakarta.ws.rs.core.Response redirectSync(String url);

    /**
     * Redirect to an internal URL, optionally forcing a full page load.
     */
    jakarta.ws.rs.core.Response redirectSync(String url, boolean fullPage);

    /**
     * Redirect back to the previous page.
     */
    jakarta.ws.rs.core.Response backSync();

    /**
     * Redirect back to the previous page, falling back to the given URL.
     */
    jakarta.ws.rs.core.Response backSync(String fallback);

    /**
     * Redirect back with an explicit status code and fallback URL.
     */
    jakarta.ws.rs.core.Response backSync(int status, String fallback);

    /**
     * Redirect back with an explicit status code and response headers.
     */
    jakarta.ws.rs.core.Response backSync(int status, Map<String, String> headers);

    /**
     * Redirect back with an explicit status code, response headers and
     * fallback URL.
     */
    jakarta.ws.rs.core.Response backSync(int status, Map<String, String> headers, String fallback);

    // ---------------------------------------------------------------------
    // Vert.x Reactive Routes (synchronous API)
    // ---------------------------------------------------------------------

    /**
     * Render an Inertia page directly into a Vert.x {@link RoutingContext}.
     * For use in {@code @Route} handlers.
     *
     * @param rc        the routing context
     * @param component the frontend component name
     * @param props     the page props
     */
    void renderVertx(io.vertx.ext.web.RoutingContext rc, String component, Map<String, Object> props);

    /**
     * Render an Inertia page directly into a Vert.x {@link RoutingContext}
     * with no props.
     */
    void renderVertx(io.vertx.ext.web.RoutingContext rc, String component);

    /**
     * Redirect to a URL directly in a Vert.x {@link RoutingContext}.
     */
    void redirectVertx(io.vertx.ext.web.RoutingContext rc, String url);

    /**
     * Redirect to a URL, optionally forcing a full page load, in a Vert.x
     * {@link RoutingContext}.
     */
    void redirectVertx(io.vertx.ext.web.RoutingContext rc, String url, boolean fullPage);

    /**
     * Redirect back to the previous page in a Vert.x {@link RoutingContext}.
     */
    void backVertx(io.vertx.ext.web.RoutingContext rc);

    /**
     * Redirect back to the previous page with a fallback URL in a Vert.x
     * {@link RoutingContext}.
     */
    void backVertx(io.vertx.ext.web.RoutingContext rc, String fallback);

    // ---------------------------------------------------------------------
    // Redirects
    // ---------------------------------------------------------------------

    /**
     * Redirect to an internal URL.
     *
     * <p>Non-GET requests get a {@code 303 See Other}, GET requests a
     * {@code 302 Found} (standard Inertia behavior). The returned
     * {@link InertiaRedirect} also accepts external URLs — those are sent to
     * the client as a {@code 409 Conflict} with the {@code X-Inertia-Location}
     * header when the request is an Inertia request.</p>
     *
     * @param url the destination URL (relative, e.g. {@code "/contacts"})
     * @return a chainable redirect; resolves to the redirect response
     */
    InertiaRedirect redirect(String url);

    /**
     * Redirect to an internal URL, optionally forcing a full page load.
     *
     * <p>When {@code fullPage} is {@code true} and the request is an Inertia
     * request, the server responds {@code 409 Conflict} with an
     * {@code X-Inertia-Location} header so the client reloads the page from
     * scratch instead of following the redirect via a visit.</p>
     *
     * @param url      the destination URL (relative)
     * @param fullPage whether to force a full browser page load
     * @return a chainable redirect; resolves to the redirect response
     */
    InertiaRedirect redirect(String url, boolean fullPage);

    /**
     * Redirect to an external URL outside the application.
     *
     * <p>For Inertia requests the response is a {@code 409 Conflict} carrying
     * the destination in the {@code X-Inertia-Location} header, so the client
     * performs a real browser navigation (equivalent of Laravel's
     * {@code Inertia::location()}).</p>
     *
     * @param url the external destination URL
     * @return a Uni resolving to the redirect response
     */
    Uni<Object> location(String url);

    /**
     * Redirect back to the previous page.
     *
     * <p>The destination is taken from the {@code Referer} header of the
     * current request (which the Inertia client always sends); if it is
     * missing, {@code "/"} is used.</p>
     *
     * @return a chainable redirect; resolves to the redirect response
     */
    InertiaRedirect back();

    /**
     * Redirect back to the previous page, falling back to the given URL.
     *
     * @param fallback the URL used when no {@code Referer} header is present
     * @return a chainable redirect; resolves to the redirect response
     * @see #back()
     */
    InertiaRedirect back(String fallback);

    /**
     * Redirect back with an explicit status code and fallback URL.
     *
     * @param status   the HTTP status of the redirect response
     * @param fallback the URL used when no {@code Referer} header is present
     * @return a chainable redirect; resolves to the redirect response
     * @see #back()
     */
    InertiaRedirect back(int status, String fallback);

    /**
     * Redirect back with an explicit status code and response headers.
     *
     * @param status  the HTTP status of the redirect response
     * @param headers custom headers applied to the redirect response
     * @return a chainable redirect; resolves to the redirect response
     * @see #back()
     */
    InertiaRedirect back(int status, Map<String, String> headers);

    /**
     * Redirect back with an explicit status code, response headers and
     * fallback URL.
     *
     * @param status   the HTTP status of the redirect response
     * @param headers  custom headers applied to the redirect response
     * @param fallback the URL used when no {@code Referer} header is present
     * @return a chainable redirect; resolves to the redirect response
     * @see #back()
     */
    InertiaRedirect back(int status, Map<String, String> headers, String fallback);

    // ---------------------------------------------------------------------
    // Response headers
    // ---------------------------------------------------------------------

    /**
     * Set a custom header on every response of the current request (page
     * JSON, HTML document, redirects and 409 conflicts).
     *
     * @param name  the header name
     * @param value the header value
     */
    void header(String name, Object value);

    /**
     * Set several custom headers on every response of the current request.
     *
     * @param headers the headers to apply
     * @see #header(String, Object)
     */
    void headers(Map<String, Object> headers);

    // ---------------------------------------------------------------------
    // Shared props
    // ---------------------------------------------------------------------

    /**
     * Expose all public getters of the given instance as page props when
     * {@link #render(String)} is called without explicit props (equivalent of
     * {@code inertia-rails} {@code use_inertia_instance_props} / Rails
     * {@code view_assigns}).
     *
     * @param instance the bean whose getters become props
     */
    void shareInstanceProps(Object instance);

    /**
     * Share a value with every page rendered in the current request.
     *
     * <p>Shared props are merged into every {@code render()} call and
     * typically carry global data such as the authenticated user, flash
     * notifications or CSRF tokens. They are removed by
     * {@link #flushShared()}.</p>
     *
     * @param key   the prop name
     * @param value the prop value
     * @see #always(String, Object)
     */
    void share(String key, Object value);

    /**
     * Share several values at once.
     *
     * @param values the props to share
     * @see #share(String, Object)
     */
    void share(Map<String, Object> values);

    /**
     * Wrap a raw JSON string as a prop value that is embedded verbatim in the
     * page object, without being re-serialized.
     *
     * @param json the raw JSON document
     * @return a wrapper usable as a prop value
     */
    RawJson rawJson(String json);

    /**
     * Share a prop that is always included in every response of the current
     * request, including partial reloads and after {@link #flushShared()}
     * (equivalent of Laravel's {@code Inertia::always()}).
     *
     * @param key   the prop name
     * @param value the prop value
     */
    void always(String key, Object value);

    // ---------------------------------------------------------------------
    // Flash data
    // ---------------------------------------------------------------------

    /**
     * Flash a value that is persisted in the session and automatically merged
     * into the {@code props} of the next rendered page, then consumed.
     *
     * <p>This is the mechanism behind success messages and validation errors:
     * flash {@code "errors"} and the next render exposes it as
     * {@code props.errors} (the same contract used by the official adapters).
     * Prefer {@code InertiaRedirect.with(...)} / {@code .withErrors(...)} when
     * redirecting.</p>
     *
     * @param key   the flash key (e.g. {@code "success"}, {@code "errors"})
     * @param value the flash value
     */
    void flash(String key, Object value);

    /**
     * Flash several values at once.
     *
     * @param values the flash entries
     * @see #flash(String, Object)
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

    // ---------------------------------------------------------------------
    // Deferred / optional / cached props
    // ---------------------------------------------------------------------

    /**
     * Register a prop that is resolved asynchronously during a partial reload.
     *
     * <p>Deferred props are excluded from the initial (full) render and are
     * loaded afterwards by the client via a partial reload of the group
     * (inertia.js "deferred props" feature, equivalent of Laravel's
     * {@code Inertia::defer()}).</p>
     *
     * @param group    the deferred group name (e.g. {@code "default"})
     * @param name     the prop name
     * @param resolver a supplier resolving the prop value
     */
    void deferred(String group, String name, Supplier<Uni<Object>> resolver);

    /**
     * Register a deferred prop in the {@code "default"} group.
     *
     * @param name     the prop name
     * @param resolver a supplier resolving the prop value
     * @see #deferred(String, String, Supplier)
     */
    void deferred(String name, Supplier<Uni<Object>> resolver);

    /**
     * Register a deferred prop whose resolved value is cached.
     *
     * @param group    the deferred group name
     * @param name     the prop name
     * @param resolver a supplier resolving the prop value
     * @param cacheKey the cache key; when {@code null} or blank, no caching
     *                 is applied
     * @see #deferred(String, String, Supplier)
     */
    void deferred(String group, String name, Supplier<Uni<Object>> resolver, String cacheKey);

    /**
     * Register a deferred prop whose resolved value is cached for a TTL.
     *
     * @param group    the deferred group name
     * @param name     the prop name
     * @param resolver a supplier resolving the prop value
     * @param cacheKey the cache key; when {@code null} or blank, no caching
     *                 is applied
     * @param cacheTtl how long the cached value is considered fresh; when
     *                 {@code null}, the value is cached indefinitely
     * @see #deferred(String, String, Supplier)
     */
    void deferred(String group, String name, Supplier<Uni<Object>> resolver, String cacheKey,
            Duration cacheTtl);

    /**
     * Register a prop that is only included on partial reloads when the
     * client explicitly requests it (equivalent of Laravel's
     * {@code Inertia::optional()}).
     *
     * @param key      the prop name
     * @param resolver a supplier resolving the prop value
     */
    void optional(String key, Supplier<Uni<Object>> resolver);

    /**
     * Register an optional prop with a cached resolver.
     *
     * @param key      the prop name
     * @param resolver a supplier resolving the prop value
     * @param cacheKey the cache key; when {@code null} or blank, no caching
     *                 is applied
     * @see #optional(String, Supplier)
     */
    void optional(String key, Supplier<Uni<Object>> resolver, String cacheKey);

    /**
     * Register an optional prop with a cached resolver and TTL.
     *
     * @param key      the prop name
     * @param resolver a supplier resolving the prop value
     * @param cacheKey the cache key; when {@code null} or blank, no caching
     *                 is applied
     * @param cacheTtl how long the cached value is considered fresh; when
     *                 {@code null}, the value is cached indefinitely
     * @see #optional(String, Supplier)
     */
    void optional(String key, Supplier<Uni<Object>> resolver, String cacheKey, Duration cacheTtl);

    /**
     * Resolve a prop value through a cache, without sharing semantics.
     *
     * <p>The value is produced by the resolver once and reused for subsequent
     * requests within the cache lifetime.</p>
     *
     * @param key      the prop name
     * @param resolver a supplier resolving the prop value
     */
    void cache(String key, Supplier<Uni<Object>> resolver);

    /**
     * Resolve a prop value through a cache with an explicit TTL.
     *
     * @param key      the prop name
     * @param ttl      how long the cached value is considered fresh
     * @param resolver a supplier resolving the prop value
     * @see #cache(String, Supplier)
     */
    void cache(String key, Duration ttl, Supplier<Uni<Object>> resolver);

    // ---------------------------------------------------------------------
    // Once props
    // ---------------------------------------------------------------------

    /**
     * Include a prop in the page object only if the client does not already
     * have it.
     *
     * <p>The client reports the once-props it has in its history, so values
     * such as flash notifications are not shown again after a back/forward
     * navigation. The prop is included in the next response, and the client
     * is told to track it for subsequent visits.</p>
     *
     * @param key   the prop name
     * @param value the prop value
     */
    void once(String key, Object value);

    /**
     * Include a once prop under a client-tracking key that differs from the
     * prop name.
     *
     * @param key       the prop name
     * @param value     the prop value
     * @param customKey the key used to track the prop client-side
     * @see #once(String, Object)
     */
    void once(String key, Object value, String customKey);

    /**
     * Register a lazily resolved once prop.
     *
     * @param key      the prop name
     * @param resolver a supplier resolving the prop value
     * @see #once(String, Object)
     */
    void once(String key, Supplier<Uni<Object>> resolver);

    /**
     * Register a lazily resolved once prop with a custom tracking key.
     *
     * @param key       the prop name
     * @param resolver  a supplier resolving the prop value
     * @param customKey the key used to track the prop client-side
     * @see #once(String, Object)
     */
    void once(String key, Supplier<Uni<Object>> resolver, String customKey);

    /**
     * Register a lazily resolved once prop that expires at a given instant.
     *
     * @param key       the prop name
     * @param resolver  a supplier resolving the prop value
     * @param customKey the key used to track the prop client-side
     * @param expiresAt the instant after which the client no longer reports
     *                  the prop as present
     * @see #once(String, Object)
     */
    void once(String key, Supplier<Uni<Object>> resolver, String customKey, Instant expiresAt);

    /**
     * Share a value and also register it as a once prop.
     *
     * <p>The prop is part of the shared props (available on every render) and
     * additionally tracked client-side like a once prop.</p>
     *
     * @param key   the prop name
     * @param value the prop value
     */
    void shareOnce(String key, Object value);

    /**
     * Share a lazily resolved value and register it as a once prop.
     *
     * @param key      the prop name
     * @param resolver a supplier resolving the prop value
     * @see #shareOnce(String, Object)
     */
    void shareOnce(String key, Supplier<Uni<Object>> resolver);

    // ---------------------------------------------------------------------
    // Merge / prepend props
    // ---------------------------------------------------------------------

    /**
     * Mark a prop as mergeable and set its value.
     *
     * <p>On partial reloads the client merges the new value into the existing
     * one instead of replacing it (inertia.js "merge props", equivalent of
     * Laravel's {@code Inertia::merge()}).</p>
     *
     * @param key   the prop name
     * @param value the prop value
     */
    void merge(String key, Object value);

    /**
     * Mark a prop as mergeable, optionally with deep merge semantics.
     *
     * @param key   the prop name
     * @param value the prop value
     * @param deep  whether nested objects are merged recursively instead of
     *              replaced
     * @see #merge(String, Object)
     */
    void merge(String key, Object value, boolean deep);

    /**
     * Mark a prop as mergeable with deep merge semantics and list-based
     * matching.
     *
     * @param key     the prop name
     * @param value   the prop value
     * @param deep    whether nested objects are merged recursively
     * @param matchOn field paths (dot notation, relative to the prop) used to
     *                match elements of arrays/lists during the merge
     * @see #merge(String, Object)
     */
    void merge(String key, Object value, boolean deep, String... matchOn);

    /**
     * Mark a prop as prepended on partial reloads and set its value.
     *
     * <p>Like a merge prop, but the new value is prepended to the existing
     * list instead of merged by key.</p>
     *
     * @param key   the prop name
     * @param value the prop value
     */
    void prepend(String key, Object value);

    // ---------------------------------------------------------------------
    // Scroll props
    // ---------------------------------------------------------------------

    /**
     * Register a prop as a "scroll" prop with only metadata.
     *
     * <p>Scroll props (equivalent of Laravel's {@code Inertia::scroll()}) let
     * the client restore scroll state for the prop's container while the
     * value is being loaded.</p>
     *
     * @param key      the prop name
     * @param metadata the scroll metadata (e.g. the wrapper element selector)
     */
    void scroll(String key, Map<String, Object> metadata);

    /**
     * Register a scroll prop with a value and metadata.
     *
     * @param key      the prop name
     * @param value    the prop value
     * @param metadata the scroll metadata
     * @see #scroll(String, Map)
     */
    void scroll(String key, Object value, Map<String, Object> metadata);

    /**
     * Register a scroll prop with a value, metadata and an explicit wrapper
     * element name.
     *
     * @param key      the prop name
     * @param value    the prop value
     * @param metadata the scroll metadata
     * @param wrapper  the element (default {@code "data"}) that wraps the prop
     *                 in the frontend markup
     * @see #scroll(String, Map)
     */
    void scroll(String key, Object value, Map<String, Object> metadata, String wrapper);

    // ---------------------------------------------------------------------
    // Error handling
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
     * <p>For Inertia requests, the mapper's result is used as the response;
     * for regular requests the error falls back to the default JAX-RS
     * handling.</p>
     *
     * @param mapper the error mapper; {@code null} resets to the default
     */
    void handleErrorUsing(ErrorMapper mapper);

    // ---------------------------------------------------------------------
    // Page metadata / history
    // ---------------------------------------------------------------------

    /**
     * Add a metadata entry to the page object.
     *
     * <p>Metadata travels with the page response (e.g. document title or
     * custom attributes) and is available to the frontend adapter.</p>
     *
     * @param key   the metadata key
     * @param value the metadata value
     */
    void meta(String key, Object value);

    /**
     * Add several metadata entries to the page object.
     *
     * @param values the metadata entries
     * @see #meta(String, Object)
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
    // Root view / SSR
    // ---------------------------------------------------------------------

    /**
     * Override the root view (the server-side HTML template) for the current
     * request.
     *
     * @param name the template name, e.g. {@code "app.html"}
     */
    void setRootView(String name);

    /**
     * Disable server-side rendering for the given paths for the current
     * request.
     *
     * @param paths the URL paths excluded from SSR
     */
    void withoutSsr(String... paths);

    /**
     * Disable server-side rendering entirely for the current request.
     */
    void disableSsr();

    // ---------------------------------------------------------------------
    // Shared data introspection
    // ---------------------------------------------------------------------

    /**
     * Return all currently shared props.
     *
     * @return an unmodifiable map of the shared props
     * @see #share(String, Object)
     */
    Map<String, Object> getShared();

    /**
     * Return a single shared prop, or a default value.
     *
     * @param key          the prop name
     * @param defaultValue returned when the prop is not shared
     * @return the shared value or {@code defaultValue}
     */
    Object getShared(String key, Object defaultValue);

    /**
     * Remove all currently shared props.
     *
     * <p>Props registered with {@link #always(String, Object)} survive the
     * flush; props registered with {@code share(...)} are removed.</p>
     */
    void flushShared();

    // ---------------------------------------------------------------------
    // Versioning
    // ---------------------------------------------------------------------

    /**
     * Return the current asset version used for cache-busting.
     *
     * <p>The client compares this value on every request; when it changes,
     * the page is fully reloaded instead of doing a partial visit.</p>
     *
     * @return the current version string
     */
    String getVersion();

    /**
     * Override the asset version for the current request.
     *
     * @param version the new version value
     * @see #getVersion()
     */
    void version(String version);
}
