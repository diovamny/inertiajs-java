package com.quarkus.inertia.testing;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quarkus.inertia.model.PageObject;

public final class InertiaPage {

    private final PageObject page;

    private InertiaPage(PageObject page) {
        this.page = page;
    }

    public static InertiaPage fromJson(String json) {
        return fromJson(json, new ObjectMapper());
    }

    public static InertiaPage fromJson(String json, ObjectMapper mapper) {
        try {
            return new InertiaPage(mapper.readValue(json, PageObject.class));
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse Inertia page JSON", e);
        }
    }

    public static InertiaPage from(PageObject page) {
        return new InertiaPage(page);
    }

    public PageObject toPageObject() {
        return page;
    }

    public String component() {
        return page.component();
    }

    public String url() {
        return page.url();
    }

    public String version() {
        return page.version();
    }

    public boolean hasProp(String key) {
        return page.props().containsKey(key);
    }

    public Object prop(String key) {
        return page.props().get(key);
    }

    public Map<String, Object> props() {
        return page.props();
    }

    public Map<String, List<String>> deferredProps() {
        return page.deferredProps() != null ? page.deferredProps() : Map.of();
    }

    public List<String> mergeProps() {
        return page.mergeProps() != null ? page.mergeProps() : List.of();
    }

    public List<String> prependProps() {
        return page.prependProps() != null ? page.prependProps() : List.of();
    }

    public List<String> deepMergeProps() {
        return page.deepMergeProps() != null ? page.deepMergeProps() : List.of();
    }

    public List<String> matchPropsOn() {
        return page.matchPropsOn() != null ? page.matchPropsOn() : List.of();
    }

    public Map<String, ?> onceProps() {
        return toPlainMap(page.onceProps());
    }

    public Map<String, Map<String, Object>> scrollProps() {
        return page.scrollProps() != null ? page.scrollProps() : Map.of();
    }

    public List<String> sharedProps() {
        return page.sharedProps() != null ? page.sharedProps() : List.of();
    }

    public List<String> rescuedProps() {
        return page.rescuedProps() != null ? page.rescuedProps() : List.of();
    }

    public Map<String, Object> meta() {
        return page.meta() != null ? page.meta() : Map.of();
    }

    public boolean hasDeferredProps() {
        return !deferredProps().isEmpty();
    }

    private static Map<String, Object> toPlainMap(Map<String, ?> source) {
        if (source == null) return new LinkedHashMap<>();
        return new LinkedHashMap<>(source);
    }

    // ── Aserciones ──────────────────────────────────────────────

    public InertiaPage assertComponent(String expected) {
        if (!component().equals(expected)) {
            throw new AssertionError("Expected component <" + expected + "> but was <" + component() + ">");
        }
        return this;
    }

    public InertiaPage assertHasProps(String... keys) {
        for (String key : keys) {
            if (!hasProp(key)) {
                throw new AssertionError("Expected prop <" + key + "> to be present but it was absent");
            }
        }
        return this;
    }

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

    public InertiaPage assertHasExactProps(Map<String, Object> expected) {
        var actual = props();
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected props <" + expected + "> but were <" + actual + ">");
        }
        return this;
    }

    public InertiaPage assertNoProp(String key) {
        if (hasProp(key)) {
            throw new AssertionError("Expected prop <" + key + "> to be absent but it was present");
        }
        return this;
    }

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

    public InertiaPage assertMergeProps(String... props) {
        return assertKeysIn("mergeProps", mergeProps(), props);
    }

    public InertiaPage assertPrependProps(String... props) {
        return assertKeysIn("prependProps", prependProps(), props);
    }

    public InertiaPage assertDeepMergeProps(String... props) {
        return assertKeysIn("deepMergeProps", deepMergeProps(), props);
    }

    public InertiaPage assertMatchPropsOn(String... props) {
        return assertKeysIn("matchPropsOn", matchPropsOn(), props);
    }

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