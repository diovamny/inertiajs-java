package com.quarkus.inertia.testing;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quarkus.inertia.model.PageObject;

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

    private InertiaPage(PageObject page) {
        this.page = page;
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

    private InertiaPage assertKeysIn(String section, List<String> actual, String... props) {
        for (String prop : props) {
            if (!actual.contains(prop)) {
                throw new AssertionError("Expected " + section + " to contain <" + prop + "> but was " + actual);
            }
        }
        return this;
    }
}