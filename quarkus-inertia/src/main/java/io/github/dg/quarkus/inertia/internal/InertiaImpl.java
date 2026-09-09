package io.github.dg.quarkus.inertia.internal;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;

import io.github.dg.quarkus.inertia.api.Inertia;
import io.github.dg.quarkus.inertia.api.InertiaRedirect;
import io.github.dg.quarkus.inertia.api.ProvidesInertiaProperties;
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
import io.github.dg.quarkus.inertia.vertx.ReactiveResponseWriter;
import io.vertx.ext.web.RoutingContext;
import jakarta.ws.rs.core.Response;

/**
 * Default {@link Inertia} implementation (application-scoped): forwards every
 * operation to the page builder, shared-data registry, redirect processor
 * and response processor that assemble the final page object.
 * State is stored per-request in the Vert.x {@link RoutingContext}.
 */
@ApplicationScoped
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
    private final ReactiveResponseWriter reactiveWriter;

    @Inject
    jakarta.enterprise.inject.Instance<io.github.dg.quarkus.inertia.spi.InertiaSharedDataContributor> sharedContributors;

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
            InertiaConfig config,
            ReactiveResponseWriter reactiveWriter) {
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
        this.reactiveWriter = reactiveWriter;
    }

    // ========================================================================
    // Reactive API (existing)
    // ========================================================================

    @Override
    public Uni<Object> render() {
        return render((String) null, Map.of());
    }

    @Override
    public Uni<Object> render(Map<String, Object> props) {
        return render((String) null, props);
    }

    @Override
    public Uni<Object> render(ProvidesInertiaProperties provider) {
        return render((String) null, provider);
    }

    @Override
    public Uni<Object> render(String component, Map<String, Object> props) {
        runSharedContributors();
        return pageBuilder.build(component, props)
            .chain(responseProcessor::process);
    }

    @Override
    public Uni<Object> render(String component, ProvidesInertiaProperties provider) {
        if (provider != null) {
            var renderContext = pageBuilder.createRenderContext(component);
            return render(component, provider.toInertiaProperties(renderContext));
        }
        return render(component, Map.of());
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

    // ========================================================================
    // Synchronous JAX-RS Response API
    // ========================================================================

    @Override
    public Response renderSync() {
        return renderSync((String) null, Map.of());
    }

    @Override
    public Response renderSync(Map<String, Object> props) {
        return renderSync((String) null, props);
    }

    @Override
    public Response renderSync(ProvidesInertiaProperties provider) {
        return renderSync((String) null, provider);
    }

    @Override
    public Response renderSync(String component, Map<String, Object> props) {
        runSharedContributors();
        var page = pageBuilder.buildSync(component, props);
        return responseProcessor.processSync(page);
    }

    @Override
    public Response renderSync(String component, ProvidesInertiaProperties provider) {
        if (provider != null) {
            var renderContext = pageBuilder.createRenderContext(component);
            return renderSync(component, provider.toInertiaProperties(renderContext));
        }
        return renderSync(component, Map.of());
    }

    @Override
    public Response renderSync(String component) {
        var page = pageBuilder.buildSync(component, Map.of());
        return responseProcessor.processSync(page);
    }

    @Override
    public Response renderSync(Enum<?> component) {
        var page = pageBuilder.buildSync(component.name(), Map.of());
        return responseProcessor.processSync(page);
    }

    @Override
    public Response renderSync(Enum<?> component, Map<String, Object> props) {
        var page = pageBuilder.buildSync(component.name(), props);
        return responseProcessor.processSync(page);
    }

    @Override
    public Response renderSync(String component, Map<String, Object> props, int status) {
        storePageStatus(status);
        var page = pageBuilder.buildSync(component, props);
        return responseProcessor.processSync(page);
    }

    @Override
    public Response renderSync(String component, int status) {
        storePageStatus(status);
        var page = pageBuilder.buildSync(component, Map.of());
        return responseProcessor.processSync(page);
    }

    @Override
    public Response renderSync(Enum<?> component, int status) {
        storePageStatus(status);
        var page = pageBuilder.buildSync(component.name(), Map.of());
        return responseProcessor.processSync(page);
    }

    @Override
    public Response renderSync(Enum<?> component, Map<String, Object> props, int status) {
        storePageStatus(status);
        var page = pageBuilder.buildSync(component.name(), props);
        return responseProcessor.processSync(page);
    }

    @Override
    public Response redirectSync(String url) {
        return redirectProcessor.processSync(url);
    }

    @Override
    public Response redirectSync(String url, boolean fullPage) {
        return redirectProcessor.processSync(url, fullPage);
    }

    @Override
    public Response backSync() {
        return redirectProcessor.backSync();
    }

    @Override
    public Response backSync(String fallback) {
        return redirectProcessor.backSync(fallback);
    }

    @Override
    public Response backSync(int status, String fallback) {
        return redirectProcessor.backSync(status, fallback);
    }

    @Override
    public Response backSync(int status, Map<String, String> headers) {
        return redirectProcessor.backSync(status, headers);
    }

    @Override
    public Response backSync(int status, Map<String, String> headers, String fallback) {
        return redirectProcessor.backSync(status, headers, fallback);
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

// ========================================================================
    // Vert.x Reactive Routes (synchronous API)
    // ========================================================================

    @Override
    public void renderVertx(RoutingContext rc, String component, Map<String, Object> props) {
        var page = pageBuilder.buildSync(component, props);
        var response = responseProcessor.processSync(page);
        writeResponse(rc, response);
    }

    @Override
    public void renderVertx(RoutingContext rc, String component) {
        renderVertx(rc, component, Map.of());
    }

    @Override
    public void redirectVertx(RoutingContext rc, String url) {
        var response = redirectProcessor.processSync(url);
        writeResponse(rc, response);
    }

    @Override
    public void redirectVertx(RoutingContext rc, String url, boolean fullPage) {
        var response = redirectProcessor.processSync(url, fullPage);
        writeResponse(rc, response);
    }

    @Override
    public void backVertx(RoutingContext rc) {
        var response = redirectProcessor.backSync();
        writeResponse(rc, response);
    }

    @Override
    public void backVertx(RoutingContext rc, String fallback) {
        var response = redirectProcessor.backSync(fallback);
        writeResponse(rc, response);
    }

    private void writeResponse(RoutingContext rc, jakarta.ws.rs.core.Response response) {
        reactiveWriter.write(rc, response);
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
    public void viewData(String key, Object value) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            Map<String, Object> current = ctx.getLocal("inertia-view-data");
            if (current == null) {
                current = new java.util.LinkedHashMap<>();
                ctx.putLocal("inertia-view-data", current);
            }
            current.put(key, value);
        }
    }

    @Override
    public void viewData(Map<String, Object> data) {
        if (data != null) {
            var ctx = io.vertx.core.Vertx.currentContext();
            if (ctx != null) {
                Map<String, Object> current = ctx.getLocal("inertia-view-data");
                if (current == null) {
                    current = new java.util.LinkedHashMap<>();
                    ctx.putLocal("inertia-view-data", current);
                }
                current.putAll(data);
            }
        }
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
    public void share(ProvidesInertiaProperties provider) {
        sharedData.addSharedProvider(provider);
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
            ctx.putLocal(io.github.dg.quarkus.inertia.internal.ErrorResponseFactory.CONTEXT_KEY, mapper);
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

    private void runSharedContributors() {
        if (sharedContributors != null && !sharedContributors.isUnsatisfied()) {
            var rc = resolveRoutingContext();
            for (var contributor : sharedContributors) {
                var contrib = contributor.contribute(rc);
                if (contrib != null && !contrib.isEmpty()) {
                    sharedData.setAll(contrib);
                }
            }
        }
    }

    private io.vertx.ext.web.RoutingContext resolveRoutingContext() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            var local = ctx.getLocal("inertia-routing-context");
            if (local instanceof io.vertx.ext.web.RoutingContext rc) {
                return rc;
            }
        }
        return null;
    }

    private io.vertx.ext.web.Session session() {
        var rc = resolveRoutingContext();
        return rc != null ? rc.session() : null;
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
            if (val != null) {
                return Boolean.TRUE.equals(val);
            }
        }
        var session = session();
        if (session != null) {
            var stored = session.get(PageObjectBuilder.SESSION_PRESERVE_FRAGMENT);
            if (Boolean.TRUE.equals(stored)) {
                session.remove(PageObjectBuilder.SESSION_PRESERVE_FRAGMENT);
                return true;
            }
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

    private void storePageStatus(int status) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal("inertia-page-status", status);
        }
    }
}
