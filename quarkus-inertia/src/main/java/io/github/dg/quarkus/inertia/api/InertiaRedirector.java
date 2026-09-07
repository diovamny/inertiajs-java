package io.github.dg.quarkus.inertia.api;

import java.util.Map;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.ws.rs.core.Response;

/**
 * Specialized redirect interface for Inertia.js responses in Quarkus.
 *
 * <p>Handles internal client-side redirects (HTTP 303 for PUT/PATCH/DELETE),
 * full-page browser reloads, external redirects (HTTP 409 Conflict with
 * {@code X-Inertia-Location}), and referer-based {@code back()} navigation.</p>
 */
public interface InertiaRedirector {

    // ---------------------------------------------------------------------
    // Reactive Redirects
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
     */
    InertiaRedirect back(String fallback);

    /**
     * Redirect back with an explicit status code and fallback URL.
     *
     * @param status   the HTTP status of the redirect response
     * @param fallback the URL used when no {@code Referer} header is present
     * @return a chainable redirect; resolves to the redirect response
     */
    InertiaRedirect back(int status, String fallback);

    /**
     * Redirect back with an explicit status code and response headers.
     *
     * @param status  the HTTP status of the redirect response
     * @param headers custom headers applied to the redirect response
     * @return a chainable redirect; resolves to the redirect response
     */
    InertiaRedirect back(int status, Map<String, String> headers);

    /**
     * Redirect back with an explicit status code, response headers and fallback URL.
     *
     * @param status   the HTTP status of the redirect response
     * @param headers  custom headers applied to the redirect response
     * @param fallback the URL used when no {@code Referer} header is present
     * @return a chainable redirect; resolves to the redirect response
     */
    InertiaRedirect back(int status, Map<String, String> headers, String fallback);

    // ---------------------------------------------------------------------
    // Synchronous JAX-RS Redirects
    // ---------------------------------------------------------------------

    /**
     * Redirect to an internal URL and return a synchronous JAX-RS Response.
     *
     * @param url the destination URL
     * @return a JAX-RS Response
     */
    Response redirectSync(String url);

    /**
     * Redirect to an internal URL, optionally forcing a full page load.
     *
     * @param url      the destination URL
     * @param fullPage whether to force a full browser page load
     * @return a JAX-RS Response
     */
    Response redirectSync(String url, boolean fullPage);

    /**
     * Redirect back to the previous page.
     *
     * @return a JAX-RS Response
     */
    Response backSync();

    /**
     * Redirect back to the previous page, falling back to the given URL.
     *
     * @param fallback the URL used when no Referer header is present
     * @return a JAX-RS Response
     */
    Response backSync(String fallback);

    /**
     * Redirect back with an explicit status code and fallback URL.
     *
     * @param status   the HTTP status
     * @param fallback the URL used when no Referer header is present
     * @return a JAX-RS Response
     */
    Response backSync(int status, String fallback);

    /**
     * Redirect back with an explicit status code and response headers.
     *
     * @param status  the HTTP status
     * @param headers response headers
     * @return a JAX-RS Response
     */
    Response backSync(int status, Map<String, String> headers);

    /**
     * Redirect back with an explicit status code, response headers and fallback URL.
     *
     * @param status   the HTTP status
     * @param headers  response headers
     * @param fallback the URL used when no Referer header is present
     * @return a JAX-RS Response
     */
    Response backSync(int status, Map<String, String> headers, String fallback);

    // ---------------------------------------------------------------------
    // Vert.x Reactive Routes Redirects
    // ---------------------------------------------------------------------

    /**
     * Redirect to a URL directly in a Vert.x {@link RoutingContext}.
     *
     * @param rc  the routing context
     * @param url the target URL
     */
    void redirectVertx(RoutingContext rc, String url);

    /**
     * Redirect to a URL, optionally forcing a full page load, in a Vert.x {@link RoutingContext}.
     *
     * @param rc       the routing context
     * @param url      the target URL
     * @param fullPage whether to force a full browser page load
     */
    void redirectVertx(RoutingContext rc, String url, boolean fullPage);

    /**
     * Redirect back to the previous page in a Vert.x {@link RoutingContext}.
     *
     * @param rc the routing context
     */
    void backVertx(RoutingContext rc);

    /**
     * Redirect back to the previous page with a fallback URL in a Vert.x {@link RoutingContext}.
     *
     * @param rc       the routing context
     * @param fallback the URL used when no Referer header is present
     */
    void backVertx(RoutingContext rc, String fallback);
}