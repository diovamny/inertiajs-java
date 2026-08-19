package io.github.dg.quarkus.inertia.internal;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;

import io.github.dg.quarkus.inertia.api.Inertia;
import io.github.dg.quarkus.inertia.api.InertiaRedirect;
import io.github.dg.quarkus.inertia.cache.CachedPropStore;
import io.github.dg.quarkus.inertia.config.InertiaConfig;
import io.github.dg.quarkus.inertia.model.AlwaysProp;
import io.github.dg.quarkus.inertia.model.RawJson;
import io.github.dg.quarkus.inertia.protocol.PageObjectBuilder;
import io.github.dg.quarkus.inertia.protocol.ResponseProcessor;
import io.github.dg.quarkus.inertia.protocol.SharedDataRegistry;
import io.github.dg.quarkus.inertia.protocol.RedirectProcessor;
import io.github.dg.quarkus.inertia.protocol.OncePropRegistry;
import io.github.dg.quarkus.inertia.protocol.MergePropProcessor;
import io.github.dg.quarkus.inertia.spi.FlashStore;
import io.github.dg.quarkus.inertia.spi.ErrorMapper;
import io.github.dg.quarkus.inertia.version.VersionProvider;

/**
 * Default {@link Inertia} implementation (request-scoped): forwards every
 * operation to the page builder, shared-data registry, redirect processor
 * and response processor that assemble the final page object.
 */
@RequestScoped
public class InertiaImpl implements Inertia {

    private final PageObjectBuilder pageBuilder;
    private final ResponseProcessor responseProcessor;
    private final SharedDataRegistry sharedData;
    private final RedirectProcessor redirectProcessor;
    private final VersionProvider versionProvider;
    private final OncePropRegistry oncePropRegistry;
    private final MergePropProcessor mergePropProcessor;
    private final FlashStore flashStore;
    private final CachedPropStore cachedPropStore;
    private final InertiaConfig config;

    @Inject
    public InertiaImpl(
            PageObjectBuilder pageBuilder,
            ResponseProcessor responseProcessor,
            SharedDataRegistry sharedData,
            RedirectProcessor redirectProcessor,
            VersionProvider versionProvider,
            OncePropRegistry oncePropRegistry,
            MergePropProcessor mergePropProcessor,
            FlashStore flashStore,
            CachedPropStore cachedPropStore,
            InertiaConfig config) {
        this.pageBuilder = pageBuilder;
        this.responseProcessor = responseProcessor;
        this.sharedData = sharedData;
        this.redirectProcessor = redirectProcessor;
        this.versionProvider = versionProvider;
        this.oncePropRegistry = oncePropRegistry;
        this.mergePropProcessor = mergePropProcessor;
        this.flashStore = flashStore;
        this.cachedPropStore = cachedPropStore;
        this.config = config;
    }

    @Override
    public Uni<Object> render(String component, Map<String, Object> props) {
        return pageBuilder.build(component, props)
            .chain(responseProcessor::process);
    }

    @Override
    public Uni<Object> render(String component) {
        return render(component, Map.of());
    }

    @Override
    public Uni<Object> render(Enum<?> component) {
        return render(component.name(), Map.of());
    }

    @Override
    public Uni<Object> render(Enum<?> component, Map<String, Object> props) {
        return render(component.name(), props);
    }

    @Override
    public Uni<Object> render(String component, Map<String, Object> props, int status) {
        storePageStatus(status);
        return render(component, props);
    }

    @Override
    public Uni<Object> render(String component, int status) {
        return render(component, Map.of(), status);
    }

    @Override
    public Uni<Object> render(Enum<?> component, int status) {
        return render(component.name(), Map.of(), status);
    }

    @Override
    public Uni<Object> render(Enum<?> component, Map<String, Object> props, int status) {
        return render(component.name(), props, status);
    }

    private void storePageStatus(int status) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal("inertia-page-status", status);
        }
    }

    @Override
    public InertiaRedirect redirect(String url) {
        return new InertiaRedirect(redirectProcessor.process(url), flashStore);
    }

    @Override
    public InertiaRedirect redirect(String url, boolean fullPage) {
        return new InertiaRedirect(redirectProcessor.process(url, fullPage), flashStore);
    }

    @Override
    public InertiaRedirect back() {
        return new InertiaRedirect(redirectProcessor.back(), flashStore);
    }

    @Override
    public InertiaRedirect back(String fallback) {
        return new InertiaRedirect(redirectProcessor.back(fallback), flashStore);
    }

    @Override
    public InertiaRedirect back(int status, String fallback) {
        return new InertiaRedirect(redirectProcessor.back(status, fallback), flashStore);
    }

    @Override
    public InertiaRedirect back(int status, Map<String, String> headers) {
        return new InertiaRedirect(redirectProcessor.back(status, headers), flashStore);
    }

    @Override
    public InertiaRedirect back(int status, Map<String, String> headers, String fallback) {
        return new InertiaRedirect(redirectProcessor.back(status, headers, fallback), flashStore);
    }

    @Override
    public void header(String name, Object value) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx == null || name == null || value == null) return;
        @SuppressWarnings("unchecked")
        var headers = (Map<String, Object>) ctx.getLocal("inertia-response-headers");
        if (headers == null) {
            headers = new java.util.LinkedHashMap<>();
            ctx.putLocal("inertia-response-headers", headers);
        }
        headers.put(name, value);
    }

    @Override
    public void headers(Map<String, Object> headers) {
        if (headers == null) return;
        for (var entry : headers.entrySet()) {
            header(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void shareInstanceProps(Object instance) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx == null || instance == null) return;
        ctx.putLocal("inertia-instance-props", instance);
    }

    @Override
    public Uni<Object> location(String url) {
        return redirectProcessor.external(url);
    }

    @Override
    public void share(String key, Object value) {
        sharedData.set(key, value);
    }

    @Override
    public void share(Map<String, Object> values) {
        sharedData.setAll(values);
    }

    @Override
    public RawJson rawJson(String json) {
        return RawJson.of(json);
    }

    @Override
    public void always(String key, Object value) {
        sharedData.set(key, AlwaysProp.of(value));
    }

    @Override
    public void flash(String key, Object value) {
        flashStore.put(key, value);
    }

    @Override
    public void flash(Map<String, Object> values) {
        flashStore.putAll(values);
    }

    @Override
    public Object getFlash(String key, Object defaultValue) {
        return flashStore.get(key, defaultValue);
    }

    @Override
    public Object pullFlash(String key, Object defaultValue) {
        return flashStore.pull(key, defaultValue);
    }

    @Override
    public void deferred(String group, String name, Supplier<Uni<Object>> resolver) {
        cachedDeferred(group, name, resolver, null, null);
    }

    @Override
    public void deferred(String name, Supplier<Uni<Object>> resolver) {
        cachedDeferred("default", name, resolver, null, null);
    }

    @Override
    public void deferred(String group, String name, Supplier<Uni<Object>> resolver, String cacheKey) {
        cachedDeferred(group, name, resolver, cacheKey, null);
    }

    @Override
    public void deferred(String group, String name, Supplier<Uni<Object>> resolver, String cacheKey,
            Duration cacheTtl) {
        cachedDeferred(group, name, resolver, cacheKey, cacheTtl);
    }

    private void cachedDeferred(String group, String name, Supplier<Uni<Object>> resolver, String cacheKey,
            Duration cacheTtl) {
        var source = wrapCache(resolver, cacheKey, cacheTtl);
        var keys = sharedData.getDeferredPropGroups().getOrDefault(group, java.util.List.of());
        var updated = new java.util.ArrayList<>(keys);
        if (!updated.contains(name)) updated.add(name);
        sharedData.addDeferredPropGroup(group, updated);
        sharedData.setWithNoTrack(name, source);
    }

    @Override
    public void optional(String key, Supplier<Uni<Object>> resolver) {
        optionalCached(key, resolver, null, null);
    }

    @Override
    public void optional(String key, Supplier<Uni<Object>> resolver, String cacheKey) {
        optionalCached(key, resolver, cacheKey, null);
    }

    @Override
    public void optional(String key, Supplier<Uni<Object>> resolver, String cacheKey, Duration cacheTtl) {
        optionalCached(key, resolver, cacheKey, cacheTtl);
    }

    private void optionalCached(String key, Supplier<Uni<Object>> resolver, String cacheKey, Duration cacheTtl) {
        sharedData.addOptionalProp(key, wrapCache(resolver, cacheKey, cacheTtl));
    }

    @Override
    public void cache(String key, Supplier<Uni<Object>> resolver) {
        sharedData.setWithNoTrack(key, wrapCache(resolver, key, null));
    }

    @Override
    public void cache(String key, Duration ttl, Supplier<Uni<Object>> resolver) {
        sharedData.setWithNoTrack(key, wrapCache(resolver, key, ttl));
    }

    private Supplier<Uni<Object>> wrapCache(Supplier<Uni<Object>> resolver, String cacheKey, Duration cacheTtl) {
        if (cacheKey == null || cacheKey.isBlank()) return resolver;
        var namespaced = "inertia_rails/" + cacheKey;
        return () -> cachedPropStore.compute(namespaced, cacheTtl, resolver);
    }

    @Override
    public void once(String key, Object value) {
        oncePropRegistry.set(key, value);
    }

    @Override
    public void once(String key, Object value, String customKey) {
        oncePropRegistry.set(key, value, customKey, null);
    }

    @Override
    public void once(String key, Supplier<Uni<Object>> resolver) {
        oncePropRegistry.setLazy(key, resolver);
    }

    @Override
    public void once(String key, Supplier<Uni<Object>> resolver, String customKey) {
        oncePropRegistry.setLazy(key, resolver, customKey);
    }

    @Override
    public void once(String key, Supplier<Uni<Object>> resolver, String customKey, Instant expiresAt) {
        oncePropRegistry.setLazy(key, resolver, customKey, expiresAt);
    }

    @Override
    public void shareOnce(String key, Object value) {
        sharedData.set(key, value);
        oncePropRegistry.set(key, value);
    }

    @Override
    public void shareOnce(String key, Supplier<Uni<Object>> resolver) {
        sharedData.set(key, resolver);
        oncePropRegistry.setLazy(key, resolver);
    }

    @Override
    public void merge(String key, Object value) {
        sharedData.merge(key, value, false);
    }

    @Override
    public void merge(String key, Object value, boolean deep) {
        sharedData.merge(key, value, deep);
    }

    @Override
    public void merge(String key, Object value, boolean deep, String... matchOn) {
        sharedData.merge(key, value, deep, matchOn);
    }

    @Override
    public void prepend(String key, Object value) {
        sharedData.addMergePropKey(key);
        sharedData.addPrependPropKey(key);
        sharedData.set(key, value);
    }

    @Override
    public void scroll(String key, Map<String, Object> metadata) {
        sharedData.addScrollProp(key, metadata);
    }

    @Override
    public void scroll(String key, Object value, Map<String, Object> metadata) {
        sharedData.addScrollProp(key, value, "data", metadata);
    }

    @Override
    public void scroll(String key, Object value, Map<String, Object> metadata, String wrapper) {
        sharedData.addScrollProp(key, value, wrapper, metadata);
    }

    @Override
    public void rescue(String key) {
        sharedData.addRescuedProp(key);
    }

    @Override
    public void handleErrorUsing(ErrorMapper mapper) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal(ErrorResponseFactory.CONTEXT_KEY, mapper);
        }
    }

    @Override
    public void meta(String key, Object value) {
        sharedData.addMeta(key, value);
    }

    @Override
    public void meta(Map<String, Object> values) {
        sharedData.addMeta(values);
    }

    @Override
    public void encryptHistory(boolean encrypt) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal("inertia-encrypt-history", encrypt);
        }
    }

    @Override
    public void clearHistory(boolean clear) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal("inertia-clear-history", clear);
        }
    }

    @Override
    public void preserveFragment(boolean preserve) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal("inertia-preserve-fragment", preserve);
        }
        var session = session();
        if (session != null) {
            if (preserve) {
                session.put(PageObjectBuilder.SESSION_PRESERVE_FRAGMENT, Boolean.TRUE);
            } else {
                session.remove(PageObjectBuilder.SESSION_PRESERVE_FRAGMENT);
            }
        }
    }

    private io.vertx.ext.web.Session session() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            var local = ctx.getLocal("inertia-routing-context");
            if (local instanceof io.vertx.ext.web.RoutingContext rc) {
                return rc.session();
            }
        }
        return null;
    }

    boolean isEncryptHistory() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal("inertia-encrypt-history");
            if (val != null) return (Boolean) val;
        }
        return config.encryptHistory();
    }

    boolean isClearHistory() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal("inertia-clear-history");
            return Boolean.TRUE.equals(val);
        }
        return false;
    }

    boolean isPreserveFragment() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal("inertia-preserve-fragment");
            return Boolean.TRUE.equals(val);
        }
        return false;
    }

    @Override
    public String getVersion() {
        return versionProvider.getVersion();
    }

    @Override
    public void version(String version) {
        versionProvider.setVersion(version);
    }

    @Override
    public void setRootView(String name) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal("inertia-root-view", name);
        }
    }

    @Override
    public void withoutSsr(String... paths) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal("inertia-ssr-exclude-paths", java.util.List.of(paths));
        }
    }

    @Override
    public void disableSsr() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal("inertia-disable-ssr", Boolean.TRUE);
        }
    }

    @Override
    public Map<String, Object> getShared() {
        return sharedData.getShared();
    }

    @Override
    public Object getShared(String key, Object defaultValue) {
        return sharedData.getShared(key, defaultValue);
    }

    @Override
    public void flushShared() {
        sharedData.flushShared();
    }
}
