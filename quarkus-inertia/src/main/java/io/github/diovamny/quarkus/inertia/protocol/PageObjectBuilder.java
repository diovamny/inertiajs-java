package io.github.diovamny.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;

import io.github.diovamny.quarkus.inertia.api.ProvidesInertiaProperties;
import io.github.diovamny.quarkus.inertia.api.RenderContext;
import io.github.diovamny.quarkus.inertia.config.InertiaConfig;
import io.github.diovamny.quarkus.inertia.model.AlwaysProp;
import io.github.diovamny.quarkus.inertia.model.PageObject;
import io.github.diovamny.quarkus.inertia.spi.ComponentTransformer;
import io.github.diovamny.quarkus.inertia.spi.FlashStore;
import io.github.diovamny.quarkus.inertia.spi.UrlResolver;
import io.github.diovamny.quarkus.inertia.version.VersionProvider;

/**
 * Application-scoped assembler of the {@link PageObject}: collects shared,
 * optional, once, merge, scroll and flash props, drains the flash store,
 * applies partial reloads and component/URL transformers, and produces the
 * final page object consumed by the response processors.
 * Per-request state is stored in the Vert.x {@link RoutingContext}.
 */
@ApplicationScoped
public class PageObjectBuilder {

    /** Session attribute carrying the preserve-fragment flag across a redirect. */
    public static final String SESSION_PRESERVE_FRAGMENT = "__inertia_preserve_fragment";

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

    // ========================================================================
    // Reactive API (existing)
    // ========================================================================

    public Uni<PageObject> build(String component, Map<String, Object> props) {
        return build(component, props, isCurrentRequestPartial());
    }

    public Uni<PageObject> build(String component, Map<String, Object> props, boolean isPartial) {
        var resolvedComponent = resolveComponent(component);
        var partialContext = buildPartialReloadContext();
        var renderContext = createRenderContext(resolvedComponent, isPartial, partialContext);

        var allProps = new HashMap<String, Object>();
        if (props != null && !props.isEmpty()) {
            allProps.putAll(props);
        } else {
            allProps.putAll(instanceProps());
        }
        allProps.putAll(sharedData.getAll());
        injectSharedProviders(allProps, renderContext);
        allProps = new HashMap<>(resolvePropertyProviders(allProps, renderContext));

        var flashOut = resolveFlashData(allProps);

        var onceMetadata = oncePropRegistry.metadata();
        var exceptOnceKeys = exceptOncePropKeys();
        if (isPartial && partialContext != null && partialContext.hasData()) {
            var onlyData = partialContext.data();
            var filtered = new java.util.HashSet<>(exceptOnceKeys);
            for (var entry : onceMetadata.entrySet()) {
                if (onlyData.contains(entry.getValue().prop())) {
                    filtered.remove(entry.getKey());
                }
            }
            exceptOnceKeys = filtered;
        }
        if (oncePropRegistry.hasProps()) {
            var onceValues = oncePropRegistry.drain(exceptOnceKeys);
            allProps.putAll(mergePropProcessor.merge(allProps, onceValues));
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

        var deferredGroups = sharedData.getDeferredPropGroups();
        var deferredKeys = deferredGroups.values().stream()
            .flatMap(List::stream)
            .collect(java.util.stream.Collectors.toSet());

        if (!isPartial) {
            deferredKeys.forEach(allProps::remove);
        }

        if (isPartial) {
            for (var entry : sharedData.getOptionalProps().entrySet()) {
                boolean selected;
                if (partialContext.hasData()) {
                    selected = partialContext.data().contains(entry.getKey());
                } else if (partialContext.hasExcept()) {
                    selected = !partialContext.except().contains(entry.getKey());
                } else {
                    selected = true;
                }
                if (selected) {
                    allProps.put(entry.getKey(), entry.getValue());
                }
            }
        }

        var deferredProps = isPartial || deferredGroups.isEmpty() ? null : deferredGroups;
        var resetKeys = resetProps();
        var mergeProps = pruneReset(new java.util.ArrayList<>(sharedData.getMergePropKeys()), resetKeys);
        var prependProps = pruneReset(new java.util.ArrayList<>(sharedData.getPrependPropKeys()), resetKeys);
        var deepMergeProps = pruneReset(new java.util.ArrayList<>(sharedData.getDeepMergePropKeys()), resetKeys);
        var matchPropsOn = pruneReset(new java.util.ArrayList<>(sharedData.getMatchPropKeys()), resetKeys);
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
            var rescuedProps = sharedData.hasRescuedProps()
                ? sharedData.getActuallyRescuedProps()
                : null;

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

    // ========================================================================
    // Synchronous API (new)
    // ========================================================================

    /**
     * Build a page object synchronously, without resolving async props.
     * Use this in blocking endpoints with synchronous props only.
     * Throws {@link IllegalStateException} if async props (deferred/optional/cache)
     * are present and would be required.
     *
     * @param component the component name
     * @param props the page props
     * @return the page object
     */
    public PageObject buildSync(String component, Map<String, Object> props) {
        return buildSync(component, props, isCurrentRequestPartial());
    }

    /**
     * Build a page object synchronously with explicit partial flag.
     */
    public PageObject buildSync(String component, Map<String, Object> props, boolean isPartial) {
        var resolvedComponent = resolveComponent(component);
        var partialContext = buildPartialReloadContext();
        var renderContext = createRenderContext(resolvedComponent, isPartial, partialContext);

        var allProps = new HashMap<String, Object>();
        if (props != null && !props.isEmpty()) {
            allProps.putAll(props);
        } else {
            allProps.putAll(instanceProps());
        }
        allProps.putAll(sharedData.getAll());
        injectSharedProviders(allProps, renderContext);
        allProps = new HashMap<>(resolvePropertyProviders(allProps, renderContext));

        var flashOut = resolveFlashData(allProps);

        var onceMetadata = oncePropRegistry.metadata();
        var exceptOnceKeys = exceptOncePropKeys();
        if (isPartial && partialContext != null && partialContext.hasData()) {
            var onlyData = partialContext.data();
            var filtered = new java.util.HashSet<>(exceptOnceKeys);
            for (var entry : onceMetadata.entrySet()) {
                if (onlyData.contains(entry.getValue().prop())) {
                    filtered.remove(entry.getKey());
                }
            }
            exceptOnceKeys = filtered;
        }
        if (oncePropRegistry.hasProps()) {
            var onceValues = oncePropRegistry.drain(exceptOnceKeys);
            allProps.putAll(mergePropProcessor.merge(allProps, onceValues));
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


        var deferredGroups = sharedData.getDeferredPropGroups();
        var deferredKeys = deferredGroups.values().stream()
            .flatMap(List::stream)
            .collect(java.util.stream.Collectors.toSet());

        if (!isPartial) {
            deferredKeys.forEach(allProps::remove);
        }

        if (isPartial) {
            for (var entry : sharedData.getOptionalProps().entrySet()) {
                boolean selected;
                if (partialContext.hasData()) {
                    selected = partialContext.data().contains(entry.getKey());
                } else if (partialContext.hasExcept()) {
                    selected = !partialContext.except().contains(entry.getKey());
                } else {
                    selected = true;
                }
                if (selected) {
                    allProps.put(entry.getKey(), entry.getValue());
                }
            }
        }

        var deferredProps = isPartial || deferredGroups.isEmpty() ? null : deferredGroups;
        var resetKeys = resetProps();
        var mergeProps = pruneReset(new java.util.ArrayList<>(sharedData.getMergePropKeys()), resetKeys);
        var prependProps = pruneReset(new java.util.ArrayList<>(sharedData.getPrependPropKeys()), resetKeys);
        var deepMergeProps = pruneReset(new java.util.ArrayList<>(sharedData.getDeepMergePropKeys()), resetKeys);
        var matchPropsOn = pruneReset(new java.util.ArrayList<>(sharedData.getMatchPropKeys()), resetKeys);
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

        // Check for async props that would need resolution
        checkAsyncProps(allProps, partialContext);

        // Use props as-is without resolving suppliers
        var resolvedProps = stripSupplierProps(allProps);

        var rescuedProps = sharedData.hasRescuedProps()
            ? sharedData.getActuallyRescuedProps()
            : null;

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
    }

    private void checkAsyncProps(Map<String, Object> props, PartialReloadProcessor.PartialReloadContext partialContext) {
        var requestedKeys = requestedKeys(partialContext);
        for (var entry : props.entrySet()) {
            if (entry.getValue() instanceof Supplier) {
                var key = entry.getKey();
                if (requestedKeys == null || requestedKeys.contains(key)) {
                    throw new IllegalStateException(
                        "Async prop '" + key + "' requires reactive endpoint. " +
                        "Use inertia.renderAsync() or remove deferred/optional/cache props from synchronous render."
                    );
                }
            }
        }
    }

    private Map<String, Object> stripSupplierProps(Map<String, Object> props) {
        var result = new HashMap<String, Object>();
        for (var entry : props.entrySet()) {
            if (!(entry.getValue() instanceof Supplier)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    // ... rest of the methods unchanged from original

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
                // Full page visit (no partial context): resolve all suppliers.
                // In a full visit the only suppliers present in the props are
                // once props (deferred are removed, optional are not added),
                // and once props must always be delivered.
                // Partial reload: only resolve suppliers selected via only/except.
                if (partialContext == null || (requestedKeys != null && requestedKeys.contains(key))) {
                    var supplier = (Supplier<Uni<Object>>) entry.getValue();
                    result = result.chain(map -> supplier.get()
                        .onFailure().recoverWithUni(failure -> {
                            if (sharedData.getRescuedProps().contains(key)) {
                                map.remove(key);
                                sharedData.markActuallyRescued(key);
                                return Uni.createFrom().nullItem();
                            }
                            return Uni.createFrom().failure(failure);
                        })
                        .map(resolved -> {
                            if (resolved == null && sharedData.getRescuedProps().contains(key)) {
                                map.remove(key);
                                sharedData.markActuallyRescued(key);
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
            var allKeys = new java.util.HashSet<String>();
            allKeys.addAll(sharedData.getAll().keySet());
            allKeys.addAll(sharedData.getOptionalProps().keySet());
            return allKeys.stream()
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

    /**
     * Drop the reset keys from a merge metadata list. A reset of a parent
     * prop ({@code contacts}) also prunes its dotted descendants
     * ({@code contacts.data}) so the client replaces the whole subtree
     * instead of merging it with the stale cached value.
     */
    private static List<String> pruneReset(List<String> keys, java.util.Set<String> resetKeys) {
        if (resetKeys.isEmpty()) {
            return keys;
        }
        keys.removeIf(key -> resetKeys.stream()
            .anyMatch(reset -> key.equals(reset) || key.startsWith(reset + ".")));
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

    private java.util.Set<String> exceptOncePropKeys() {
        var ctx = Vertx.currentContext();
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
        if (componentTransformer != null && componentTransformer.isResolvable()) {
            var transformed = componentTransformer.get().transform(component);
            if (transformed != null && !transformed.isBlank()) {
                return transformed;
            }
        }
        return component != null && !component.isBlank() ? component : "Index";
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

            Set<String> data = splitToSet(dataStr);
            Set<String> except = splitToSet(exceptStr);
            Set<String> reset = splitToSet(resetStr);

            return new PartialReloadProcessor.PartialReloadContext(
                component,
                data.isEmpty() ? Set.of() : data,
                except.isEmpty() ? Set.of() : except,
                reset.isEmpty() ? Set.of() : reset
            );
        }

        return buildPartialReloadFromRequest(resolveRequest());
    }

    /**
     * Split a comma-separated header value into a deduplicated set of
     * trimmed, non-blank keys.
     *
     * <p>Inertia 3.x sends {@code X-Inertia-Partial-Data} as
     * {@code only.concat(reset).join(",")}, so the same key can appear more
     * than once; {@link Set#of(Object...)} would reject the duplicates.</p>
     *
     * @param raw the raw header value, or {@code null}
     * @return a set of keys; never contains duplicates or blank entries
     */
    private static Set<String> splitToSet(String raw) {
        if (raw == null || raw.isBlank()) return Set.of();
        var keys = new LinkedHashSet<String>();
        for (var part : raw.split(",")) {
            var trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                keys.add(trimmed);
            }
        }
        return keys;
    }

    private PartialReloadProcessor.PartialReloadContext buildPartialReloadFromRequest(HttpServerRequest request) {
        if (request == null) return null;
        var component = request.getHeader("X-Inertia-Partial-Component");
        if (component == null) return null;

        var dataStr = request.getHeader("X-Inertia-Partial-Data");
        var exceptStr = request.getHeader("X-Inertia-Partial-Except");
        var resetStr = request.getHeader("X-Inertia-Reset");

        Set<String> data = splitToSet(dataStr);
        Set<String> except = splitToSet(exceptStr);
        Set<String> reset = splitToSet(resetStr);

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
            if (val != null) return Boolean.TRUE.equals(val);
        }
        return false;
    }

    private boolean preserveFragment() {
        var ctx = Vertx.currentContext();
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

    private io.vertx.ext.web.Session session() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var local = ctx.getLocal("inertia-routing-context");
            if (local instanceof io.vertx.ext.web.RoutingContext rc) {
                return rc.session();
            }
        }
        try {
            var routingContext = currentVertxRequest.getCurrent();
            if (routingContext != null) {
                return routingContext.session();
            }
        } catch (Exception ignored) {
            // no active request
        }
        return null;
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

    public RenderContext createRenderContext(String component) {
        var resolvedComponent = resolveComponent(component);
        var partialContext = buildPartialReloadContext();
        boolean isPartial = isCurrentRequestPartial();
        return createRenderContext(resolvedComponent, isPartial, partialContext);
    }

    private RenderContext createRenderContext(String component, boolean isPartial, PartialReloadProcessor.PartialReloadContext partialContext) {
        String url = resolveUrl(currentUrl());
        Set<String> partialData = (isPartial && partialContext != null && partialContext.hasData())
            ? partialContext.data() : Set.of();
        Set<String> partialExcept = (isPartial && partialContext != null && partialContext.hasExcept())
            ? partialContext.except() : Set.of();
        return new RenderContext(component, url, isPartial, partialData, partialExcept);
    }

    private void injectSharedProviders(Map<String, Object> props, RenderContext renderContext) {
        for (var provider : sharedData.getSharedProviders()) {
            var provided = provider.toInertiaProperties(renderContext);
            if (provided != null) {
                provided.forEach((k, v) -> {
                    if (!props.containsKey(k)) {
                        props.put(k, v);
                    }
                });
            }
        }
    }

    private Map<String, Object> resolvePropertyProviders(Map<String, Object> props, RenderContext renderContext) {
        if (props == null || props.isEmpty()) {
            return props;
        }
        Map<String, Object> resolved = new HashMap<>();
        for (var entry : props.entrySet()) {
            var val = entry.getValue();
            if (val instanceof ProvidesInertiaProperties provider) {
                resolved.put(entry.getKey(), resolvePropertyProviders(provider.toInertiaProperties(renderContext), renderContext));
            } else if (val instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typedMap = (Map<String, Object>) map;
                resolved.put(entry.getKey(), resolvePropertyProviders(typedMap, renderContext));
            } else {
                resolved.put(entry.getKey(), val);
            }
        }
        return resolved;
    }
}
