package io.github.dg.spring.inertia.protocol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.context.annotation.RequestScope;

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
    public static final String CONTEXT_CAMELIZE_PROPS = "inertia-camelize-props";

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

        injectFlash(merged);
        injectValidationErrors(merged, alwaysIncludeErrors);
        injectSharedProps(merged);
        applyOnceProps(merged);
        applyAlwaysProps(merged);

        var deferredProps = collectDeferredProps(merged);
        var scrollProps = applyScrollProps(merged);
        var mergeProps = stringList(CONTEXT_MERGE_PROPS);
        var prependProps = stringList(CONTEXT_PREPEND_PROPS);
        var deepMergeProps = stringList(CONTEXT_DEEP_MERGE_PROPS);
        var matchPropsOn = stringList(CONTEXT_MATCH_PROPS_ON);

        var partial = partialReloadProcessor.isPartialReload(component);
        if (partial) {
            if (partialReloadProcessor.isPartialReset()) {
                mergePropProcessor.reset();
            } else {
                merged = mergePropProcessor.mergeProps(merged, mergeProps, prependProps, deepMergeProps, matchPropsOn);
            }
            merged = partialReloadProcessor.filterProps(merged, alwaysPropsMap(merged));
            mergePropProcessor.propagateProps(merged);
        } else {
            mergePropProcessor.propagateProps(merged);
        }

        merged = resolveLazyProps(merged);
        merged = unwrapOptionals(merged);

        var url = urlResolver != null
            ? urlResolver.resolve(InertiaRequestContext.uri())
            : InertiaRequestContext.uri();
        var version = resolveVersion();

        InertiaRequestContext.set(CONTEXT_PAGE_VERSION, version);
        var clientVersion = InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_VERSION);
        InertiaRequestContext.set(CONTEXT_VERSION_MISMATCH,
            "GET".equalsIgnoreCase(InertiaRequestContext.method())
                && clientVersion != null
                && version != null
                && !version.equals(String.valueOf(clientVersion)));

        var flash = flashStore.hasData() ? flashStore.drain() : null;
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
            false,
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

    private void injectValidationErrors(Map<String, Object> merged, boolean alwaysIncludeErrors) {
        var bag = InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_ERROR_BAG);
        var key = bag != null ? String.valueOf(bag) : "errors";
        var errors = InertiaRequestContext.get(CONTEXT_ERRORS);
        if (errors instanceof Map<?, ?> map && !map.isEmpty()) {
            merged.put(key, new LinkedHashMap<>(map));
            alwaysErrorsKey = key;
        } else if (alwaysIncludeErrors) {
            merged.putIfAbsent(key, Map.of());
        }
    }

    private void injectFlash(Map<String, Object> merged) {
        if (!flashStore.hasData()) {
            return;
        }
        for (var entry : flashStore.drain().entrySet()) {
            merged.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    private void injectSharedProps(Map<String, Object> merged) {
        for (var entry : sharedDataRegistry.sharedProps().entrySet()) {
            merged.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    private void applyOnceProps(Map<String, Object> merged) {
        for (var entry : oncePropRegistry.metadata().entrySet()) {
            if (oncePropRegistry.alreadyShown(entry.getKey())) {
                merged.remove(entry.getValue().prop());
            } else {
                oncePropRegistry.markShown(entry.getKey());
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

    private Map<String, Object> unwrapOptionals(Map<String, Object> props) {
        var toRemove = new ArrayList<String>();
        var toAdd = new LinkedHashMap<String, Object>();
        for (var entry : props.entrySet()) {
            if (entry.getValue() instanceof Optional<?> optional) {
                toRemove.add(entry.getKey());
                optional.ifPresent(value -> toAdd.put(entry.getKey(), value));
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

    private static Map<String, Object> camelize(Map<String, Object> props) {
        var result = new LinkedHashMap<String, Object>();
        for (var entry : props.entrySet()) {
            result.put(toSnakeCase(entry.getKey()), entry.getValue());
        }
        return result;
    }

    private static String toSnakeCase(String key) {
        var sb = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            var c = key.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}