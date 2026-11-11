package io.github.diovamny.spring.inertia.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

import io.github.diovamny.inertia.core.model.DeferredProp;
import io.github.diovamny.inertia.core.model.LivePropListener;
import io.github.diovamny.inertia.core.model.PageObject;
import io.github.diovamny.inertia.core.protocol.PartialFilter;
import io.github.diovamny.inertia.core.spi.FlashStore;
import io.github.diovamny.spring.inertia.internal.InertiaRequestContext;
import io.github.diovamny.spring.inertia.protocol.InertiaHeaderExtractor;
import io.github.diovamny.spring.inertia.protocol.LivePropRegistry;
import io.github.diovamny.spring.inertia.protocol.PageObjectBuilder;
import io.github.diovamny.spring.inertia.protocol.ResponseProcessor;
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
    private final PageObject page;
    private final int status;
    private final ResponseProcessor responseProcessor;

    public InertiaRender(InertiaResponse response, FlashStore flashStore) {
        this(response, flashStore, null);
    }

    public InertiaRender(InertiaResponse response, FlashStore flashStore,
            io.github.diovamny.spring.inertia.renderer.SsrCachePolicy ssrCachePolicy) {
        this(response, flashStore, ssrCachePolicy, null, 200, null);
    }

    public InertiaRender(InertiaResponse response, FlashStore flashStore,
            io.github.diovamny.spring.inertia.renderer.SsrCachePolicy ssrCachePolicy,
            PageObject page, int status, ResponseProcessor responseProcessor) {
        super(response.getStatusCode(), copyHeaders(response), response.getBody());
        this.flashStore = flashStore;
        this.ssrCachePolicy = ssrCachePolicy;
        this.page = page;
        this.status = status;
        this.responseProcessor = responseProcessor;
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
        this.page = null;
        this.status = status != null ? status.value() : 200;
        this.responseProcessor = null;
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

    /**
     * Preserve 64-bit integer precision for this response only, overriding
     * the global {@code inertia.bigint-enabled} setting. The response body is
     * re-serialized in place; unsafe values travel as
     * {@code {"$bigint":"..."}} markers and the page carries
     * {@code preserveBigIntegers: true}.
     *
     * @param preserve {@code true} to opt in, {@code false} to opt out
     * @return the re-serialized render for chaining
     */
    public InertiaRender preserveBigIntegers(boolean preserve) {
        InertiaRequestContext.set(PageObjectBuilder.CONTEXT_PRESERVE_BIG_INTEGERS, preserve);
        if (page != null && responseProcessor != null) {
            var reserialized = responseProcessor.serialize(page, status);
            return new InertiaRender(reserialized, flashStore, ssrCachePolicy,
                page, status, responseProcessor);
        }
        return this;
    }

    /**
     * Alias of {@link #preserveBigIntegers(boolean)} with {@code true}.
     *
     * @return the re-serialized render for chaining
     */
    public InertiaRender preserveBigIntegers() {
        return preserveBigIntegers(true);
    }

    /**
     * Register a live prop after rendering, mirroring the fluent
     * {@code Inertia::render(...)->live(...)} API.
     *
     * <p>The resolver is evaluated immediately unless the prop was also
     * registered as deferred or the current partial reload does not select
     * it. The page is re-serialized so the final JSON contains both
     * {@code props} and {@code liveProps}.</p>
     *
     * @param key       the prop name
     * @param resolver  produces the current value
     * @param listeners channels and events that trigger live updates
     * @return a re-serialized render for chaining
     */
    public InertiaRender live(String key, Supplier<Object> resolver,
            LivePropListener... listeners) {
        return live(key, resolver, null, listeners);
    }

    /**
     * Register a live prop with a minimum update interval after rendering.
     *
     * @param key        the prop name
     * @param resolver   produces the current value
     * @param throttleMs minimum interval in milliseconds between updates
     * @param listeners  channels and events that trigger live updates
     * @return a re-serialized render for chaining
     */
    public InertiaRender live(String key, Supplier<Object> resolver, int throttleMs,
            LivePropListener... listeners) {
        if (throttleMs < 0) {
            throw new IllegalArgumentException("live prop throttle must not be negative");
        }
        return live(key, resolver, Integer.valueOf(throttleMs), listeners);
    }

    private InertiaRender live(String key, Supplier<Object> resolver, Integer throttleMs,
            LivePropListener... listeners) {
        LivePropRegistry.register(key, resolver, throttleMs, listeners);
        if (page == null || responseProcessor == null) {
            return this;
        }
        var updated = page;
        if (!LivePropRegistry.isDeferred(key)
                && !LivePropRegistry.isResolved(key)
                && isSelectedOnChain(key)) {
            var props = new LinkedHashMap<>(page.props());
            props.put(key, resolver.get());
            LivePropRegistry.markResolved(key);
            updated = updated.withProps(props);
        }
        return reserialize(updated.withLiveProps(LivePropRegistry.metadata()));
    }

    /**
     * Register a deferred prop after rendering, composable with
     * {@link #live(String, Supplier, LivePropListener...)} to mirror
     * Laravel's {@code Inertia::defer(...)->live()}.
     *
     * <p>On a full visit the prop is removed from the page props and grouped
     * under {@code deferredProps}; on a partial reload that selects it, the
     * resolver runs and the value is included.</p>
     *
     * @param group    the deferred group name
     * @param key      the prop name
     * @param resolver produces the prop value on partial reload
     * @return a re-serialized render for chaining
     */
    public InertiaRender defer(String group, String key, Supplier<Object> resolver) {
        if (group == null || group.isBlank()) {
            throw new IllegalArgumentException("deferred group must not be blank");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("deferred key must not be blank");
        }
        if (resolver == null) {
            throw new IllegalArgumentException("deferred resolver must not be null");
        }
        var deferred = deferredRegistry();
        deferred.put(key, new DeferredProp<>(group, key, resolver));
        InertiaRequestContext.set(PageObjectBuilder.CONTEXT_DEFERRED, deferred);

        if (page == null || responseProcessor == null) {
            return this;
        }
        var partial = isPartialOnChain();
        var props = new LinkedHashMap<>(page.props());
        props.remove(key);
        if (partial && isSelectedOnChain(key)) {
            props.put(key, resolver.get());
            LivePropRegistry.markResolved(key);
        }

        Map<String, List<String>> groups = new LinkedHashMap<>();
        if (page.deferredProps() != null) {
            page.deferredProps().forEach((name, members) ->
                groups.put(name, new ArrayList<>(members)));
        }
        groups.computeIfAbsent(group, name -> new ArrayList<>()).add(key);

        var updated = page.withProps(props)
            .withDeferredProps(partial ? page.deferredProps() : groups);
        return reserialize(updated);
    }

    private InertiaRender reserialize(PageObject updated) {
        var reserialized = responseProcessor.serialize(updated, status);
        return new InertiaRender(reserialized, flashStore, ssrCachePolicy,
            updated, status, responseProcessor);
    }

    private boolean isPartialOnChain() {
        var partialComponent = InertiaRequestContext.get(
            InertiaHeaderExtractor.CONTEXT_PARTIAL_COMPONENT);
        return PartialFilter.matchesComponent(
            partialComponent != null ? String.valueOf(partialComponent) : null,
            page != null ? page.component() : null);
    }

    private boolean isSelectedOnChain(String key) {
        if (!isPartialOnChain()) {
            return true;
        }
        Set<String> only = PartialFilter.parseCsv(stringAttr(
            InertiaHeaderExtractor.CONTEXT_PARTIAL_DATA));
        Set<String> except = PartialFilter.parseCsv(stringAttr(
            InertiaHeaderExtractor.CONTEXT_PARTIAL_EXCEPT));
        if (PartialFilter.isExcluded(key, except)) {
            return false;
        }
        return only.isEmpty() || PartialFilter.isSelected(key, only);
    }

    private static String stringAttr(String name) {
        var value = InertiaRequestContext.get(name);
        return value != null ? String.valueOf(value) : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, DeferredProp<Object>> deferredRegistry() {
        var stored = InertiaRequestContext.get(PageObjectBuilder.CONTEXT_DEFERRED);
        if (stored instanceof Map<?, ?> map) {
            return (Map<String, DeferredProp<Object>>) map;
        }
        return new LinkedHashMap<>();
    }

    private static HttpHeaders copyHeaders(InertiaResponse response) {
        var headers = new HttpHeaders();
        headers.putAll(response.getHeaders());
        return headers;
    }
}
