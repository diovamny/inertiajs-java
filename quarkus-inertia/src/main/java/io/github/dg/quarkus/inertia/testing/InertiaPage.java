package io.github.dg.quarkus.inertia.testing;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dg.quarkus.inertia.model.PageObject;

/**
 * Read model over an Inertia page object for use in tests: parse the JSON
 * payload of an Inertia response and assert on its component, props and
 * metadata with a fluent API.
 *
 * <pre>{@code
 * InertiaPage page = InertiaPage.fromJson(html.getBody());
 * page.assertComponent("Dashboard")
 *     .assertProp("users", expectedUsers)
 *     .assertNoDeferredProps();
 * }</pre>
 */
public final class InertiaPage {

    private final PageObject page;
    private final InertiaReloadExecutor reloadExecutor;

    private InertiaPage(PageObject page) {
        this(page, null);
    }

    private InertiaPage(PageObject page, InertiaReloadExecutor reloadExecutor) {
        this.page = page;
        this.reloadExecutor = reloadExecutor;
    }

    /**
     * Parse a page object from a JSON document using a default
     * {@link ObjectMapper}.
     *
     * @param json the JSON payload of an Inertia response
     * @return the parsed page
     * @throws IllegalArgumentException when the JSON cannot be parsed
     */
    public static InertiaPage fromJson(String json) {
        return fromJson(json, new ObjectMapper());
    }

    /**
     * Parse a page object from a JSON document using a custom mapper.
     *
     * @param json   the JSON payload of an Inertia response
     * @param mapper the mapper used for deserialization
     * @return the parsed page
     * @throws IllegalArgumentException when the JSON cannot be parsed
     */
    public static InertiaPage fromJson(String json, ObjectMapper mapper) {
        try {
            return new InertiaPage(mapper.readValue(json, PageObject.class));
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse Inertia page JSON", e);
        }
    }

    /**
     * Wrap an existing page object.
     *
     * @param page the page object
     * @return the wrapper
     */
    public static InertiaPage from(PageObject page) {
        return new InertiaPage(page);
    }

    /**
     * Attach a custom reload executor.
     *
     * @param executor the reload executor
     * @return new InertiaPage with the executor attached
     */
    public InertiaPage withExecutor(InertiaReloadExecutor executor) {
        return new InertiaPage(this.page, executor);
    }

    // --- Active Reload Operations (Laravel/Rails Parity) ---

    /**
     * Execute a partial reload request with custom only and except filters.
     *
     * @param only     props to include (null or empty for all)
     * @param except   props to exclude (null or empty for none)
     * @param callback optional assertion callback on the reloaded page
     * @return the reloaded InertiaPage
     */
    public InertiaPage reload(List<String> only, List<String> except, Consumer<InertiaPage> callback) {
        if (reloadExecutor == null) {
            throw new IllegalStateException("No InertiaReloadExecutor configured. Call .withExecutor(...) or use a client factory to enable reload operations.");
        }
        InertiaPage reloaded = reloadExecutor.execute(this.url(), this.component(), this.version(), only, except);
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
     * Request deferred props in the specified group (or all groups if group is null).
     *
     * @param group the deferred group name, or null for all groups
     * @return the reloaded page
     */
    public InertiaPage loadDeferredProps(String group) {
        return loadDeferredProps(group, null);
    }

    /**
     * Request deferred props in the specified group, running assertions on the reloaded page.
     *
     * @param group    the deferred group name, or null for all groups
     * @param callback assertion callback
     * @return the reloaded page
     */
    public InertiaPage loadDeferredProps(String group, Consumer<InertiaPage> callback) {
        List<String> targetProps;
        if (group != null) {
            targetProps = deferredProps().get(group);
            if (targetProps == null || targetProps.isEmpty()) {
                throw new AssertionError("Deferred group <" + group + "> not found in page deferred props: " + deferredProps());
            }
        } else {
            targetProps = deferredProps().values().stream().flatMap(List::stream).distinct().toList();
            if (targetProps.isEmpty()) {
                throw new AssertionError("No deferred props declared on the page to load.");
            }
        }

        return reload(targetProps, null, reloaded -> {
            for (String prop : targetProps) {
                reloaded.assertPropExists(prop);
            }
            if (callback != null) {
                callback.accept(reloaded);
            }
        });
    }

    /**
     * The underlying page object.
     *
     * @return the wrapped page object
     */
    public PageObject toPageObject() {
        return page;
    }

    /**
     * The frontend component name.
     *
     * @return the component name
     */
    public String component() {
        return page.component();
    }

    /**
     * The resolved URL of the page.
     *
     * @return the URL
     */
    public String url() {
        return page.url();
    }

    /**
     * The asset version of the page.
     *
     * @return the version
     */
    public String version() {
        return page.version();
    }

    /**
     * Whether a prop with the given key exists.
     *
     * @param key the prop key
     * @return {@code true} when present
     */
    public boolean hasProp(String key) {
        return page.props().containsKey(key);
    }

    /**
     * The value of a prop, or {@code null} when absent.
     *
     * @param key the prop key
     * @return the value
     */
    public Object prop(String key) {
        return page.props().get(key);
    }

    /**
     * All page props.
     *
     * @return the props map
     */
    public Map<String, Object> props() {
        return page.props();
    }

    /**
     * The flash data map delivered to the client, empty when none.
     *
     * @return the flash map
     */
    public Map<String, Object> flash() {
        return page.flash() != null ? page.flash() : Map.of();
    }

    /**
     * The value of a flash key.
     *
     * @param key the flash key
     * @return the value or {@code null}
     */
    public Object flash(String key) {
        return page.flash() != null ? page.flash().get(key) : null;
    }

    /**
     * Whether the client must encrypt history.
     *
     * @return the encryptHistory flag
     */
    public Boolean encryptHistory() {
        return page.encryptHistory();
    }

    /**
     * Whether the client must clear history.
     *
     * @return the clearHistory flag
     */
    public Boolean clearHistory() {
        return page.clearHistory();
    }

    /**
     * Whether the client must preserve URL fragment.
     *
     * @return the preserveFragment flag
     */
    public Boolean preserveFragment() {
        return page.preserveFragment();
    }

    /**
     * The deferred groups ({@code group -> member names}), empty when none.
     *
     * @return the deferred props
     */
    public Map<String, List<String>> deferredProps() {
        return page.deferredProps() != null ? page.deferredProps() : Map.of();
    }

    /**
     * The mergeable prop names, empty when none.
     *
     * @return the merge props
     */
    public List<String> mergeProps() {
        return page.mergeProps() != null ? page.mergeProps() : List.of();
    }

    /**
     * The prependable prop names, empty when none.
     *
     * @return the prepend props
     */
    public List<String> prependProps() {
        return page.prependProps() != null ? page.prependProps() : List.of();
    }

    /**
     * The deep-merge prop names, empty when none.
     *
     * @return the deep-merge props
     */
    public List<String> deepMergeProps() {
        return page.deepMergeProps() != null ? page.deepMergeProps() : List.of();
    }

    /**
     * The match-on fields (dot notation), empty when none.
     *
     * @return the match-on fields
     */
    public List<String> matchPropsOn() {
        return page.matchPropsOn() != null ? page.matchPropsOn() : List.of();
    }

    /**
     * The once-props metadata ({@code key -> {prop, expiresAt}}), empty when
     * none.
     *
     * @return the once props
     */
    public Map<String, ?> onceProps() {
        return toPlainMap(page.onceProps());
    }

    /**
     * The scroll prop metadata, empty when none.
     *
     * @return the scroll props
     */
    public Map<String, Map<String, Object>> scrollProps() {
        return page.scrollProps() != null ? page.scrollProps() : Map.of();
    }

    /**
     * The shared prop names, empty when none.
     *
     * @return the shared props
     */
    public List<String> sharedProps() {
        return page.sharedProps() != null ? page.sharedProps() : List.of();
    }

    /**
     * The rescued prop names, empty when none.
     *
     * @return the rescued props
     */
    public List<String> rescuedProps() {
        return page.rescuedProps() != null ? page.rescuedProps() : List.of();
    }

    /**
     * The page metadata, empty when none.
     *
     * @return the metadata map
     */
    public Map<String, Object> meta() {
        return page.meta() != null ? page.meta() : Map.of();
    }

    /**
     * Whether the page declares any deferred group.
     *
     * @return {@code true} when deferred props exist
     */
    public boolean hasDeferredProps() {
        return !deferredProps().isEmpty();
    }

    /**
     * Fail when the page declares any deferred group.
     *
     * @return this, for chaining
     * @throws AssertionError when deferred props exist
     */
    public InertiaPage assertNoDeferredProps() {
        if (hasDeferredProps()) {
            throw new AssertionError("Expected no deferred props but were " + deferredProps());
        }
        return this;
    }

    /**
     * Fail when the page declares any once prop.
     *
     * @return this, for chaining
     * @throws AssertionError when once props exist
     */
    public InertiaPage assertNoOnceProps() {
        if (!onceProps().isEmpty()) {
            throw new AssertionError("Expected no once props but were " + onceProps());
        }
        return this;
    }

    private static Map<String, Object> toPlainMap(Map<String, ?> source) {
        if (source == null) return new LinkedHashMap<>();
        return new LinkedHashMap<>(source);
    }

    // ── Aserciones ──────────────────────────────────────────────

    /**
     * Assert the component name.
     *
     * @param expected the expected component
     * @return this, for chaining
     * @throws AssertionError when the component differs
     */
    public InertiaPage assertComponent(String expected) {
        if (!component().equals(expected)) {
            throw new AssertionError("Expected component <" + expected + "> but was <" + component() + ">");
        }
        return this;
    }

    /**
     * Assert the page URL.
     *
     * @param expected the expected URL
     * @return this, for chaining
     * @throws AssertionError when the URL differs
     */
    public InertiaPage assertUrl(String expected) {
        if (!expected.equals(url())) {
            throw new AssertionError("Expected url <" + expected + "> but was <" + url() + ">");
        }
        return this;
    }

    /**
     * Assert the asset version.
     *
     * @param expected the expected version
     * @return this, for chaining
     * @throws AssertionError when the version differs
     */
    public InertiaPage assertVersion(String expected) {
        if (!expected.equals(version())) {
            throw new AssertionError("Expected version <" + expected + "> but was <" + version() + ">");
        }
        return this;
    }

    /**
     * Assert a prop exists and equals the expected value.
     *
     * @param key      the prop key
     * @param expected the expected value
     * @return this, for chaining
     * @throws AssertionError when absent or different
     */
    public InertiaPage assertProp(String key, Object expected) {
        if (!hasProp(key)) {
            throw new AssertionError("Expected prop <" + key + "> to be present but it was absent");
        }
        var actual = prop(key);
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected prop <" + key + "> to equal <" + expected + "> but was <" + actual + ">");
        }
        return this;
    }

    /**
     * Assert all the given props exist.
     *
     * @param keys the prop keys
     * @return this, for chaining
     * @throws AssertionError when any is absent
     */
    public InertiaPage assertHasProps(String... keys) {
        for (String key : keys) {
            if (!hasProp(key)) {
                throw new AssertionError("Expected prop <" + key + "> to be present but it was absent");
            }
        }
        return this;
    }

    /**
     * Assert each entry of the map exists as a prop with the given value.
     *
     * @param expected the expected prop/value pairs
     * @return this, for chaining
     * @throws AssertionError when any is absent or different
     */
    public InertiaPage assertHasProps(Map<String, Object> expected) {
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            if (!hasProp(entry.getKey())) {
                throw new AssertionError("Expected prop <" + entry.getKey() + "> to be present but it was absent");
            }
            Object actual = prop(entry.getKey());
            if (!entry.getValue().equals(actual)) {
                throw new AssertionError("Expected prop <" + entry.getKey() + "> to equal <" + entry.getValue()
                        + "> but was <" + actual + ">");
            }
        }
        return this;
    }

    /**
     * Assert the props map equals the expected map exactly.
     *
     * @param expected the expected props map
     * @return this, for chaining
     * @throws AssertionError when the maps differ
     */
    public InertiaPage assertHasExactProps(Map<String, Object> expected) {
        var actual = props();
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected props <" + expected + "> but were <" + actual + ">");
        }
        return this;
    }

    /**
     * Assert a prop exists.
     *
     * @param key the prop key
     * @return this, for chaining
     */
    public InertiaPage assertPropExists(String key) {
        if (!hasProp(key)) {
            throw new AssertionError("Expected prop <" + key + "> to be present but it was absent");
        }
        return this;
    }

    /**
     * Assert a prop is absent.
     *
     * @param key the prop key
     * @return this, for chaining
     * @throws AssertionError when present
     */
    public InertiaPage assertNoProp(String key) {
        if (hasProp(key)) {
            throw new AssertionError("Expected prop <" + key + "> to be absent but it was present");
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
     * @return this, for chaining
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
     * @param key        prop key
     * @param assertions consumer accepting the nested map
     * @return this, for chaining
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

    /**
     * Assert the given props are declared as deferred (in any group).
     *
     * @param props the prop names
     * @return this, for chaining
     * @throws AssertionError when none are declared or any is missing
     */
    public InertiaPage assertDeferredProps(String... props) {
        if (!hasDeferredProps()) {
            throw new AssertionError("Expected deferred props <" + List.of(props) + "> but there were none");
        }
        var flattened = deferredProps().values().stream().flatMap(List::stream).toList();
        for (String prop : props) {
            if (!flattened.contains(prop)) {
                throw new AssertionError("Expected deferred prop <" + prop + "> in " + deferredProps());
            }
        }
        return this;
    }

    /**
     * Assert the given props are declared as deferred inside a group.
     *
     * @param group the group name
     * @param props the prop names
     * @return this, for chaining
     * @throws AssertionError when the group is missing or any prop is not in it
     */
    public InertiaPage assertDeferredPropsInGroup(String group, String... props) {
        var groupProps = deferredProps().get(group);
        if (groupProps == null) {
            throw new AssertionError("Expected deferred group <" + group + "> in " + deferredProps());
        }
        for (String prop : props) {
            if (!groupProps.contains(prop)) {
                throw new AssertionError("Expected deferred prop <" + prop + "> in group <" + group + "> but was in "
                        + groupProps);
            }
        }
        return this;
    }

    /**
     * Assert the given props are declared as mergeable.
     *
     * @param props the prop names
     * @return this, for chaining
     * @throws AssertionError when any is missing
     */
    public InertiaPage assertMergeProps(String... props) {
        return assertKeysIn("mergeProps", mergeProps(), props);
    }

    /**
     * Assert the given props are declared as prependable.
     *
     * @param props the prop names
     * @return this, for chaining
     * @throws AssertionError when any is missing
     */
    public InertiaPage assertPrependProps(String... props) {
        return assertKeysIn("prependProps", prependProps(), props);
    }

    /**
     * Assert the given props are declared as deep-mergeable.
     *
     * @param props the prop names
     * @return this, for chaining
     * @throws AssertionError when any is missing
     */
    public InertiaPage assertDeepMergeProps(String... props) {
        return assertKeysIn("deepMergeProps", deepMergeProps(), props);
    }

    /**
     * Assert the given match-on fields are declared.
     *
     * @param props the field names (dot notation)
     * @return this, for chaining
     * @throws AssertionError when any is missing
     */
    public InertiaPage assertMatchPropsOn(String... props) {
        return assertKeysIn("matchPropsOn", matchPropsOn(), props);
    }

    /**
     * Assert the given once props are declared.
     *
     * @param props the prop names
     * @return this, for chaining
     * @throws AssertionError when none are declared or any is missing
     */
    public InertiaPage assertOnceProps(String... props) {
        if (onceProps().isEmpty()) {
            throw new AssertionError("Expected once props <" + List.of(props) + "> but there were none");
        }
        for (String prop : props) {
            if (!onceProps().containsKey(prop)) {
                throw new AssertionError("Expected once prop <" + prop + "> in " + onceProps());
            }
        }
        return this;
    }

    /**
     * Assert the given scroll props are declared.
     *
     * @param keys the prop keys
     * @return this, for chaining
     * @throws AssertionError when none are declared or any is missing
     */
    public InertiaPage assertScrollProps(String... keys) {
        if (scrollProps().isEmpty()) {
            throw new AssertionError("Expected scroll props <" + List.of(keys) + "> but there were none");
        }
        for (String key : keys) {
            if (!scrollProps().containsKey(key)) {
                throw new AssertionError("Expected scroll prop <" + key + "> in " + scrollProps());
            }
        }
        return this;
    }

    /**
     * Assert a metadata entry exists with the given value.
     *
     * @param key   the metadata key
     * @param value the expected value
     * @return this, for chaining
     * @throws AssertionError when absent or different
     */
    public InertiaPage assertMeta(String key, Object value) {
        if (!meta().containsKey(key)) {
            throw new AssertionError("Expected meta <" + key + "> to be present but it was absent");
        }
        if (!meta().get(key).equals(value)) {
            throw new AssertionError("Expected meta <" + key + "> to equal <" + value + "> but was <" + meta().get(key) + ">");
        }
        return this;
    }

    /**
     * Assert the given rescued props are declared.
     *
     * @param keys the prop keys
     * @return this, for chaining
     * @throws AssertionError when none are declared or any is missing
     */
    public InertiaPage assertRescuedProps(String... keys) {
        if (rescuedProps().isEmpty()) {
            throw new AssertionError("Expected rescued props <" + List.of(keys) + "> but there were none");
        }
        for (String key : keys) {
            if (!rescuedProps().contains(key)) {
                throw new AssertionError("Expected rescued prop <" + key + "> in " + rescuedProps());
            }
        }
        return this;
    }

    /**
     * Assert the encryptHistory flag.
     *
     * @param expected expected value
     * @return this, for chaining
     * @throws AssertionError when value differs
     */
    public InertiaPage assertEncryptHistory(boolean expected) {
        boolean actual = Boolean.TRUE.equals(encryptHistory());
        if (actual != expected) {
            throw new AssertionError("Expected encryptHistory to be <" + expected + "> but was <" + actual + ">");
        }
        return this;
    }

    /**
     * Assert the clearHistory flag.
     *
     * @param expected expected value
     * @return this, for chaining
     * @throws AssertionError when value differs
     */
    public InertiaPage assertClearHistory(boolean expected) {
        boolean actual = Boolean.TRUE.equals(clearHistory());
        if (actual != expected) {
            throw new AssertionError("Expected clearHistory to be <" + expected + "> but was <" + actual + ">");
        }
        return this;
    }

    /**
     * Assert the preserveFragment flag.
     *
     * @param expected expected value
     * @return this, for chaining
     * @throws AssertionError when value differs
     */
    public InertiaPage assertPreserveFragment(boolean expected) {
        boolean actual = Boolean.TRUE.equals(preserveFragment());
        if (actual != expected) {
            throw new AssertionError("Expected preserveFragment to be <" + expected + "> but was <" + actual + ">");
        }
        return this;
    }

    /**
     * Assert a flash entry equals the expected value.
     *
     * @param key      flash key
     * @param expected expected value
     * @return this, for chaining
     * @throws AssertionError when absent or different
     */
    public InertiaPage assertFlash(String key, Object expected) {
        Object actual = flash(key);
        if (actual == null) {
            throw new AssertionError("Expected flash <" + key + "> to be present but was absent");
        }
        if (!actual.equals(expected)) {
            throw new AssertionError("Expected flash <" + key + "> to equal <" + expected + "> but was <" + actual + ">");
        }
        return this;
    }

    /**
     * Dump the page payload to stdout for test debugging.
     *
     * @return this, for chaining
     */
    public InertiaPage dump() {
        System.out.println("=== [InertiaPage Dump (Quarkus)] ===");
        System.out.println("Component: " + component());
        System.out.println("Props: " + props());
        System.out.println("URL: " + url());
        System.out.println("Version: " + version());
        System.out.println("Flash: " + flash());
        System.out.println("DeferredProps: " + deferredProps());
        System.out.println("MergeProps: " + mergeProps());
        System.out.println("PrependProps: " + prependProps());
        System.out.println("DeepMergeProps: " + deepMergeProps());
        System.out.println("MatchPropsOn: " + matchPropsOn());
        System.out.println("OnceProps: " + onceProps());
        System.out.println("ScrollProps: " + scrollProps());
        System.out.println("SharedProps: " + sharedProps());
        System.out.println("RescuedProps: " + rescuedProps());
        System.out.println("Meta: " + meta());
        System.out.println("EncryptHistory: " + encryptHistory());
        System.out.println("ClearHistory: " + clearHistory());
        System.out.println("PreserveFragment: " + preserveFragment());
        System.out.println("=====================================");
        return this;
    }

    /**
     * Dump a single prop to stdout.
     *
     * @param key prop key
     * @return this, for chaining
     */
    public InertiaPage dump(String key) {
        System.out.println("=== [InertiaPage Dump Prop: " + key + "] ===");
        System.out.println(prop(key));
        System.out.println("==========================================");
        return this;
    }

    private InertiaPage assertKeysIn(String section, List<String> actual, String... props) {
        for (String prop : props) {
            if (!actual.contains(prop)) {
                throw new AssertionError("Expected " + section + " to contain <" + prop + "> but was " + actual);
            }
        }
        return this;
    }
}