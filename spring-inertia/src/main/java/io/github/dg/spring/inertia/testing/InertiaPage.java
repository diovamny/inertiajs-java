package io.github.dg.spring.inertia.testing;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.lang.reflect.Array;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;

/**
 * Deserialized view of the Inertia page payload for tests with a rich,
 * fluent assertion DSL mirroring Laravel's AssertableInertia.
 *
 * <p>Supports both passive assertions on response payloads and active partial
 * reload requests via {@link #reloadOnly(String...)}, {@link #reloadExcept(String...)},
 * and {@link #loadDeferredProps()}.</p>
 *
 * <pre>{@code
 * InertiaPage page = InertiaPage.from(mockMvc, "/users");
 * page.assertComponent("Users/Index")
 *     .assertPropCount("users", 10)
 *     .assertMissing("secret")
 *     .loadDeferredProps("stats", reloaded -> {
 *         reloaded.assertProp("analytics.total", 100);
 *     });
 * }</pre>
 */
public final class InertiaPage {

    private static final ObjectMapper DEFAULT = new ObjectMapper();

    private final String component;
    private final Map<String, Object> props;
    private final String url;
    private final String version;
    private final Map<String, Object> flash;
    private final Map<String, List<String>> deferredProps;
    private final List<String> mergeProps;
    private final List<String> prependProps;
    private final List<String> deepMergeProps;
    private final List<String> matchPropsOn;
    private final Map<String, Object> onceProps;
    private final Map<String, Object> scrollProps;
    private final List<String> sharedProps;
    private final List<String> rescuedProps;
    private final Map<String, Object> meta;
    private final Boolean encryptHistory;
    private final Boolean clearHistory;
    private final Boolean preserveFragment;
    private final InertiaReloadExecutor reloadExecutor;

    public InertiaPage(
            String component,
            Map<String, Object> props,
            String url,
            String version,
            Map<String, Object> flash) {
        this(component, props, url, version, flash, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public InertiaPage(
            String component,
            Map<String, Object> props,
            String url,
            String version,
            Map<String, Object> flash,
            Map<String, List<String>> deferredProps,
            List<String> mergeProps,
            List<String> prependProps,
            List<String> deepMergeProps,
            List<String> matchPropsOn,
            Map<String, Object> onceProps,
            Map<String, Object> scrollProps,
            List<String> sharedProps,
            List<String> rescuedProps,
            Map<String, Object> meta,
            Boolean encryptHistory,
            Boolean clearHistory,
            Boolean preserveFragment) {
        this(component, props, url, version, flash, deferredProps, mergeProps, prependProps, deepMergeProps, matchPropsOn,
                onceProps, scrollProps, sharedProps, rescuedProps, meta, encryptHistory, clearHistory, preserveFragment, null);
    }

    public InertiaPage(
            String component,
            Map<String, Object> props,
            String url,
            String version,
            Map<String, Object> flash,
            Map<String, List<String>> deferredProps,
            List<String> mergeProps,
            List<String> prependProps,
            List<String> deepMergeProps,
            List<String> matchPropsOn,
            Map<String, Object> onceProps,
            Map<String, Object> scrollProps,
            List<String> sharedProps,
            List<String> rescuedProps,
            Map<String, Object> meta,
            Boolean encryptHistory,
            Boolean clearHistory,
            Boolean preserveFragment,
            InertiaReloadExecutor reloadExecutor) {
        this.component = component;
        this.props = props != null ? props : Map.of();
        this.url = url;
        this.version = version;
        this.flash = flash;
        this.deferredProps = deferredProps != null ? deferredProps : Map.of();
        this.mergeProps = mergeProps != null ? mergeProps : List.of();
        this.prependProps = prependProps != null ? prependProps : List.of();
        this.deepMergeProps = deepMergeProps != null ? deepMergeProps : List.of();
        this.matchPropsOn = matchPropsOn != null ? matchPropsOn : List.of();
        this.onceProps = onceProps != null ? onceProps : Map.of();
        this.scrollProps = scrollProps != null ? scrollProps : Map.of();
        this.sharedProps = sharedProps != null ? sharedProps : List.of();
        this.rescuedProps = rescuedProps != null ? rescuedProps : List.of();
        this.meta = meta != null ? meta : Map.of();
        this.encryptHistory = encryptHistory;
        this.clearHistory = clearHistory;
        this.preserveFragment = preserveFragment;
        this.reloadExecutor = reloadExecutor;
    }

    // --- Factory Methods ---

    /**
     * Parse a page payload using the default ObjectMapper.
     *
     * @param json the JSON document
     * @return the parsed page
     */
    public static InertiaPage fromJson(String json) {
        return fromJson(json, DEFAULT);
    }

    /**
     * Parse a page payload with a custom mapper.
     *
     * @param json   the JSON document
     * @param mapper the mapper to use
     * @return the parsed page
     */
    public static InertiaPage fromJson(String json, ObjectMapper mapper) {
        try {
            var node = mapper.readTree(json);
            Map<String, Object> props = node.has("props")
                ? mapper.convertValue(node.path("props"), new TypeReference<Map<String, Object>>() { })
                : new LinkedHashMap<>();
            Map<String, Object> flash = node.has("flash")
                ? mapper.convertValue(node.path("flash"), new TypeReference<Map<String, Object>>() { })
                : null;
            Map<String, List<String>> deferredProps = node.has("deferredProps")
                ? mapper.convertValue(node.path("deferredProps"), new TypeReference<Map<String, List<String>>>() { })
                : null;
            List<String> mergeProps = node.has("mergeProps")
                ? mapper.convertValue(node.path("mergeProps"), new TypeReference<List<String>>() { })
                : null;
            List<String> prependProps = node.has("prependProps")
                ? mapper.convertValue(node.path("prependProps"), new TypeReference<List<String>>() { })
                : null;
            List<String> deepMergeProps = node.has("deepMergeProps")
                ? mapper.convertValue(node.path("deepMergeProps"), new TypeReference<List<String>>() { })
                : null;
            List<String> matchPropsOn = node.has("matchPropsOn")
                ? mapper.convertValue(node.path("matchPropsOn"), new TypeReference<List<String>>() { })
                : null;
            Map<String, Object> onceProps = node.has("onceProps")
                ? mapper.convertValue(node.path("onceProps"), new TypeReference<Map<String, Object>>() { })
                : null;
            Map<String, Object> scrollProps = node.has("scrollProps")
                ? mapper.convertValue(node.path("scrollProps"), new TypeReference<Map<String, Object>>() { })
                : null;
            List<String> sharedProps = node.has("sharedProps")
                ? mapper.convertValue(node.path("sharedProps"), new TypeReference<List<String>>() { })
                : null;
            List<String> rescuedProps = node.has("rescuedProps")
                ? mapper.convertValue(node.path("rescuedProps"), new TypeReference<List<String>>() { })
                : null;
            Map<String, Object> meta = node.has("meta")
                ? mapper.convertValue(node.path("meta"), new TypeReference<Map<String, Object>>() { })
                : null;
            Boolean encryptHistory = node.has("encryptHistory") ? node.path("encryptHistory").asBoolean() : null;
            Boolean clearHistory = node.has("clearHistory") ? node.path("clearHistory").asBoolean() : null;
            Boolean preserveFragment = node.has("preserveFragment") ? node.path("preserveFragment").asBoolean() : null;

            return new InertiaPage(
                node.path("component").asString(null),
                props,
                node.path("url").asString(null),
                node.has("version") && !node.path("version").isNull() ? node.path("version").asString() : null,
                flash,
                deferredProps,
                mergeProps,
                prependProps,
                deepMergeProps,
                matchPropsOn,
                onceProps,
                scrollProps,
                sharedProps,
                rescuedProps,
                meta,
                encryptHistory,
                clearHistory,
                preserveFragment,
                null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Inertia page JSON: " + json, e);
        }
    }

    /**
     * Create an InertiaPage from a Spring MVC test MvcResult.
     *
     * @param result the test result
     * @return parsed InertiaPage
     */
    public static InertiaPage from(MvcResult result) {
        Objects.requireNonNull(result, "MvcResult must not be null");
        String body = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        return fromJson(body);
    }

    /**
     * Create an InertiaPage from a Spring MVC test MvcResult, attaching the MockMvc instance for reload operations.
     *
     * @param mockMvc the test client
     * @param result  the test result
     * @return parsed InertiaPage with active reload capabilities
     */
    public static InertiaPage from(MockMvc mockMvc, MvcResult result) {
        return from(result).withClient(mockMvc);
    }

    /**
     * Perform an initial GET request with X-Inertia: true and parse the resulting InertiaPage.
     *
     * @param mockMvc the test client
     * @param url     the initial request URL
     * @return parsed InertiaPage with active reload capabilities
     */
    public static InertiaPage from(MockMvc mockMvc, String url) {
        Objects.requireNonNull(mockMvc, "mockMvc must not be null");
        Objects.requireNonNull(url, "url must not be null");
        try {
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url).header("X-Inertia", "true")).andReturn();
            return from(mockMvc, result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform initial Inertia GET to: " + url, e);
        }
    }

    /**
     * Attach a MockMvc client to enable reload operations.
     *
     * @param mockMvc the MockMvc instance
     * @return new InertiaPage with active reload capabilities
     */
    public InertiaPage withClient(MockMvc mockMvc) {
        Objects.requireNonNull(mockMvc, "mockMvc must not be null");
        return withExecutor((targetUrl, targetComp, targetVer, only, except) -> {
            try {
                MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(targetUrl)
                        .header("X-Inertia", "true");
                if (targetVer != null && !targetVer.isBlank()) {
                    builder.header("X-Inertia-Version", targetVer);
                }
                if (targetComp != null && !targetComp.isBlank()) {
                    builder.header("X-Inertia-Partial-Component", targetComp);
                }
                if (only != null && !only.isEmpty()) {
                    builder.header("X-Inertia-Partial-Data", String.join(",", only));
                }
                if (except != null && !except.isEmpty()) {
                    builder.header("X-Inertia-Partial-Except", String.join(",", except));
                }
                MvcResult reloadResult = mockMvc.perform(builder).andReturn();
                return from(mockMvc, reloadResult);
            } catch (Exception e) {
                throw new RuntimeException("Failed to execute reload request to: " + targetUrl, e);
            }
        });
    }

    /**
     * Attach a custom reload executor.
     *
     * @param executor the reload executor
     * @return new InertiaPage with the executor attached
     */
    public InertiaPage withExecutor(InertiaReloadExecutor executor) {
        return new InertiaPage(
            component, props, url, version, flash, deferredProps, mergeProps,
            prependProps, deepMergeProps, matchPropsOn, onceProps, scrollProps,
            sharedProps, rescuedProps, meta, encryptHistory, clearHistory,
            preserveFragment, executor
        );
    }

    // --- Active Reload Operations (Laravel/Rails Parity) ---

    /**
     * Execute a partial reload request with custom only and except filters.
     *
     * @param only       props to include (null or empty for all)
     * @param except     props to exclude (null or empty for none)
     * @param callback   optional assertion callback on the reloaded page
     * @return the reloaded InertiaPage
     */
    public InertiaPage reload(List<String> only, List<String> except, Consumer<InertiaPage> callback) {
        if (reloadExecutor == null) {
            throw new IllegalStateException("No InertiaReloadExecutor configured. Call .withClient(mockMvc) or InertiaPage.from(mockMvc, ...) to enable reload operations.");
        }
        InertiaPage reloaded = reloadExecutor.execute(this.url, this.component, this.version, only, except);
        if (callback != null) {
            callback.accept(reloaded);
        }
        return reloaded;
    }

    /**
     * Reload the page requesting ONLY the specified prop keys.
     *
     * @param props prop keys to request
     * @return the reloaded page
     */
    public InertiaPage reloadOnly(String... props) {
        return reloadOnly(props != null ? List.of(props) : List.of(), null);
    }

    /**
     * Reload the page requesting ONLY the specified prop keys.
     *
     * @param props prop keys to request
     * @return the reloaded page
     */
    public InertiaPage reloadOnly(List<String> props) {
        return reloadOnly(props, null);
    }

    /**
     * Reload the page requesting ONLY the specified prop keys, running assertions on the reloaded page.
     *
     * @param props    prop keys to request
     * @param callback assertion callback
     * @return the reloaded page
     */
    public InertiaPage reloadOnly(List<String> props, Consumer<InertiaPage> callback) {
        return reload(props, null, reloaded -> {
            if (props != null) {
                for (String p : props) {
                    reloaded.assertPropExists(p);
                }
            }
            if (callback != null) {
                callback.accept(reloaded);
            }
        });
    }

    /**
     * Reload the page EXCLUDING the specified prop keys.
     *
     * @param props prop keys to exclude
     * @return the reloaded page
     */
    public InertiaPage reloadExcept(String... props) {
        return reloadExcept(props != null ? List.of(props) : List.of(), null);
    }

    /**
     * Reload the page EXCLUDING the specified prop keys.
     *
     * @param props prop keys to exclude
     * @return the reloaded page
     */
    public InertiaPage reloadExcept(List<String> props) {
        return reloadExcept(props, null);
    }

    /**
     * Reload the page EXCLUDING the specified prop keys, running assertions on the reloaded page.
     *
     * @param props    prop keys to exclude
     * @param callback assertion callback
     * @return the reloaded page
     */
    public InertiaPage reloadExcept(List<String> props, Consumer<InertiaPage> callback) {
        return reload(null, props, reloaded -> {
            if (props != null) {
                for (String p : props) {
                    reloaded.assertNoProp(p);
                }
            }
            if (callback != null) {
                callback.accept(reloaded);
            }
        });
    }

    /**
     * Request all deferred props across all groups.
     *
     * @return the reloaded page
     */
    public InertiaPage loadDeferredProps() {
        return loadDeferredProps(null, null);
    }

    /**
     * Request all deferred props across all groups, running assertions on the reloaded page.
     *
     * @param callback assertion callback
     * @return the reloaded page
     */
    public InertiaPage loadDeferredProps(Consumer<InertiaPage> callback) {
        return loadDeferredProps(null, callback);
    }

    /**
     * Request deferred props belonging to the specified group.
     *
     * @param group group name (e.g. "default")
     * @return the reloaded page
     */
    public InertiaPage loadDeferredProps(String group) {
        return loadDeferredProps(group, null);
    }

    /**
     * Request deferred props belonging to the specified group, running assertions on the reloaded page.
     *
     * @param group    group name (or null for all groups)
     * @param callback assertion callback
     * @return the reloaded page
     */
    public InertiaPage loadDeferredProps(String group, Consumer<InertiaPage> callback) {
        if (this.deferredProps == null || this.deferredProps.isEmpty()) {
            return this;
        }
        List<String> propsToLoad;
        if (group == null || group.isBlank()) {
            propsToLoad = this.deferredProps.values().stream().flatMap(List::stream).toList();
        } else {
            propsToLoad = this.deferredProps.getOrDefault(group, List.of());
        }
        if (propsToLoad.isEmpty()) {
            return this;
        }
        return reloadOnly(propsToLoad, callback);
    }

    // --- Getters ---

    public String component() {
        return component;
    }

    public Map<String, Object> props() {
        return props;
    }

    public String url() {
        return url;
    }

    public String version() {
        return version;
    }

    public Map<String, Object> flash() {
        return flash;
    }

    public Map<String, List<String>> deferredProps() {
        return deferredProps;
    }

    public List<String> mergeProps() {
        return mergeProps;
    }

    public List<String> prependProps() {
        return prependProps;
    }

    public List<String> deepMergeProps() {
        return deepMergeProps;
    }

    public List<String> matchPropsOn() {
        return matchPropsOn;
    }

    public Map<String, Object> onceProps() {
        return onceProps;
    }

    public Map<String, Object> scrollProps() {
        return scrollProps;
    }

    public List<String> sharedProps() {
        return sharedProps;
    }

    public List<String> rescuedProps() {
        return rescuedProps;
    }

    public Map<String, Object> meta() {
        return meta;
    }

    public Boolean encryptHistory() {
        return encryptHistory;
    }

    public Boolean clearHistory() {
        return clearHistory;
    }

    public Boolean preserveFragment() {
        return preserveFragment;
    }

    public boolean hasProp(String key) {
        return prop(key) != null;
    }

    public Object prop(String key) {
        if (key == null) return null;
        if (!key.contains(".")) {
            return props.get(key);
        }
        String[] parts = key.split("\\.");
        Object current = props;
        for (String part : parts) {
            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else {
                return null;
            }
        }
        return current;
    }

    public Object flash(String key) {
        return flash != null ? flash.get(key) : null;
    }

    // --- Assertions DSL ---

    public InertiaPage assertComponent(String expected) {
        if (!Objects.equals(expected, component)) {
            throw new AssertionError("Expected component <" + expected + "> but was <" + component + ">");
        }
        return this;
    }

    public InertiaPage assertUrl(String expected) {
        if (!Objects.equals(expected, url)) {
            throw new AssertionError("Expected url <" + expected + "> but was <" + url + ">");
        }
        return this;
    }

    public InertiaPage assertVersion(String expected) {
        if (!Objects.equals(expected, version)) {
            throw new AssertionError("Expected version <" + expected + "> but was <" + version + ">");
        }
        return this;
    }

    public InertiaPage assertProp(String key, Object expected) {
        var actual = prop(key);
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Expected prop <" + key + "> to equal <" + expected + "> but was <" + actual + ">");
        }
        return this;
    }

    public InertiaPage assertPropExists(String key) {
        if (!hasProp(key)) {
            throw new AssertionError("Expected prop <" + key + "> to exist");
        }
        return this;
    }

    public InertiaPage assertNoProp(String key) {
        if (hasProp(key)) {
            throw new AssertionError("Expected prop <" + key + "> not to exist, but was <" + prop(key) + ">");
        }
        return this;
    }

    /**
     * Alias for {@link #assertNoProp(String)}, matching Laravel's {@code missing()} assertion.
     */
    public InertiaPage assertMissing(String key) {
        return assertNoProp(key);
    }

    /**
     * Assert that a collection, map, or array prop has the expected element count.
     *
     * @param key          prop key
     * @param expectedSize expected number of items
     * @return this
     */
    public InertiaPage assertPropCount(String key, int expectedSize) {
        var val = prop(key);
        if (val == null) {
            throw new AssertionError("Expected prop '" + key + "' to have count <" + expectedSize + "> but prop is absent/null");
        }
        int actualSize;
        if (val instanceof Collection<?> col) {
            actualSize = col.size();
        } else if (val instanceof Map<?, ?> map) {
            actualSize = map.size();
        } else if (val.getClass().isArray()) {
            actualSize = Array.getLength(val);
        } else {
            throw new AssertionError("Expected prop '" + key + "' to be a Collection, Map or Array, but was " + val.getClass().getName());
        }
        if (actualSize != expectedSize) {
            throw new AssertionError("Expected prop '" + key + "' to have count <" + expectedSize + "> but was <" + actualSize + ">");
        }
        return this;
    }

    /**
     * Execute assertions against a nested Map prop.
     *
     * @param key       prop key
     * @param assertions consumer accepting the nested map
     * @return this
     */
    @SuppressWarnings("unchecked")
    public InertiaPage assertPropMap(String key, Consumer<Map<String, Object>> assertions) {
        var val = prop(key);
        if (!(val instanceof Map<?, ?> map)) {
            throw new AssertionError("Expected prop '" + key + "' to be a Map, but was: " + (val == null ? "null" : val.getClass().getName()));
        }
        assertions.accept((Map<String, Object>) map);
        return this;
    }

    public InertiaPage assertHasProps(String... keys) {
        for (String key : keys) {
            assertPropExists(key);
        }
        return this;
    }

    public InertiaPage assertHasProps(Map<String, Object> expected) {
        for (var entry : expected.entrySet()) {
            assertProp(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public InertiaPage assertHasExactProps(Map<String, Object> expected) {
        if (!Objects.equals(expected, props)) {
            throw new AssertionError("Expected exact props <" + expected + "> but was <" + props + ">");
        }
        return this;
    }

    public InertiaPage assertDeferredProps(String... keys) {
        for (String key : keys) {
            boolean found = deferredProps.values().stream().anyMatch(list -> list.contains(key));
            if (!found) {
                throw new AssertionError("Expected deferred prop <" + key + "> to exist");
            }
        }
        return this;
    }

    public InertiaPage assertDeferredPropsInGroup(String group, String... keys) {
        var list = deferredProps.get(group);
        if (list == null) {
            throw new AssertionError("Expected deferred prop group <" + group + "> to exist");
        }
        for (String key : keys) {
            if (!list.contains(key)) {
                throw new AssertionError("Expected deferred prop <" + key + "> in group <" + group + ">");
            }
        }
        return this;
    }

    public InertiaPage assertNoDeferredProps() {
        if (!deferredProps.isEmpty()) {
            throw new AssertionError("Expected no deferred props, but was <" + deferredProps + ">");
        }
        return this;
    }

    public InertiaPage assertMergeProps(String... keys) {
        for (String key : keys) {
            if (!mergeProps.contains(key)) {
                throw new AssertionError("Expected merge prop <" + key + "> to exist");
            }
        }
        return this;
    }

    public InertiaPage assertPrependProps(String... keys) {
        for (String key : keys) {
            if (!prependProps.contains(key)) {
                throw new AssertionError("Expected prepend prop <" + key + "> to exist");
            }
        }
        return this;
    }

    public InertiaPage assertDeepMergeProps(String... keys) {
        for (String key : keys) {
            if (!deepMergeProps.contains(key)) {
                throw new AssertionError("Expected deepMerge prop <" + key + "> to exist");
            }
        }
        return this;
    }

    public InertiaPage assertMatchPropsOn(String... fields) {
        for (String field : fields) {
            if (!matchPropsOn.contains(field)) {
                throw new AssertionError("Expected matchPropsOn <" + field + "> to exist");
            }
        }
        return this;
    }

    public InertiaPage assertOnceProps(String... keys) {
        for (String key : keys) {
            if (!onceProps.containsKey(key)) {
                throw new AssertionError("Expected once prop <" + key + "> to exist");
            }
        }
        return this;
    }

    public InertiaPage assertNoOnceProps() {
        if (!onceProps.isEmpty()) {
            throw new AssertionError("Expected no once props, but was <" + onceProps + ">");
        }
        return this;
    }

    public InertiaPage assertScrollProps(String... keys) {
        for (String key : keys) {
            if (!scrollProps.containsKey(key)) {
                throw new AssertionError("Expected scroll prop <" + key + "> to exist");
            }
        }
        return this;
    }

    public InertiaPage assertSharedProps(String... keys) {
        for (String key : keys) {
            if (!sharedProps.contains(key)) {
                throw new AssertionError("Expected shared prop <" + key + "> to exist");
            }
        }
        return this;
    }

    public InertiaPage assertRescuedProps(String... keys) {
        for (String key : keys) {
            if (!rescuedProps.contains(key)) {
                throw new AssertionError("Expected rescued prop <" + key + "> to exist");
            }
        }
        return this;
    }

    public InertiaPage assertMeta(String key, Object expected) {
        var actual = meta.get(key);
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Expected meta <" + key + "> to equal <" + expected + "> but was <" + actual + ">");
        }
        return this;
    }

    public InertiaPage assertEncryptHistory(boolean expected) {
        if (!Objects.equals(Boolean.valueOf(expected), encryptHistory != null ? encryptHistory : Boolean.FALSE)) {
            throw new AssertionError("Expected encryptHistory to be <" + expected + "> but was <" + encryptHistory + ">");
        }
        return this;
    }

    public InertiaPage assertClearHistory(boolean expected) {
        if (!Objects.equals(Boolean.valueOf(expected), clearHistory != null ? clearHistory : Boolean.FALSE)) {
            throw new AssertionError("Expected clearHistory to be <" + expected + "> but was <" + clearHistory + ">");
        }
        return this;
    }

    public InertiaPage assertPreserveFragment(boolean expected) {
        if (!Objects.equals(Boolean.valueOf(expected), preserveFragment != null ? preserveFragment : Boolean.FALSE)) {
            throw new AssertionError("Expected preserveFragment to be <" + expected + "> but was <" + preserveFragment + ">");
        }
        return this;
    }

    public InertiaPage assertFlash(String key, Object expected) {
        var actual = flash(key);
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Expected flash <" + key + "> to equal <" + expected + "> but was <" + actual + ">");
        }
        return this;
    }

    public InertiaPage dump() {
        System.out.println("=== [InertiaPage Dump] ===");
        System.out.println("Component: " + component);
        System.out.println("Props: " + props);
        System.out.println("URL: " + url);
        System.out.println("Version: " + version);
        System.out.println("Flash: " + flash);
        System.out.println("DeferredProps: " + deferredProps);
        System.out.println("MergeProps: " + mergeProps);
        System.out.println("PrependProps: " + prependProps);
        System.out.println("DeepMergeProps: " + deepMergeProps);
        System.out.println("MatchPropsOn: " + matchPropsOn);
        System.out.println("OnceProps: " + onceProps);
        System.out.println("ScrollProps: " + scrollProps);
        System.out.println("SharedProps: " + sharedProps);
        System.out.println("RescuedProps: " + rescuedProps);
        System.out.println("Meta: " + meta);
        System.out.println("EncryptHistory: " + encryptHistory);
        System.out.println("ClearHistory: " + clearHistory);
        System.out.println("PreserveFragment: " + preserveFragment);
        System.out.println("==========================");
        return this;
    }

    public InertiaPage dump(String key) {
        System.out.println("=== [InertiaPage Dump Prop: " + key + "] ===");
        System.out.println(prop(key));
        System.out.println("==========================================");
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InertiaPage that = (InertiaPage) o;
        return Objects.equals(component, that.component) &&
               Objects.equals(props, that.props) &&
               Objects.equals(url, that.url) &&
               Objects.equals(version, that.version) &&
               Objects.equals(flash, that.flash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(component, props, url, version, flash);
    }

    @Override
    public String toString() {
        return "InertiaPage[component=" + component + ", props=" + props + ", url=" + url + ", version=" + version + ", flash=" + flash + "]";
    }
}
