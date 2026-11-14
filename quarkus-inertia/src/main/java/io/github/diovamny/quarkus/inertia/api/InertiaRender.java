package io.github.diovamny.quarkus.inertia.api;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniAwait;
import io.smallrye.mutiny.groups.UniConvert;
import io.smallrye.mutiny.groups.UniIfNoItem;
import io.smallrye.mutiny.groups.UniMemoize;
import io.smallrye.mutiny.groups.UniOnCancel;
import io.smallrye.mutiny.groups.UniOnFailure;
import io.smallrye.mutiny.groups.UniOnItem;
import io.smallrye.mutiny.groups.UniOnItemOrFailure;
import io.smallrye.mutiny.groups.UniOnSubscribe;
import io.smallrye.mutiny.groups.UniOnTerminate;
import io.smallrye.mutiny.groups.UniRepeat;
import io.smallrye.mutiny.groups.UniSubscribe;

import io.github.diovamny.inertia.core.spi.FlashStore;
import io.github.diovamny.quarkus.inertia.protocol.DeferredPropRegistry;
import io.github.diovamny.quarkus.inertia.protocol.LivePropRegistry;

/**
 * A rendered page that can be chained with flash data before being returned,
 * mirroring Laravel's {@code Inertia::render(...)->flash(...)}.
 *
 * <p>Returned by {@link Inertia#render(String, Map)}, so renders are chainable
 * with {@link #flash(String, Object)}, {@link #flash(Map)} and
 * {@link #withErrors(Map)}. Flash entries are stored immediately in the
 * session {@link FlashStore} and merged into the props of the <em>next</em>
 * rendered page, then consumed.
 *
 * <p>Implements {@link Uni} by delegation, so existing callers that return
 * {@code inertia.render(...)} directly keep working unchanged.
 */
public class InertiaRender implements Uni<Object> {

    private final Uni<Object> delegate;
    private final FlashStore flashStore;
    private final io.github.diovamny.quarkus.inertia.renderer.SsrCachePolicy ssrCachePolicy;

    public InertiaRender(Uni<Object> delegate, FlashStore flashStore) {
        this(delegate, flashStore, null);
    }

    public InertiaRender(Uni<Object> delegate, FlashStore flashStore,
            io.github.diovamny.quarkus.inertia.renderer.SsrCachePolicy ssrCachePolicy) {
        this.delegate = delegate;
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
        return flash("errors", io.github.diovamny.quarkus.inertia.support.ErrorBags.wrap(errors));
    }

    /**
     * Flash multiple messages per field (Inertia {@code withAllErrors} parity).
     *
     * @param errors immutable multi-message bag
     * @return this render for chaining
     */
    public InertiaRender withValidationErrors(
            io.github.diovamny.inertia.core.model.ValidationErrors errors) {
        return flash("errors", io.github.diovamny.quarkus.inertia.support.ErrorBags.wrap(
            errors != null ? errors.toWireMap(true) : Map.of()));
    }

    /**
     * Flash multiple messages per field from a plain multimap.
     *
     * @param errors field-to-messages multimap
     * @return this render for chaining
     */
    public InertiaRender withErrorMessages(Map<String, ? extends java.util.Collection<String>> errors) {
        return flash("errors", io.github.diovamny.quarkus.inertia.support.ErrorBags.wrap(
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
     * Register a live prop after rendering, mirroring the fluent
     * {@code Inertia::render(...)->live(...)} API.
     *
     * <p>The page assembly of the reactive pipeline is lazy, so the
     * registration is visible when the returned {@link Uni} is subscribed:
     * the resolver runs on a full visit and on a partial reload that selects
     * the prop, and its metadata travels in {@code liveProps}.</p>
     *
     * @param key       the prop name
     * @param resolver  produces the current value asynchronously
     * @param listeners channels and events that trigger live updates
     * @return this render for chaining
     */
    public InertiaRender live(String key, Supplier<Uni<Object>> resolver,
            io.github.diovamny.inertia.core.model.LivePropListener... listeners) {
        LivePropRegistry.register(key, resolver, null, listeners);
        return this;
    }

    /**
     * Register a live prop with a minimum update interval after rendering.
     *
     * @param key        the prop name
     * @param resolver   produces the current value asynchronously
     * @param throttleMs minimum interval in milliseconds between updates
     * @param listeners  channels and events that trigger live updates
     * @return this render for chaining
     */
    public InertiaRender live(String key, Supplier<Uni<Object>> resolver, int throttleMs,
            io.github.diovamny.inertia.core.model.LivePropListener... listeners) {
        if (throttleMs < 0) {
            throw new IllegalArgumentException("live prop throttle must not be negative");
        }
        LivePropRegistry.register(key, resolver, throttleMs, listeners);
        return this;
    }

    /**
     * Register a deferred prop after rendering, composable with
     * {@link #live(String, Supplier, io.github.diovamny.inertia.core.model.LivePropListener...)}
     * to mirror Laravel's {@code Inertia::defer(...)->live()}.
     *
     * <p>On a full visit the prop is grouped under {@code deferredProps}; on
     * a partial reload that selects it, the resolver runs and the value is
     * included.</p>
     *
     * @param group    the deferred group name
     * @param key      the prop name
     * @param resolver produces the prop value on partial reload
     * @return this render for chaining
     */
    public InertiaRender defer(String group, String key, Supplier<Uni<Object>> resolver) {
        DeferredPropRegistry.register(group, key, resolver);
        return this;
    }

    /** {@inheritDoc} */

    @Override
    public UniSubscribe<Object> subscribe() {
        return delegate.subscribe();
    }

    /** {@inheritDoc} */

    @Override
    public UniAwait<Object> await() {
        return delegate.await();
    }

    /** {@inheritDoc} */

    @Override
    public UniOnItem<Object> onItem() {
        return delegate.onItem();
    }

    /** {@inheritDoc} */

    @Override
    public UniOnSubscribe<Object> onSubscription() {
        return delegate.onSubscription();
    }

    /** {@inheritDoc} */

    @Override
    public UniOnItemOrFailure<Object> onItemOrFailure() {
        return delegate.onItemOrFailure();
    }

    /** {@inheritDoc} */

    @Override
    public UniOnFailure<Object, Throwable> onFailure() {
        return delegate.onFailure();
    }

    /** {@inheritDoc} */

    @Override
    public UniOnFailure<Object, Throwable> onFailure(Predicate<? super Throwable> predicate) {
        return delegate.onFailure(predicate);
    }

    /** {@inheritDoc} */

    @Override
    public <E extends Throwable> UniOnFailure<Object, E> onFailure(Class<E> typeOfFailure) {
        return delegate.onFailure(typeOfFailure);
    }

    /** {@inheritDoc} */

    @Override
    public UniIfNoItem<Object> ifNoItem() {
        return delegate.ifNoItem();
    }

    /** {@inheritDoc} */

    @Override
    public Uni<Object> emitOn(Executor executor) {
        return delegate.emitOn(executor);
    }

    /** {@inheritDoc} */

    @Override
    public Uni<Object> runSubscriptionOn(Executor executor) {
        return delegate.runSubscriptionOn(executor);
    }

    /** {@inheritDoc} */

    @Override
    public UniMemoize<Object> memoize() {
        return delegate.memoize();
    }

    /** {@inheritDoc} */

    @Override
    public UniConvert<Object> convert() {
        return delegate.convert();
    }

    /** {@inheritDoc} */

    @Override
    public Multi<Object> toMulti() {
        return delegate.toMulti();
    }

    /** {@inheritDoc} */

    @Override
    public UniRepeat<Object> repeat() {
        return delegate.repeat();
    }

    /** {@inheritDoc} */

    @Override
    public UniOnTerminate<Object> onTermination() {
        return delegate.onTermination();
    }

    /** {@inheritDoc} */

    @Override
    public UniOnCancel<Object> onCancellation() {
        return delegate.onCancellation();
    }

    /** {@inheritDoc} */

    @Override
    public Uni<Object> log(String identifier) {
        return delegate.log(identifier);
    }

    /** {@inheritDoc} */

    @Override
    public Uni<Object> log() {
        return delegate.log();
    }
}
