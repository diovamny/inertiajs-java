package io.github.dg.spring.inertia.protocol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.context.annotation.RequestScope;

import io.github.dg.spring.inertia.api.ProvidesInertiaProperties;
import io.github.dg.spring.inertia.api.RenderContext;
import io.github.dg.spring.inertia.config.InertiaProperties;
import io.github.dg.spring.inertia.internal.InertiaRequestContext;
import io.github.dg.spring.inertia.internal.LazyProp;
import io.github.dg.spring.inertia.model.DeferredProp;
import io.github.dg.spring.inertia.model.PageObject;
import io.github.dg.spring.inertia.model.ScrollProp;
import io.github.dg.spring.inertia.spi.ComponentTransformer;
import io.github.dg.spring.inertia.spi.FlashStore;
import io.github.dg.spring.inertia.spi.UrlResolver;
import io.github.dg.spring.inertia.version.VersionProvider;

/**
 * Assembles the {@link PageObject} for the current visit: merges props,
 * shared/always props, flash data, validation errors, once/deferred/merge
 * metadata, applies partial reloads, resolves the URL and the asset version.
 */
@RequestScope
public class PageObjectBuilder {

    public static final String CONTEXT_ERRORS = "inertia-validation-errors";
    public static final String CONTEXT_DEFERRED = "inertia-deferred-props";
    public static final String CONTEXT_MERGE_PROPS = "inertia-merge-props";
    public static final String CONTEXT_PREPEND_PROPS = "inertia-prepend-props";
    public static final String CONTEXT_DEEP_MERGE_PROPS = "inertia-deep-merge-props";
    public static final String CONTEXT_MATCH_PROPS_ON = "inertia-match-props-on";
    public static final String CONTEXT_SCROLL_PROPS = "inertia-scroll-props";
    public static final String CONTEXT_RESCUED_PROPS = "inertia-rescued-props";
    public static final String CONTEXT_RESCUED_CANDIDATES = "inertia-rescued-candidates";
    public static final String CONTEXT_PRESERVE_FRAGMENT = "inertia-preserve-fragment";
    public static final String CONTEXT_META = "inertia-meta";
    public static final String CONTEXT_PAGE_STATUS = "inertia-page-status";
    public static final String CONTEXT_PAGE_VERSION = "inertia-page-version";
    public static final String CONTEXT_VERSION_MISMATCH = "inertia-version-mismatch";
    public static final String CONTEXT_CUSTOM_HEADERS = "inertia-custom-headers";
    public static final String CONTEXT_ENCRYPT_HISTORY = "inertia-encrypt-history";
    public static final String CONTEXT_CLEAR_HISTORY = "inertia-clear-history";
    public static final String CONTEXT_CAMELIZE_PROPS = "inertia-camelize-props";
    /** Session attribute carrying the preserve-fragment flag across a redirect. */
    public static final String SESSION_PRESERVE_FRAGMENT = "__inertia_preserve_fragment";

    private final InertiaProperties properties;
    private final SharedDataRegistry sharedDataRegistry;
    private final OncePropRegistry oncePropRegistry;
    private final PartialReloadProcessor partialReloadProcessor;
    private final MergePropProcessor mergePropProcessor;
    private final FlashStore flashStore;
    private final VersionProvider versionProvider;
    private final ComponentTransformer componentTransformer;
    private final UrlResolver urlResolver;

    private String alwaysErrorsKey;

    public PageObjectBuilder(InertiaProperties properties,
            SharedDataRegistry sharedDataRegistry,
            OncePropRegistry oncePropRegistry,
            PartialReloadProcessor partialReloadProcessor,
            MergePropProcessor mergePropProcessor,
            FlashStore flashStore,
            VersionProvider versionProvider,
            ComponentTransformer componentTransformer,
            UrlResolver urlResolver) {
        this.properties = properties;
        this.sharedDataRegistry = sharedDataRegistry;
        this.oncePropRegistry = oncePropRegistry;
        this.partialReloadProcessor = partialReloadProcessor;
        this.mergePropProcessor = mergePropProcessor;
        this.flashStore = flashStore;
        this.versionProvider = versionProvider;
        this.componentTransformer = componentTransformer;
        this.urlResolver = urlResolver;
    }

    /**
     * Build the page object for the given component and props.
     *
     * @param component          the frontend component
     * @param props              the page props
     * @param alwaysIncludeErrors whether validation errors are injected
     * @return the assembled page object
     */
    public PageObject build(String component, Map<String, Object> props, boolean alwaysIncludeErrors) {
        Map<String, Object> merged = new LinkedHashMap<>(props != null ? props : Map.of());
        if (isCamelizeEnabled()) {
            merged = camelize(merged);
        }

        var url = urlResolver != null
            ? urlResolver.resolve(InertiaRequestContext.uri())
            : InertiaRequestContext.uri();
        var version = resolveVersion();

        InertiaRequestContext.set(CONTEXT_PAGE_VERSION, version);
        var clientVersion = InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_VERSION);
        var isPrefetch = "true".equals(InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_PREFETCH));
        InertiaRequestContext.set(CONTEXT_VERSION_MISMATCH,
            !isPrefetch
                && "GET".equalsIgnoreCase(InertiaRequestContext.method())
                && clientVersion != null
                && version != null
                && !version.equals(String.valueOf(clientVersion)));

        var flash = resolveFlashData(merged);

        var partial = partialReloadProcessor.isPartialReload(component);
        var partialData = new LinkedHashSet<>(attrList(InertiaHeaderExtractor.CONTEXT_PARTIAL_DATA));
        var partialExcept = new LinkedHashSet<>(attrList(InertiaHeaderExtractor.CONTEXT_PARTIAL_EXCEPT));
        var renderContext = new RenderContext(component, url, partial, partialData, partialExcept);

        injectValidationErrors(merged, alwaysIncludeErrors);
        injectSharedProps(merged, renderContext);
        merged = resolvePropertyProviders(merged, renderContext);
        applyOnceProps(merged);
        applyAlwaysProps(merged);

        var deferredProps = collectDeferredProps(merged);
        var scrollProps = applyScrollProps(merged);
        var resetKeys = resetKeys();
        var mergeProps = pruneReset(stringList(CONTEXT_MERGE_PROPS), resetKeys);
        var prependProps = pruneReset(stringList(CONTEXT_PREPEND_PROPS), resetKeys);
        var deepMergeProps = pruneReset(stringList(CONTEXT_DEEP_MERGE_PROPS), resetKeys);
        var matchPropsOn = pruneReset(stringList(CONTEXT_MATCH_PROPS_ON), resetKeys);

        if (partial) {
            if (partialReloadProcessor.isPartialReset()) {
                mergePropProcessor.reset();
            }
            merged = partialReloadProcessor.filterProps(merged, partialReloadBaseProps(merged));
        }

        merged = resolveLazyProps(merged);
        merged = unwrapOptionals(merged, partial);

        var rescued = rescuedKeys();

        var resolvedComponent = componentTransformer != null
            ? componentTransformer.transform(component)
            : component;

        var page = new PageObject(resolvedComponent, merged, url, version,
            flash,
            deferredProps.isEmpty() || partial ? null : deferredProps,
            mergeProps.isEmpty() ? null : mergeProps,
            prependProps.isEmpty() ? null : prependProps,
            deepMergeProps.isEmpty() ? null : deepMergeProps,
            matchPropsOn.isEmpty() ? null : matchPropsOn,
            oncePropRegistry.metadata().isEmpty() ? null : oncePropRegistry.metadata(),
            scrollProps.isEmpty() ? null : scrollProps,
            sharedDataRegistry.sharedProps().isEmpty() ? null : List.copyOf(sharedDataRegistry.sharedProps().keySet()),
            rescued.isEmpty() ? null : List.copyOf(rescued),
            meta(),
            encryptHistoryEnabled(),
            clearHistoryEnabled(),
            preserveFragmentEnabled());
        return page;
    }

    private String resolveVersion() {
        var override = InertiaRequestContext.get(CONTEXT_PAGE_VERSION);
        if (override != null) {
            return String.valueOf(override);
        }
        return versionProvider != null ? versionProvider.version() : null;
    }

    private boolean isCamelizeEnabled() {
        var override = InertiaRequestContext.get(CONTEXT_CAMELIZE_PROPS);
        if (override != null) {
            return Boolean.TRUE.equals(override);
        }
        return properties.isCamelizeProps();
    }

    private boolean encryptHistoryEnabled() {
        var override = InertiaRequestContext.get(CONTEXT_ENCRYPT_HISTORY);
        if (override != null) {
            return Boolean.TRUE.equals(override);
        }
        return properties.isEncryptHistory();
    }

    private boolean clearHistoryEnabled() {
        var override = InertiaRequestContext.get(CONTEXT_CLEAR_HISTORY);
        if (override != null) {
            return Boolean.TRUE.equals(override);
        }
        return properties.isClearHistory();
    }

    private void injectValidationErrors(Map<String, Object> merged, boolean alwaysIncludeErrors) {
        var bag = InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_ERROR_BAG);
        var errors = InertiaRequestContext.get(CONTEXT_ERRORS);
        if (errors instanceof Map<?, ?> map && !map.isEmpty()) {
            var target = bag != null
                ? Map.of(String.valueOf(bag), new LinkedHashMap<>(map))
                : new LinkedHashMap<>(map);
            merged.put("errors", target);
            alwaysErrorsKey = "errors";
        } else if (alwaysIncludeErrors) {
            merged.putIfAbsent("errors", Map.of());
        }
    }

    /**
     * Drain the flash store and return the flash data delivered to the
     * client as the top-level {@code flash} page key, or {@code null}
     * when there is nothing to deliver.
     *
     * <p>The flashed values are mirrored into the page props as well
     * (legacy flat prop contract), but the {@code errors} key is kept
     * only in the props (where the validation machinery consumes it)
     * and excluded from the top-level flash so it never fires the
     * client's {@code flash} event.</p>
     *
     * @param merged the accumulated page props (mutated in place with the
     *               drained flash keys)
     * @return the top-level flash map, or {@code null} when empty
     */
    private Map<String, Object> resolveFlashData(Map<String, Object> merged) {
        if (!flashStore.hasData()
                || Boolean.TRUE.equals(InertiaRequestContext.get(CONTEXT_VERSION_MISMATCH))) {
            return null;
        }
        var flashed = flashStore.drain();
        if (flashed.isEmpty()) {
            return null;
        }
        var allowed = properties.getFlashKeys();
        var target = flashed;
        if (!allowed.isEmpty()) {
            var filtered = new LinkedHashMap<String, Object>();
            for (var key : allowed) {
                if (flashed.containsKey(key)) {
                    filtered.put(key, flashed.get(key));
                }
            }
            target = filtered;
        }
        for (var entry : target.entrySet()) {
            merged.put(entry.getKey(), entry.getValue());
        }
        var flashData = new LinkedHashMap<>(target);
        flashData.remove("errors");
        return flashData.isEmpty() ? null : flashData;
    }

    private void injectSharedProps(Map<String, Object> merged, RenderContext renderContext) {
        for (var provider : sharedDataRegistry.sharedProviders()) {
            try {
                var provided = provider.toInertiaProperties(renderContext);
                if (provided != null) {
                    for (var entry : provided.entrySet()) {
                        merged.putIfAbsent(entry.getKey(), entry.getValue());
                    }
                }
            } catch (Exception ignored) {
            }
        }
        for (var entry : sharedDataRegistry.sharedProps().entrySet()) {
            merged.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolvePropertyProviders(Map<String, Object> props, RenderContext renderContext) {
        if (props == null || props.isEmpty()) {
            return props != null ? props : new LinkedHashMap<>();
        }
        var result = new LinkedHashMap<String, Object>();
        for (var entry : props.entrySet()) {
            var value = entry.getValue();
            if (value instanceof ProvidesInertiaProperties provider) {
                try {
                    var provided = provider.toInertiaProperties(renderContext);
                    if (provided != null) {
                        result.put(entry.getKey(), resolvePropertyProviders(new LinkedHashMap<>(provided), renderContext));
                    } else {
                        result.put(entry.getKey(), null);
                    }
                } catch (Exception e) {
                    result.put(entry.getKey(), null);
                }
            } else if (value instanceof Map<?, ?> nestedMap) {
                result.put(entry.getKey(), resolvePropertyProviders((Map<String, Object>) nestedMap, renderContext));
            } else {
                result.put(entry.getKey(), value);
            }
        }
        return result;
    }

    private void applyOnceProps(Map<String, Object> merged) {
        var exceptKeys = oncePropRegistry.exceptOnceKeys();
        var onlyKeys = new LinkedHashSet<>(attrList(InertiaHeaderExtractor.CONTEXT_PARTIAL_DATA));
        for (var entry : oncePropRegistry.metadata().entrySet()) {
            if (exceptKeys.contains(entry.getKey())) {
                // Explicit partial reload request overrides Except-Once-Props.
                // Check against the actual prop key, not the tracking key.
                if (!onlyKeys.contains(entry.getValue().prop())) {
                    merged.remove(entry.getValue().prop());
                }
            }
        }
    }

    private void applyAlwaysProps(Map<String, Object> merged) {
        for (var entry : alwaysPropsMap(merged).entrySet()) {
            merged.put(entry.getKey(), entry.getValue());
        }
    }

    private Map<String, Object> alwaysPropsMap(Map<String, Object> merged) {
        var map = new LinkedHashMap<String, Object>();
        for (var entry : sharedDataRegistry.alwaysProps().entrySet()) {
            map.put(entry.getKey(), entry.getValue().value());
        }
        if (alwaysErrorsKey != null && merged.containsKey(alwaysErrorsKey)) {
            map.put(alwaysErrorsKey, merged.get(alwaysErrorsKey));
        }
        return map;
    }

    /**
     * The props that survive a partial reload unconditionally: only props
     * explicitly registered as "always". Regular shared props are treated
     * as ordinary props and subject to the only/except filter.
     *
     * @param merged the accumulated page props
     * @return the base props for the partial reload filter
     */
    private Map<String, Object> partialReloadBaseProps(Map<String, Object> merged) {
        return alwaysPropsMap(merged);
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<String>> collectDeferredProps(Map<String, Object> merged) {
        var registered = InertiaRequestContext.get(CONTEXT_DEFERRED);
        if (!(registered instanceof Map<?, ?> map) || map.isEmpty()) {
            return Map.of();
        }
        var groups = new LinkedHashMap<String, List<String>>();
        var isPartial = partialReloadProcessor.isPartialReload(partialComponent());
        var requested = isPartial ? requestedDeferredKeys((Map<String, DeferredProp<?>>) map) : null;
        for (var entry : ((Map<String, DeferredProp<?>>) map).entrySet()) {
            var deferred = entry.getValue();
            var members = groups.computeIfAbsent(deferred.group(), k -> new ArrayList<>());
            members.add(deferred.name());
            if (requested != null && requested.contains(deferred.name())) {
                resolveDeferred(merged, deferred);
            }
        }
        return groups;
    }

    @SuppressWarnings("unchecked")
    private java.util.Set<String> requestedDeferredKeys(Map<String, DeferredProp<?>> registered) {
        var data = partialReloadProcessor.partialData();
        if (!data.isEmpty()) {
            return new java.util.HashSet<>(data);
        }
        var except = partialReloadProcessor.partialExcept();
        if (!except.isEmpty()) {
            var keys = new java.util.HashSet<String>();
            for (var name : registered.keySet()) {
                if (!except.contains(name)) {
                    keys.add(name);
                }
            }
            return keys;
        }
        return java.util.Set.of();
    }

    private void resolveDeferred(Map<String, Object> merged, DeferredProp<?> deferred) {
        var name = deferred.name();
        if (!rescuedCandidates().contains(name)) {
            merged.put(name, deferred.resolve());
            return;
        }
        Object value;
        try {
            value = deferred.resolve();
        } catch (RuntimeException e) {
            markRescued(name);
            return;
        }
        if (value != null) {
            merged.put(name, value);
        } else {
            markRescued(name);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveLazyProps(Map<String, Object> props) {
        for (var entry : props.entrySet()) {
            if (entry.getValue() instanceof LazyProp lazy) {
                var name = entry.getKey();
                if (!rescuedCandidates().contains(name)) {
                    props.put(name, lazy.resolve());
                    continue;
                }
                Object value;
                try {
                    value = lazy.resolve();
                } catch (RuntimeException e) {
                    props.remove(name);
                    markRescued(name);
                    continue;
                }
                if (value != null) {
                    props.put(name, value);
                } else {
                    props.remove(name);
                    markRescued(name);
                }
            }
        }
        return props;
    }

    private Map<String, Object> unwrapOptionals(Map<String, Object> props, boolean isPartial) {
        // In a full page visit, optional props are not evaluated - they remain
        // as Optional and will be serialized as null (or omitted).
        // In a partial reload, only optional props that were selected by the
        // partial reload headers are unwrapped.
        if (!isPartial) {
            return props;
        }
        var onlyKeys = new LinkedHashSet<>(attrList(InertiaHeaderExtractor.CONTEXT_PARTIAL_DATA));
        var exceptKeys = new LinkedHashSet<>(attrList(InertiaHeaderExtractor.CONTEXT_PARTIAL_EXCEPT));
        var toRemove = new ArrayList<String>();
        var toAdd = new LinkedHashMap<String, Object>();
        for (var entry : props.entrySet()) {
            if (entry.getValue() instanceof Optional<?> optional) {
                var key = entry.getKey();
                // Resolve optional only if selected by only or not excluded by except
                boolean selected;
                if (!onlyKeys.isEmpty()) {
                    selected = onlyKeys.contains(key);
                } else if (!exceptKeys.isEmpty()) {
                    selected = !exceptKeys.contains(key);
                } else {
                    selected = true;
                }
                if (selected) {
                    toRemove.add(key);
                    optional.ifPresent(value -> toAdd.put(key, value));
                }
            }
        }
        toRemove.forEach(props::remove);
        props.putAll(toAdd);
        return props;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> applyScrollProps(Map<String, Object> merged) {
        var registered = InertiaRequestContext.get(CONTEXT_SCROLL_PROPS);
        if (!(registered instanceof Map<?, ?> map) || map.isEmpty()) {
            return Map.of();
        }
        var resetKeys = resetKeys();
        var intent = InertiaRequestContext.header("X-Inertia-Infinite-Scroll-Merge-Intent");
        var result = new LinkedHashMap<String, Map<String, Object>>();
        for (var entry : ((Map<String, ScrollProp>) map).entrySet()) {
            var key = entry.getKey();
            var spec = entry.getValue();
            var wrapper = spec.wrapperOrData();
            if (spec.value() != null) {
                merged.put(key, spec.value());
            }
            var mergePath = key + "." + wrapper;
            if ("prepend".equalsIgnoreCase(intent)) {
                addList(CONTEXT_PREPEND_PROPS, mergePath);
            } else {
                addList(CONTEXT_MERGE_PROPS, mergePath);
            }
            var metadata = spec.metadata() != null ? spec.metadata() : Map.of();
            var matchOn = metadata.get("matchOn");
            if (matchOn instanceof String s) {
                addList(CONTEXT_MATCH_PROPS_ON, mergePath + "." + s);
            } else if (matchOn instanceof List<?> list) {
                for (var path : list) {
                    addList(CONTEXT_MATCH_PROPS_ON, mergePath + "." + path);
                }
            }
            var clean = new LinkedHashMap<String, Object>();
            for (var name : List.of("merge", "previousPage", "nextPage", "currentPage", "pageName")) {
                if (metadata.containsKey(name)) {
                    clean.put(name, metadata.get(name));
                }
            }
            clean.put("reset", resetKeys.contains(key));
            result.put(key, clean);
        }
        return result;
    }

    private java.util.Set<String> resetKeys() {
        var value = InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_PARTIAL_RESET);
        if (value == null) {
            return java.util.Set.of();
        }
        var keys = new java.util.HashSet<String>();
        for (var part : String.valueOf(value).split(",")) {
            var trimmed = part.trim();
            if (!trimmed.isBlank()) {
                keys.add(trimmed);
            }
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
        return keys.stream()
            .filter(key -> resetKeys.stream().noneMatch(reset -> key.equals(reset) || key.startsWith(reset + ".")))
            .toList();
    }

    @SuppressWarnings("unchecked")
    private List<String> rescuedCandidates() {
        var stored = InertiaRequestContext.get(CONTEXT_RESCUED_CANDIDATES);
        if (stored instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private List<String> rescuedKeys() {
        var stored = InertiaRequestContext.get(CONTEXT_RESCUED_PROPS);
        if (stored instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private void markRescued(String key) {
        var stored = InertiaRequestContext.get(CONTEXT_RESCUED_PROPS);
        List<String> list;
        if (stored instanceof List<?> existing) {
            list = (List<String>) existing;
        } else {
            list = new ArrayList<>();
        }
        if (!list.contains(key)) {
            list.add(key);
        }
        InertiaRequestContext.set(CONTEXT_RESCUED_PROPS, list);
    }

    private boolean preserveFragmentEnabled() {
        var override = InertiaRequestContext.get(CONTEXT_PRESERVE_FRAGMENT);
        if (override != null) {
            return Boolean.TRUE.equals(override);
        }
        var request = InertiaRequestContext.request();
        if (request != null) {
            var session = request.getSession(false);
            if (session != null) {
                var stored = session.getAttribute(SESSION_PRESERVE_FRAGMENT);
                if (Boolean.TRUE.equals(stored)) {
                    session.removeAttribute(SESSION_PRESERVE_FRAGMENT);
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> meta() {
        var stored = InertiaRequestContext.get(CONTEXT_META);
        return stored instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(String attr) {
        var stored = InertiaRequestContext.get(attr);
        if (stored instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
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
        var value = InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_PARTIAL_COMPONENT);
        return value != null ? String.valueOf(value) : null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> camelize(Map<String, Object> props) {
        var result = new LinkedHashMap<String, Object>();
        for (var entry : props.entrySet()) {
            result.put(toCamelCase(entry.getKey()), camelizeValue(entry.getValue()));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Object camelizeValue(Object value) {
        if (value instanceof Map) {
            var map = (Map<String, Object>) value;
            var result = new LinkedHashMap<String, Object>();
            for (var entry : map.entrySet()) {
                result.put(toCamelCase(entry.getKey()), camelizeValue(entry.getValue()));
            }
            return result;
        }
        if (value instanceof List) {
            return ((List<?>) value).stream().map(PageObjectBuilder::camelizeValue).toList();
        }
        return value;
    }

    private static String toCamelCase(String key) {
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

    private static List<String> attrList(String name) {
        var value = InertiaRequestContext.get(name);
        if (value == null) {
            return List.of();
        }
        var list = new ArrayList<String>();
        for (var part : String.valueOf(value).split(",")) {
            var trimmed = part.trim();
            if (!trimmed.isBlank()) {
                list.add(trimmed);
            }
        }
        return list;
    }
}