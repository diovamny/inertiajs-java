package com.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;

import com.quarkus.inertia.config.InertiaConfig;
import com.quarkus.inertia.model.AlwaysProp;
import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.spi.ComponentTransformer;
import com.quarkus.inertia.spi.FlashStore;
import com.quarkus.inertia.spi.UrlResolver;
import com.quarkus.inertia.version.VersionProvider;

/**
 * Request-scoped assembler of the {@link PageObject}: collects shared,
 * optional, once, merge, scroll and flash props, drains the flash store,
 * applies partial reloads and component/URL transformers, and produces the
 * final page object consumed by the response processors.
 */
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
    private final Instance<ComponentTransformer> componentTransformer;
    private final Instance<UrlResolver> urlResolver;

    @Inject
    public PageObjectBuilder(
            SharedDataRegistry sharedData,
            VersionProvider versionProvider,
            PartialReloadProcessor partialReloadProcessor,
            OncePropRegistry oncePropRegistry,
            MergePropProcessor mergePropProcessor,
            FlashStore flashStore,
            CurrentVertxRequest currentVertxRequest,
            InertiaConfig config,
            Instance<ComponentTransformer> componentTransformer,
            Instance<UrlResolver> urlResolver) {
        this.sharedData = sharedData;
        this.versionProvider = versionProvider;
        this.partialReloadProcessor = partialReloadProcessor;
        this.oncePropRegistry = oncePropRegistry;
        this.mergePropProcessor = mergePropProcessor;
        this.flashStore = flashStore;
        this.currentVertxRequest = currentVertxRequest;
        this.config = config;
        this.componentTransformer = componentTransformer;
        this.urlResolver = urlResolver;
    }

    PageObjectBuilder(
            SharedDataRegistry sharedData,
            VersionProvider versionProvider,
            PartialReloadProcessor partialReloadProcessor,
            OncePropRegistry oncePropRegistry,
            MergePropProcessor mergePropProcessor,
            FlashStore flashStore,
            CurrentVertxRequest currentVertxRequest,
            InertiaConfig config) {
        this(sharedData, versionProvider, partialReloadProcessor, oncePropRegistry, mergePropProcessor,
            flashStore, currentVertxRequest, config, null, null);
    }

    public Uni<PageObject> build(String component, Map<String, Object> props) {
        return build(component, props, isCurrentRequestPartial());
    }

    public Uni<PageObject> build(String component, Map<String, Object> props, boolean isPartial) {
        var resolvedComponent = resolveComponent(component);
        var allProps = new HashMap<String, Object>();
        if (props != null && !props.isEmpty()) {
            allProps.putAll(props);
        } else {
            allProps.putAll(instanceProps());
        }
        allProps.putAll(sharedData.getAll());

        var flashOut = resolveFlashData(allProps);

        var onceMetadata = oncePropRegistry.metadata();
        var exceptOnceKeys = exceptOncePropKeys();
        var clientHasOnceKeys = oncePropRegistry.propKeys(exceptOnceKeys);
        if (oncePropRegistry.hasProps()) {
            var onceValues = oncePropRegistry.drain(exceptOnceKeys);
            allProps.putAll(mergePropProcessor.merge(allProps, onceValues));
        }
        if (!clientHasOnceKeys.isEmpty()) {
            clientHasOnceKeys.forEach(allProps::remove);
        }

        wrapScrollPropValues(allProps);

        var errors = resolveErrors();
        if (!allProps.containsKey("errors")) {
            boolean hasErrors = errors instanceof Map<?, ?> errs && !errs.isEmpty();
            if (config.alwaysIncludeErrors() || hasErrors) {
                allProps.put("errors", AlwaysProp.of(errors));
            }
        }

        var url = resolveUrl(currentUrl());
        var version = versionProvider.getVersion();

        var partialContext = buildPartialReloadContext();

        var deferredGroups = sharedData.getDeferredPropGroups();
        var deferredKeys = deferredGroups.values().stream()
            .flatMap(List::stream)
            .collect(java.util.stream.Collectors.toSet());

        if (!isPartial) {
            deferredKeys.forEach(allProps::remove);
        }

        if (isPartial) {
            var explicitDataKeys = explicitPartialDataKeys(partialContext);
            if (explicitDataKeys != null) {
                for (var entry : sharedData.getOptionalProps().entrySet()) {
                    if (explicitDataKeys.contains(entry.getKey())) {
                        allProps.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        var deferredProps = isPartial || deferredGroups.isEmpty() ? null : deferredGroups;
        var resetKeys = resetProps();
        var mergeProps = new java.util.ArrayList<>(sharedData.getMergePropKeys());
        mergeProps.removeIf(resetKeys::contains);
        var prependProps = new java.util.ArrayList<>(sharedData.getPrependPropKeys());
        prependProps.removeIf(resetKeys::contains);
        var deepMergeProps = new java.util.ArrayList<>(sharedData.getDeepMergePropKeys());
        deepMergeProps.removeIf(resetKeys::contains);
        var matchPropsOn = new java.util.ArrayList<>(sharedData.getMatchPropKeys());
        matchPropsOn.removeIf(resetKeys::contains);
        var onceProps = onceMetadata.isEmpty() ? null : onceMetadata;
        var scrollProps = sharedData.hasScrollProps()
            ? buildScrollProps(mergeProps, prependProps, matchPropsOn)
            : null;
        var mergePropsOut = mergeProps.isEmpty() ? null : mergeProps;
        var prependPropsOut = prependProps.isEmpty() ? null : prependProps;
        var deepMergePropsOut = deepMergeProps.isEmpty() ? null : java.util.Collections.unmodifiableList(deepMergeProps);
        var matchPropsOnOut = matchPropsOn.isEmpty() ? null : matchPropsOn;
        var sharedKeys = sharedData.getSharedKeys();
        var meta = sharedData.hasMeta() ? sharedData.getMeta() : null;

        var encryptHistoryVal = encryptHistory() ? Boolean.TRUE : null;
        var clearHistoryVal = clearHistory() ? Boolean.TRUE : null;
        var preserveFragmentVal = preserveFragment() ? Boolean.TRUE : null;

        return resolveSupplierProps(allProps, partialContext).map(resolvedProps -> {
            var rescuedProps = sharedData.hasRescuedProps() ? sharedData.getRescuedProps() : null;

            var page = new PageObject(resolvedComponent, copyOfNullTolerant(resolvedProps), url, version,
                flashOut, deferredProps, mergePropsOut, prependPropsOut, deepMergePropsOut, matchPropsOnOut,
                onceProps, scrollProps, sharedKeys.isEmpty() ? null : sharedKeys, rescuedProps, meta,
                encryptHistoryVal, clearHistoryVal, preserveFragmentVal);

            page = expandDotNotation(page);

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

    /**
     * Drain the flash store and return the flash data delivered to the
     * client as the top-level {@code flash} page key, or {@code null}
     * when there is nothing to deliver.
     *
     * <p>The {@code errors} key is kept in the page props (where the
     * validation machinery consumes it) but excluded from the top-level
     * flash so it never fires the client's {@code flash} event.</p>
     *
     * @param allProps the accumulated page props (mutated in place with
     *                 the drained flash keys, mirroring the legacy flat
     *                 prop contract)
     * @return the top-level flash map, or {@code null} when empty
     */
    private Map<String, Object> resolveFlashData(Map<String, Object> allProps) {
        if (!flashStore.hasData() || isGetVersionMismatch()) {
            return null;
        }
        var flashed = flashStore.drain();
        if (flashed.isEmpty()) {
            return null;
        }
        var allowed = config.flashKeys().orElse(null);
        var target = flashed;
        if (allowed != null) {
            var filtered = new HashMap<String, Object>();
            for (var key : allowed) {
                if (flashed.containsKey(key)) {
                    filtered.put(key, flashed.get(key));
                }
            }
            target = filtered;
        }
        mergeFlashed(allProps, target);
        var flashData = new HashMap<>(target);
        flashData.remove("errors");
        return flashData.isEmpty() ? null : Map.copyOf(flashData);
    }

    @SuppressWarnings("unchecked")
    private Uni<Map<String, Object>> resolveSupplierProps(Map<String, Object> props,
            PartialReloadProcessor.PartialReloadContext partialContext) {
        var requestedKeys = requestedKeys(partialContext);
        Uni<Map<String, Object>> result = Uni.createFrom().item(props);
        for (var entry : props.entrySet()) {
            if (entry.getValue() instanceof Supplier) {
                var key = entry.getKey();
                if (requestedKeys == null || requestedKeys.contains(key)) {
                    var supplier = (Supplier<Uni<Object>>) entry.getValue();
                    result = result.chain(map -> supplier.get()
                        .onFailure().recoverWithUni(failure -> {
                            if (sharedData.getRescuedProps().contains(key)) {
                                map.remove(key);
                                return Uni.createFrom().nullItem();
                            }
                            return Uni.createFrom().failure(failure);
                        })
                        .map(resolved -> {
                            if (resolved == null && sharedData.getRescuedProps().contains(key)) {
                                map.remove(key);
                            } else {
                                map.put(key, resolved);
                            }
                            return map;
                        })
                    );
                }
            }
        }
        return result;
    }

    private java.util.Set<String> requestedKeys(PartialReloadProcessor.PartialReloadContext partialContext) {
        if (partialContext == null) return null;
        if (partialContext.hasData()) return partialContext.data();
        if (partialContext.hasExcept()) {
            return sharedData.getAll().keySet().stream()
                .filter(key -> !partialContext.except().contains(key))
                .collect(java.util.stream.Collectors.toSet());
        }
        return null;
    }

    private java.util.Set<String> explicitPartialDataKeys(PartialReloadProcessor.PartialReloadContext partialContext) {
        if (partialContext == null) return null;
        return partialContext.hasData() ? partialContext.data() : null;
    }

    private Map<String, Map<String, Object>> buildScrollProps(List<String> mergeProps,
            List<String> prependProps, List<String> matchPropsOn) {
        var result = new LinkedHashMap<String, Map<String, Object>>();
        var intent = scrollMergeIntent();
        var resetProps = resetProps();
        for (var entry : sharedData.getScrollSpecs().entrySet()) {
            var key = entry.getKey();
            var spec = entry.getValue();
            var metadata = spec.metadata();
            var wrapper = spec.wrapper() != null ? spec.wrapper() : "data";

            var mergePath = wrapper;
            if ("prepend".equals(intent)) {
                prependProps.add(key + "." + mergePath);
            } else {
                mergeProps.add(key + "." + mergePath);
            }

            var matchOn = metadata.get("matchOn");
            if (matchOn instanceof String s) {
                matchPropsOn.add(key + "." + mergePath + "." + s);
            } else if (matchOn instanceof List<?> list) {
                for (var p : list) {
                    matchPropsOn.add(key + "." + mergePath + "." + p);
                }
            }

            var clean = new LinkedHashMap<String, Object>();
            for (var name : List.of("previousPage", "nextPage", "currentPage", "pageName")) {
                if (metadata.containsKey(name)) {
                    clean.put(name, metadata.get(name));
                }
            }
            clean.put("reset", resetProps.contains(key));
            result.put(key, java.util.Collections.unmodifiableMap(clean));
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    private java.util.Set<String> resetProps() {
        var ctx = Vertx.currentContext();
        if (ctx == null) return java.util.Set.of();
        var raw = (String) ctx.getLocal("inertia-reset");
        if (raw == null || raw.isBlank()) return java.util.Set.of();
        var keys = new HashSet<String>();
        for (var part : raw.split(",")) {
            var trimmed = part.trim();
            if (!trimmed.isEmpty()) keys.add(trimmed);
        }
        return keys;
    }

    private void wrapScrollPropValues(Map<String, Object> allProps) {
        if (!sharedData.hasScrollProps()) return;
        for (var entry : sharedData.getScrollSpecs().entrySet()) {
            var spec = entry.getValue();
            if (spec.value() != null) {
                allProps.put(entry.getKey(), spec.value());
            }
        }
    }

    private String scrollMergeIntent() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var intent = (String) ctx.getLocal("inertia-scroll-merge-intent");
            if (intent != null) return intent;
        }
        return null;
    }

    private Map<String, Object> instanceProps() {
        var ctx = Vertx.currentContext();
        if (ctx == null) return Map.of();
        var instance = ctx.getLocal("inertia-instance-props");
        if (instance == null) return Map.of();
        try {
            var info = java.beans.Introspector.getBeanInfo(instance.getClass(), Object.class);
            var result = new LinkedHashMap<String, Object>();
            for (var descriptor : info.getPropertyDescriptors()) {
                var method = descriptor.getReadMethod();
                if (method == null || method.getParameterCount() != 0) continue;
                var value = method.invoke(instance);
                if (value == null) continue;
                result.put(descriptor.getName(), value);
            }
            return result;
        } catch (Exception e) {
            return Map.of();
        }
    }

    private boolean isGetVersionMismatch() {
        var ctx = Vertx.currentContext();
        if (ctx == null) return false;
        if (!Boolean.TRUE.equals(ctx.getLocal("inertia-request"))) return false;
        var method = (String) ctx.getLocal("request-method");
        if (!"GET".equalsIgnoreCase(method)) return false;
        var clientVersion = (String) ctx.getLocal("inertia-version");
        if (clientVersion == null || clientVersion.isBlank()) return false;
        return !clientVersion.equals(versionProvider.getVersion());
    }

    private java.util.Set<String> exceptOncePropKeys() {        var ctx = Vertx.currentContext();
        if (ctx == null) return java.util.Set.of();
        var raw = (String) ctx.getLocal("inertia-except-once-props");
        if (raw == null || raw.isBlank()) return java.util.Set.of();
        var keys = new HashSet<String>();
        for (var part : raw.split(",")) {
            var trimmed = part.trim();
            if (!trimmed.isEmpty()) keys.add(trimmed);
        }
        return keys;
    }

    private void mergeFlashed(Map<String, Object> target, Map<String, Object> flashed) {
        for (var entry : flashed.entrySet()) {
            var value = entry.getValue();
            target.put(entry.getKey(), "errors".equals(entry.getKey()) ? AlwaysProp.of(value) : value);
        }
    }

    private Map<String, Object> resolveErrors() {
        var ctx = Vertx.currentContext();
        if (ctx == null) return Map.of();

        var errorBag = (String) ctx.getLocal("inertia-error-bag");
        var rawErrors = (Map<String, Object>) ctx.getLocal("inertia-validation-errors");
        if (rawErrors == null || rawErrors.isEmpty()) {
            return Map.of();
        }

        if (errorBag != null && !errorBag.isBlank()) {
            return Map.of(errorBag, rawErrors);
        }
        return rawErrors;
    }

    private static Map<String, Object> copyOfNullTolerant(Map<String, Object> source) {
        var copy = new HashMap<String, Object>();
        if (source != null) {
            copy.putAll(source);
        }
        return java.util.Collections.unmodifiableMap(copy);
    }

    private String resolveComponent(String component) {
        if (component == null || componentTransformer == null || !componentTransformer.isResolvable()) {
            return component;
        }
        var transformed = componentTransformer.get().transform(component);
        return transformed != null && !transformed.isBlank() ? transformed : component;
    }

    private String resolveUrl(String url) {
        if (url == null || urlResolver == null || !urlResolver.isResolvable()) {
            return url;
        }
        var resolved = urlResolver.get().resolve(url);
        return resolved != null && !resolved.isBlank() ? resolved : url;
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

    @SuppressWarnings("unchecked")
    private PageObject expandDotNotation(PageObject page) {
        var props = page.props();
        boolean hasDotted = props.keySet().stream().anyMatch(k -> k != null && k.contains("."));
        if (!hasDotted) return page;

        var expanded = new LinkedHashMap<String, Object>();
        for (var entry : props.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (key != null && key.contains(".")) {
                var parts = key.split("\\.");
                Map<String, Object> current = expanded;
                for (int i = 0; i < parts.length - 1; i++) {
                    var part = parts[i];
                    var existing = current.get(part);
                    Map<String, Object> next;
                    if (existing instanceof Map<?, ?> map) {
                        next = new LinkedHashMap<>();
                        map.forEach((k, v) -> next.put(String.valueOf(k), v));
                    } else {
                        next = new LinkedHashMap<>();
                    }
                    current.put(part, next);
                    current = next;
                }
                current.put(parts[parts.length - 1], value);
            } else {
                var existing = expanded.get(key);
                if (existing instanceof Map<?, ?> map && value instanceof Map<?, ?> vmap) {
                    var merged = new LinkedHashMap<String, Object>();
                    map.forEach((k, v) -> merged.put(String.valueOf(k), v));
                    vmap.forEach((k, v) -> merged.put(String.valueOf(k), v));
                    expanded.put(key, merged);
                } else {
                    expanded.put(key, value);
                }
            }
        }
        return page.withProps(copyOfNullTolerant(expanded));
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
        return page.withProps(copyOfNullTolerant(unwrapped));
    }

    @SuppressWarnings("unchecked")
    private PageObject camelizeProps(PageObject page) {
        var props = page.props();
        var camelized = new LinkedHashMap<String, Object>();
        for (var entry : props.entrySet()) {
            camelized.put(toCamelCase(entry.getKey()), camelizeValue(entry.getValue()));
        }
        return page.withProps(copyOfNullTolerant(camelized));
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
