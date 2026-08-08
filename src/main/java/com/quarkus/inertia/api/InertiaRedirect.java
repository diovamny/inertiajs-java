package com.quarkus.inertia.api;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
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

import com.quarkus.inertia.spi.FlashStore;

/**
 * A redirect response that can be chained with flash data before being
 * returned, mirroring Laravel's
 * {@code Redirect::back()->with(...)->withErrors(...)}.
 * <p>
 * Returned by {@link Inertia#back()} and {@link Inertia#redirect(String)},
 * so both are chainable with {@link #with(String, Object)},
 * {@link #withErrors(Map)} and {@link #withInput(Map)}.
 * <p>
 * Implements {@link Uni} by delegation, so existing callers that return
 * {@code inertia.back()} or {@code inertia.redirect(url)} directly keep
 * working unchanged.
 */
public class InertiaRedirect implements Uni<Object> {

    private final Uni<Object> delegate;
    private final FlashStore flashStore;

    public InertiaRedirect(Uni<Object> delegate, FlashStore flashStore) {
        this.delegate = delegate;
        this.flashStore = flashStore;
    }

    /**
     * Flash a single key/value pair, equivalent to Laravel's {@code ->with($key, $value)}.
     */
    public InertiaRedirect with(String key, Object value) {
        flashStore.put(key, value);
        return this;
    }

    /**
     * Flash several key/value pairs at once.
     */
    public InertiaRedirect with(Map<String, Object> values) {
        flashStore.putAll(values);
        return this;
    }

    /**
     * Alias of {@link #with(String, Object)}.
     */
    public InertiaRedirect flash(String key, Object value) {
        return with(key, value);
    }

    /**
     * Alias of {@link #with(Map)}.
     */
    public InertiaRedirect flash(Map<String, Object> values) {
        return with(values);
    }

    /**
     * Flash validation errors, equivalent to Laravel's {@code ->withErrors([...])}.
     */
    public InertiaRedirect withErrors(Map<String, String> errors) {
        return with("errors", errors);
    }

    /**
     * Flash the submitted form data so a full-page reload can repopulate the
     * form, equivalent to Laravel's {@code ->withInput()}.
     */
    public InertiaRedirect withInput(Map<String, Object> input) {
        return with("input", input);
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
