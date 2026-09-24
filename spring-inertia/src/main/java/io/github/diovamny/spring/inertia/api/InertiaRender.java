package io.github.diovamny.spring.inertia.api;

import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

import io.github.diovamny.inertia.core.spi.FlashStore;
import io.github.diovamny.spring.inertia.support.ErrorBags;

/**
 * A rendered page that can be chained with flash data before being returned,
 * mirroring Laravel's {@code Inertia::render(...)->flash(...)}.
 *
 * <p>Extends {@link InertiaResponse} so Spring MVC serves it natively.
 * Flash entries are stored immediately in the session {@link FlashStore} and
 * merged into the props of the <em>next</em> rendered page, then consumed.
 */
public class InertiaRender extends InertiaResponse {

    private final FlashStore flashStore;
    private final io.github.diovamny.spring.inertia.renderer.SsrCachePolicy ssrCachePolicy;

    public InertiaRender(InertiaResponse response, FlashStore flashStore) {
        this(response, flashStore, null);
    }

    public InertiaRender(InertiaResponse response, FlashStore flashStore,
            io.github.diovamny.spring.inertia.renderer.SsrCachePolicy ssrCachePolicy) {
        super(response.getStatusCode(), copyHeaders(response), response.getBody());
        this.flashStore = flashStore;
        this.ssrCachePolicy = ssrCachePolicy;
    }

    public InertiaRender(HttpStatusCode status, HttpHeaders headers, String body,
            FlashStore flashStore) {
        this(status, headers, body, flashStore, null);
    }

    public InertiaRender(HttpStatusCode status, HttpHeaders headers, String body,
            FlashStore flashStore,
            io.github.diovamny.spring.inertia.renderer.SsrCachePolicy ssrCachePolicy) {
        super(status, headers, body);
        this.flashStore = flashStore;
        this.ssrCachePolicy = ssrCachePolicy;
    }

    /**
     * Flash a single key/value pair for the next rendered page.
     */
    public InertiaRender flash(String key, Object value) {
        flashStore.put(key, value);
        return this;
    }

    /**
     * Flash several key/value pairs for the next rendered page.
     */
    public InertiaRender flash(Map<String, Object> values) {
        flashStore.putAll(values);
        return this;
    }

    /**
     * Alias of {@link #flash(String, Object)}.
     */
    public InertiaRender with(String key, Object value) {
        return flash(key, value);
    }

    /**
     * Alias of {@link #flash(Map)}.
     */
    public InertiaRender with(Map<String, Object> values) {
        return flash(values);
    }

    /**
     * Flash validation errors under the {@code errors} key (one message per field).
     */
    public InertiaRender withErrors(Map<String, String> errors) {
        return flash("errors", ErrorBags.wrap(errors));
    }

    /**
     * Flash multiple messages per field (Inertia {@code withAllErrors} parity).
     *
     * @param errors immutable multi-message bag
     * @return this render for chaining
     */
    public InertiaRender withValidationErrors(
            io.github.diovamny.inertia.core.model.ValidationErrors errors) {
        return flash("errors", ErrorBags.wrap(errors != null ? errors.toWireMap(true) : Map.of()));
    }

    /**
     * Flash multiple messages per field from a plain multimap.
     *
     * @param errors field-to-messages multimap
     * @return this render for chaining
     */
    public InertiaRender withErrorMessages(Map<String, ? extends java.util.Collection<String>> errors) {
        return flash("errors", ErrorBags.wrap(
            io.github.diovamny.inertia.core.model.ValidationErrors.ofLists(errors).toWireMap(true)));
    }

    /**
     * Cache this render's SSR response for the given TTL (Rails
     * {@code ssr_cache} parity). Only applies when the page is rendered
     * through the SSR sidecar; the cache key is the page-content hash.
     *
     * @param ttl how long the SSR response stays cached
     * @return this render for chaining
     */
    public InertiaRender enableSsrCache(java.time.Duration ttl) {
        if (ssrCachePolicy == null) {
            throw new IllegalStateException(
                "SSR response caching requires the request-scoped SsrCachePolicy");
        }
        ssrCachePolicy.setTtlMillis(ttl != null ? ttl.toMillis() : null);
        return this;
    }

    private static HttpHeaders copyHeaders(InertiaResponse response) {
        var headers = new HttpHeaders();
        headers.putAll(response.getHeaders());
        return headers;
    }
}
