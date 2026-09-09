package io.github.diovamny.spring.inertia.internal;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.web.context.annotation.RequestScope;

import io.github.diovamny.spring.inertia.api.Inertia;
import io.github.diovamny.spring.inertia.api.InertiaRedirect;
import io.github.diovamny.spring.inertia.api.InertiaResponse;
import io.github.diovamny.spring.inertia.api.ProvidesInertiaProperties;
import io.github.diovamny.spring.inertia.api.RenderContext;
import io.github.diovamny.spring.inertia.cache.CachedPropStore;
import io.github.diovamny.spring.inertia.config.InertiaProperties;
import io.github.diovamny.spring.inertia.model.AlwaysProp;
import io.github.diovamny.spring.inertia.model.DeferredProp;
import io.github.diovamny.spring.inertia.model.PageObject;
import io.github.diovamny.spring.inertia.model.RawJson;
import io.github.diovamny.spring.inertia.model.ScrollProp;
import io.github.diovamny.spring.inertia.protocol.PageObjectBuilder;
import io.github.diovamny.spring.inertia.protocol.PartialReloadProcessor;
import io.github.diovamny.spring.inertia.protocol.RedirectProcessor;
import io.github.diovamny.spring.inertia.protocol.ResponseProcessor;
import io.github.diovamny.spring.inertia.protocol.SharedDataRegistry;
import io.github.diovamny.spring.inertia.protocol.OncePropRegistry;
import io.github.diovamny.spring.inertia.spi.ErrorMapper;
import io.github.diovamny.spring.inertia.spi.FlashStore;

/**
 * Request-scoped implementation of {@link Inertia}: records every
 * registration (shared/always/deferred/once/merge props, custom headers,
 * version overrides, error mappers) in the request context and delegates
 * the final assembly to the protocol layer.
 */
@RequestScope
public class InertiaImpl implements Inertia {

    public static final String CONTEXT_VIEW_DATA = "inertia-view-data";

    private final InertiaProperties properties;
    private final SharedDataRegistry sharedDataRegistry;
    private final OncePropRegistry oncePropRegistry;
    private final PartialReloadProcessor partialReloadProcessor;
    private final RedirectProcessor redirectProcessor;
    private final ResponseProcessor responseProcessor;
    private final FlashStore flashStore;
    private final CachedPropStore cachedPropStore;
    private final String version;

    public InertiaImpl(InertiaProperties properties,
            SharedDataRegistry sharedDataRegistry,
            OncePropRegistry oncePropRegistry,
            PartialReloadProcessor partialReloadProcessor,
            RedirectProcessor redirectProcessor,
            ResponseProcessor responseProcessor,
            FlashStore flashStore,
            CachedPropStore cachedPropStore,
            io.github.diovamny.spring.inertia.version.VersionProvider versionProvider) {
        this.properties = properties;
        this.sharedDataRegistry = sharedDataRegistry;
        this.oncePropRegistry = oncePropRegistry;
        this.partialReloadProcessor = partialReloadProcessor;
        this.redirectProcessor = redirectProcessor;
        this.responseProcessor = responseProcessor;
        this.flashStore = flashStore;
        this.cachedPropStore = cachedPropStore;
        this.version = versionProvider != null ? versionProvider.version() : null;
    }

    @Override
    public Object render() {
        return render(null, Map.of());
    }

    @Override
    public Object render(Map<String, Object> props) {
        return render(null, props);
    }

    @Override
    public Object render(String component) {
        return render(component, Map.of());
    }

    @Override
    public Object render(String component, Map<String, Object> props) {
        return responseProcessor.process(component, props);
    }

    @Override
    public Object render(ProvidesInertiaProperties provider) {
        return render((String) null, provider);
    }

    @Override
    public Object render(String component, ProvidesInertiaProperties provider) {
        return render(component, provider != null ? provider.toInertiaProperties(createRenderContext(component)) : Map.of());
    }

    @Override
    public Object render(String component, ProvidesInertiaProperties provider, Map<String, Object> meta) {
        if (meta != null) {
            InertiaRequestContext.set(PageObjectBuilder.CONTEXT_META, meta);
        }
        return render(component, provider);
    }

    private RenderContext createRenderContext(String component) {
        String url = InertiaRequestContext.uri();
        boolean partial = partialReloadProcessor.isPartialReload(component);
        var partialData = new java.util.LinkedHashSet<>(partialReloadProcessor.partialData());
        var partialExcept = new java.util.LinkedHashSet<>(partialReloadProcessor.partialExcept());
        return new RenderContext(component, url, partial, partialData, partialExcept);
    }

    @Override
    public Object render(String component, Map<String, Object> props, Map<String, Object> meta) {
        if (meta != null) {
            InertiaRequestContext.set(PageObjectBuilder.CONTEXT_META, meta);
        }
        return render(component, props);
    }

    @Override
    public void viewData(String key, Object value) {
        var viewData = viewDataRegistry();
        viewData.put(key, value);
        InertiaRequestContext.set(CONTEXT_VIEW_DATA, viewData);
    }

    @Override
    public void viewData(Map<String, Object> data) {
        if (data != null) {
            var viewData = viewDataRegistry();
            viewData.putAll(data);
            InertiaRequestContext.set(CONTEXT_VIEW_DATA, viewData);
        }
    }

    @Override
    public InertiaResponse with(PageObject page, int status) {
        return responseProcessor.serialize(page, status);
    }

    @Override
    public InertiaRedirect redirect(String url) {
        return redirectProcessor.redirect(url, false);
    }

    @Override
    public InertiaRedirect redirect(String url, boolean fullPage) {
        return redirectProcessor.redirect(url, fullPage);
    }

    @Override
    public InertiaRedirect back() {
        return redirectProcessor.back();
    }

    @Override
    public InertiaRedirect back(String fallback) {
        return redirectProcessor.back(fallback);
    }

    @Override
    public InertiaRedirect back(int status, Map<String, String> headers) {
        return redirectProcessor.back(status, headers);
    }

    @Override
    public InertiaRedirect back(int status, Map<String, String> headers, String fallback) {
        return redirectProcessor.back(status, headers, fallback);
    }

    @Override
    public InertiaRedirect location(String url) {
        return redirectProcessor.location(url);
    }

    @Override
    public void flash(String key, Object value) {
        flashStore.put(key, value);
    }

    @Override
    public Object share(String key, Object value) {
        sharedDataRegistry.setSharedProp(key, value);
        return value;
    }

    @Override
    public Object share(Map<String, Object> values) {
        sharedDataRegistry.setSharedProps(values);
        return values;
    }

    @Override
    public Object share(ProvidesInertiaProperties provider) {
        sharedDataRegistry.addSharedProvider(provider);
        return provider;
    }

    @Override
    public Object shared(String key) {
        return sharedDataRegistry.sharedProp(key);
    }

    @Override
    public Object shared(String key, Object defaultValue) {
        return sharedDataRegistry.sharedProp(key, defaultValue);
    }

    @Override
    public void flushShared() {
        sharedDataRegistry.flushShared();
    }

    @Override
    public Object always(String key, Object value) {
        sharedDataRegistry.setAlwaysProp(key, AlwaysProp.of(value));
        return value;
    }

    @Override
    public Object always(Map<String, Object> values) {
        for (var entry : values.entrySet()) {
            sharedDataRegistry.setAlwaysProp(entry.getKey(), AlwaysProp.of(entry.getValue()));
        }
        return values;
    }

    @Override
    public String deferred(String group, String name, Supplier<Object> resolver) {
        var deferred = deferredRegistry();
        deferred.put(name, new DeferredProp<>(group, name, resolver));
        InertiaRequestContext.set(PageObjectBuilder.CONTEXT_DEFERRED, deferred);
        return name;
    }

    @Override
    public Object deferred(Map<String, DeferredProp<Object>> props) {
        var deferred = deferredRegistry();
        deferred.putAll(props);
        InertiaRequestContext.set(PageObjectBuilder.CONTEXT_DEFERRED, deferred);
        return props;
    }

    @Override
    public Object once(String key, Object value) {
        oncePropRegistry.remember(key, null);
        return value;
    }

    @Override
    public Object once(String key, Object value, Duration ttl) {
        oncePropRegistry.remember(key, ttl);
        return value;
    }

    @Override
    public Object once(String key, Object value, String customKey) {
        oncePropRegistry.remember(key, customKey, null);
        return value;
    }

    @Override
    public Object once(String key, Object value, String customKey, Duration ttl) {
        oncePropRegistry.remember(key, customKey, ttl);
        return value;
    }

    @Override
    public Object merge(String key, Object value) {
        return merge(key, value, MergeRule.MERGE);
    }

    @Override
    public Object merge(String key, Object value, MergeRule rule) {
        switch (rule) {
            case MERGE -> addList(PageObjectBuilder.CONTEXT_MERGE_PROPS, key);
            case PREPEND -> addList(PageObjectBuilder.CONTEXT_PREPEND_PROPS, key);
            case DEEP_MERGE -> addList(PageObjectBuilder.CONTEXT_DEEP_MERGE_PROPS, key);
            default -> throw new IllegalArgumentException("Unknown merge rule: " + rule);
        }
        return value;
    }

    @Override
    public Object merge(String key, Object value, MergeRule rule, String... matchOn) {
        merge(key, value, rule);
        for (var field : matchOn) {
            if (field != null && !field.isBlank()) {
                addList(PageObjectBuilder.CONTEXT_MATCH_PROPS_ON, key + "." + field);
            }
        }
        return value;
    }

    @Override
    public Object merge(Map<String, Object> values, MergeRule rule) {
        for (var key : values.keySet()) {
            merge(key, values.get(key), rule);
        }
        return values;
    }

    @Override
    public Object rawJson(String json) {
        return RawJson.of(json);
    }

    @Override
    public Object optional(String key, Supplier<Object> callback) {
        if (!partialReloadProcessor.isPartialReload(partialComponent())) {
            return Optional.empty();
        }
        var only = partialReloadProcessor.partialData();
        var except = partialReloadProcessor.partialExcept();
        boolean selected;
        if (!only.isEmpty()) {
            // Apply only first, then except (except takes precedence).
            selected = only.contains(key) && (except.isEmpty() || !except.contains(key));
        } else if (!except.isEmpty()) {
            selected = !except.contains(key);
        } else {
            selected = true;
        }
        if (!selected) {
            return Optional.empty();
        }
        return callback.get();
    }

    @Override
    public Object cached(String key, Supplier<Object> resolver) {
        return cached(key, null, resolver);
    }

    @Override
    public Object cached(String key, Duration ttl, Supplier<Object> resolver) {
        return new LazyProp(() -> cachedPropStore.compute(key, ttl, resolver));
    }

    @Override
    public void setVersion(String version) {
        InertiaRequestContext.set(PageObjectBuilder.CONTEXT_PAGE_VERSION, version);
    }

    @Override
    public void setEncryptHistory(boolean encrypt) {
        InertiaRequestContext.set(PageObjectBuilder.CONTEXT_ENCRYPT_HISTORY, encrypt);
    }

    @Override
    public void setClearHistory(boolean clear) {
        InertiaRequestContext.set(PageObjectBuilder.CONTEXT_CLEAR_HISTORY, clear);
    }

    @Override
    public void setCamelizeProps(boolean camelize) {
        InertiaRequestContext.set(PageObjectBuilder.CONTEXT_CAMELIZE_PROPS, camelize);
    }

    @Override
    public void handleErrorUsing(ErrorMapper mapper) {
        InertiaRequestContext.set(ErrorResponseFactory.CONTEXT_KEY, mapper);
    }

    @Override
    public Object rememberScrollProp(String key, Object value) {
        var scroll = scrollRegistry();
        scroll.put(key, new ScrollProp(value, null, Map.of("merge", Boolean.TRUE)));
        InertiaRequestContext.set(PageObjectBuilder.CONTEXT_SCROLL_PROPS, scroll);
        return value;
    }

    @Override
    public void scroll(String key, Map<String, Object> metadata) {
        scroll(key, null, metadata);
    }

    @Override
    public void scroll(String key, Object value, Map<String, Object> metadata) {
        var scroll = scrollRegistry();
        scroll.put(key, new ScrollProp(value, null, metadata != null ? metadata : Map.of()));
        InertiaRequestContext.set(PageObjectBuilder.CONTEXT_SCROLL_PROPS, scroll);
    }

    @Override
    public void rescue(String key) {
        var rescued = rescuedRegistry();
        if (!rescued.contains(key)) {
            rescued.add(key);
        }
        InertiaRequestContext.set(PageObjectBuilder.CONTEXT_RESCUED_CANDIDATES, rescued);
    }

    @Override
    public void preserveFragment(boolean preserve) {
        InertiaRequestContext.set(PageObjectBuilder.CONTEXT_PRESERVE_FRAGMENT, preserve);
        var request = InertiaRequestContext.request();
        if (request != null) {
            var session = request.getSession(true);
            if (preserve) {
                session.setAttribute(PageObjectBuilder.SESSION_PRESERVE_FRAGMENT, Boolean.TRUE);
            } else {
                session.removeAttribute(PageObjectBuilder.SESSION_PRESERVE_FRAGMENT);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> viewDataRegistry() {
        var stored = InertiaRequestContext.get(CONTEXT_VIEW_DATA);
        if (stored instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private Map<String, DeferredProp<Object>> deferredRegistry() {
        var stored = InertiaRequestContext.get(PageObjectBuilder.CONTEXT_DEFERRED);
        if (stored instanceof Map<?, ?> map) {
            return (Map<String, DeferredProp<Object>>) map;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private Map<String, ScrollProp> scrollRegistry() {
        var stored = InertiaRequestContext.get(PageObjectBuilder.CONTEXT_SCROLL_PROPS);
        if (stored instanceof Map<?, ?> map) {
            return (Map<String, ScrollProp>) map;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private List<String> rescuedRegistry() {
        var stored = InertiaRequestContext.get(PageObjectBuilder.CONTEXT_RESCUED_CANDIDATES);
        if (stored instanceof List<?> list) {
            return (List<String>) list;
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private void addList(String attr, String key) {
        var stored = InertiaRequestContext.get(attr);
        List<String> list;
        if (stored instanceof List<?> existing) {
            list = (List<String>) existing;
        } else {
            list = new ArrayList<>();
        }
        list.add(key);
        InertiaRequestContext.set(attr, list);
    }

    private static String partialComponent() {
        var value = InertiaRequestContext.get(io.github.diovamny.spring.inertia.protocol.InertiaHeaderExtractor.CONTEXT_PARTIAL_COMPONENT);
        return value != null ? String.valueOf(value) : null;
    }
}
