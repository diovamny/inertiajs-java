package com.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;

import com.quarkus.inertia.config.InertiaConfig;
import com.quarkus.inertia.model.AlwaysProp;
import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.spi.FlashStore;
import com.quarkus.inertia.version.VersionProvider;

@RequestScoped
public class PageObjectBuilder {

    private final SharedDataRegistry sharedData;
    private final VersionProvider versionProvider;
    private final PartialReloadProcessor partialReloadProcessor;
    private final OncePropRegistry oncePropRegistry;
    private final MergePropProcessor mergePropProcessor;
    private final FlashStore flashStore;
    private final CurrentVertxRequest currentVertxRequest;
    private final InertiaConfig config;

    @Inject
    public PageObjectBuilder(
            SharedDataRegistry sharedData,
            VersionProvider versionProvider,
            PartialReloadProcessor partialReloadProcessor,
            OncePropRegistry oncePropRegistry,
            MergePropProcessor mergePropProcessor,
            FlashStore flashStore,
            CurrentVertxRequest currentVertxRequest,
            InertiaConfig config) {
        this.sharedData = sharedData;
        this.versionProvider = versionProvider;
        this.partialReloadProcessor = partialReloadProcessor;
        this.oncePropRegistry = oncePropRegistry;
        this.mergePropProcessor = mergePropProcessor;
        this.flashStore = flashStore;
        this.currentVertxRequest = currentVertxRequest;
        this.config = config;
    }

    public Uni<PageObject> build(String component, Map<String, Object> props) {
        return build(component, props, isCurrentRequestPartial());
    }

    public Uni<PageObject> build(String component, Map<String, Object> props, boolean isPartial) {
        var allProps = new HashMap<String, Object>();
        if (props != null) allProps.putAll(props);
        allProps.putAll(sharedData.getAll());

        if (flashStore.hasData()) {
            allProps.putAll(flashStore.drain());
        }

        if (oncePropRegistry.hasProps()) {
            mergePropProcessor.merge(allProps, oncePropRegistry.drain());
        }

        var errors = resolveErrors();
        if (!errors.isEmpty()) {
            allProps.put("errors", AlwaysProp.of(errors));
        }

        var url = currentUrl();
        var version = versionProvider.getVersion();

        var deferredGroups = sharedData.getDeferredPropGroups();
        var deferredKeys = deferredGroups.values().stream()
            .flatMap(List::stream)
            .collect(java.util.stream.Collectors.toSet());

        if (!isPartial) {
            deferredKeys.forEach(allProps::remove);
        }

        var deferredProps = isPartial ? Map.<String, List<String>>of() : deferredGroups;
        var mergeProps = isPartial ? List.<String>of() : sharedData.getMergePropKeys();
        var prependProps = isPartial ? List.<String>of() : sharedData.getPrependPropKeys();
        var matchPropsOn = isPartial ? List.<String>of() : sharedData.getMatchPropKeys();
        var onceProps = isPartial ? Map.<String, String>of() : sharedData.getOncePropKeys();
        var scrollProps = sharedData.hasScrollProps() ? sharedData.getScrollProps() : null;
        var sharedKeys = sharedData.getSharedKeys();
        var rescuedProps = sharedData.hasRescuedProps() ? sharedData.getRescuedProps() : null;
        var meta = sharedData.hasMeta() ? sharedData.getMeta() : null;

        var encryptHistoryVal = encryptHistory();
        var clearHistoryVal = clearHistory();
        var preserveFragmentVal = preserveFragment();

        var partialContext = buildPartialReloadContext();

        Uni<Map<String, Object>> resolvedPropsUni;
        if (isPartial) {
            resolvedPropsUni = resolveSupplierProps(allProps);
        } else {
            resolvedPropsUni = Uni.createFrom().item(allProps);
        }

        return resolvedPropsUni.map(resolvedProps -> {
            var page = new PageObject(component, Map.copyOf(resolvedProps), url, version,
                deferredProps, mergeProps, prependProps, List.of(), matchPropsOn, onceProps,
                scrollProps, sharedKeys.isEmpty() ? null : sharedKeys, rescuedProps, meta,
                encryptHistoryVal, clearHistoryVal, preserveFragmentVal);

            if (partialContext != null) {
                page = partialReloadProcessor.apply(page, partialContext);
            }

            page = unwrapAlwaysProps(page);

            if (config.camelizeProps()) {
                page = camelizeProps(page);
            }

            return page;
        });
    }

    @SuppressWarnings("unchecked")
    private Uni<Map<String, Object>> resolveSupplierProps(Map<String, Object> props) {
        Uni<Map<String, Object>> result = Uni.createFrom().item(props);
        for (var entry : props.entrySet()) {
            if (entry.getValue() instanceof Supplier) {
                var key = entry.getKey();
                var supplier = (Supplier<Uni<Object>>) entry.getValue();
                result = result.chain(map ->
                    supplier.get().map(resolved -> {
                        map.put(key, resolved);
                        return map;
                    })
                );
            }
        }
        return result;
    }

    private Map<String, Object> resolveErrors() {
        var ctx = Vertx.currentContext();
        if (ctx == null) return Map.of();

        var errorBag = (String) ctx.getLocal("inertia-error-bag");
        var rawErrors = (Map<String, Object>) ctx.getLocal("inertia-validation-errors");
        if (rawErrors == null || rawErrors.isEmpty()) return Map.of();

        if (errorBag != null && !errorBag.isBlank()) {
            return Map.of(errorBag, rawErrors);
        }
        return rawErrors;
    }

    private String currentUrl() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var uri = ctx.getLocal("request-uri");
            if (uri != null) return (String) uri;
        }
        var request = resolveRequest();
        if (request != null) {
            var uri = request.uri();
            if (uri != null) return uri;
        }
        return "/";
    }

    private PartialReloadProcessor.PartialReloadContext buildPartialReloadContext() {
        var ctx = Vertx.currentContext();
        if (ctx == null) {
            var request = resolveRequest();
            return buildPartialReloadFromRequest(request);
        }

        var component = (String) ctx.getLocal("inertia-partial-component");
        if (component != null) {
            var dataStr = (String) ctx.getLocal("inertia-partial-data");
            var exceptStr = (String) ctx.getLocal("inertia-partial-except");
            var resetStr = (String) ctx.getLocal("inertia-reset");

            Set<String> data = dataStr != null ?
                Set.of(dataStr.split(",")) : Set.of();
            Set<String> except = exceptStr != null ?
                Set.of(exceptStr.split(",")) : Set.of();
            Set<String> reset = resetStr != null ?
                Set.of(resetStr.split(",")) : Set.of();

            return new PartialReloadProcessor.PartialReloadContext(
                component,
                data.isEmpty() ? Set.of() : data,
                except.isEmpty() ? Set.of() : except,
                reset.isEmpty() ? Set.of() : reset
            );
        }

        return buildPartialReloadFromRequest(resolveRequest());
    }

    private PartialReloadProcessor.PartialReloadContext buildPartialReloadFromRequest(HttpServerRequest request) {
        if (request == null) return null;
        var component = request.getHeader("X-Inertia-Partial-Component");
        if (component == null) return null;

        var dataStr = request.getHeader("X-Inertia-Partial-Data");
        var exceptStr = request.getHeader("X-Inertia-Partial-Except");
        var resetStr = request.getHeader("X-Inertia-Reset");

        Set<String> data = dataStr != null ?
            Set.of(dataStr.split(",")) : Set.of();
        Set<String> except = exceptStr != null ?
            Set.of(exceptStr.split(",")) : Set.of();
        Set<String> reset = resetStr != null ?
            Set.of(resetStr.split(",")) : Set.of();

        return new PartialReloadProcessor.PartialReloadContext(
            component,
            data.isEmpty() ? Set.of() : data,
            except.isEmpty() ? Set.of() : except,
            reset.isEmpty() ? Set.of() : reset
        );
    }

    private boolean isCurrentRequestPartial() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var component = ctx.getLocal("inertia-partial-component");
            if (component != null) return true;
        }
        var request = resolveRequest();
        if (request != null) {
            return request.getHeader("X-Inertia-Partial-Component") != null;
        }
        return false;
    }

    private boolean encryptHistory() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal("inertia-encrypt-history");
            if (val != null) return Boolean.TRUE.equals(val);
        }
        return config.encryptHistory();
    }

    private boolean clearHistory() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal("inertia-clear-history");
            return Boolean.TRUE.equals(val);
        }
        return false;
    }

    private boolean preserveFragment() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal("inertia-preserve-fragment");
            return Boolean.TRUE.equals(val);
        }
        return false;
    }

    private PageObject unwrapAlwaysProps(PageObject page) {
        var props = page.props();
        boolean hasWrapped = props.values().stream().anyMatch(v -> v instanceof AlwaysProp);
        if (!hasWrapped) return page;

        var unwrapped = new HashMap<String, Object>();
        for (var entry : props.entrySet()) {
            var value = entry.getValue();
            unwrapped.put(entry.getKey(), value instanceof AlwaysProp<?> a ? a.value() : value);
        }
        return page.withProps(Map.copyOf(unwrapped));
    }

    @SuppressWarnings("unchecked")
    private PageObject camelizeProps(PageObject page) {
        var props = page.props();
        var camelized = new LinkedHashMap<String, Object>();
        for (var entry : props.entrySet()) {
            camelized.put(toCamelCase(entry.getKey()), camelizeValue(entry.getValue()));
        }
        return page.withProps(Map.copyOf(camelized));
    }

    @SuppressWarnings("unchecked")
    private Object camelizeValue(Object value) {
        if (value instanceof Map) {
            var map = (Map<String, Object>) value;
            var result = new LinkedHashMap<String, Object>();
            for (var entry : map.entrySet()) {
                result.put(toCamelCase(entry.getKey()), camelizeValue(entry.getValue()));
            }
            return result;
        }
        if (value instanceof List) {
            var list = (List<Object>) value;
            return list.stream().map(this::camelizeValue).toList();
        }
        return value;
    }

    static String toCamelCase(String key) {
        if (key == null || key.isEmpty()) return key;
        if (!key.contains("_")) return key;
        var parts = key.split("_");
        var sb = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            var p = parts[i];
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0)));
                sb.append(p.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    private HttpServerRequest resolveRequest() {
        try {
            var routingContext = currentVertxRequest.getCurrent();
            if (routingContext != null) {
                return routingContext.request();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
