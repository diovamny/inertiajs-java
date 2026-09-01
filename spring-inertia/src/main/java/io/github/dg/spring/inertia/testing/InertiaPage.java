package io.github.dg.spring.inertia.testing;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;

/**
 * Deserialized view of the Inertia page payload for tests with a rich,
 * fluent assertion DSL mirroring Laravel's AssertableInertia.
 *
 * <pre>{@code
 * InertiaPage page = InertiaPage.fromJson(responseBody);
 * page.assertComponent("Users/Index")
 *     .assertProp("users", expectedUsers)
 *     .assertNoProp("secret")
 *     .assertDeferredProps("analytics");
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

    public InertiaPage(
            String component,
            Map<String, Object> props,
            String url,
            String version,
            Map<String, Object> flash) {
        this(component, props, url, version, flash, null, null, null, null, null, null, null, null, null, null, null, null, null);
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
    }

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
                node.path("component").asText(null),
                props,
                node.path("url").asText(null),
                node.has("version") && !node.path("version").isNull() ? node.path("version").asText() : null,
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
                preserveFragment);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Inertia page JSON: " + json, e);
        }
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

    public Object prop(String name) {
        return props != null ? props.get(name) : null;
    }

    public boolean hasProp(String name) {
        return props != null && props.containsKey(name);
    }

    public Object flash(String key) {
        return flash != null ? flash.get(key) : null;
    }

    public boolean hasDeferredProps() {
        return deferredProps != null && !deferredProps.isEmpty();
    }

    // --- Fluent Assertions ---

    public InertiaPage assertComponent(String expected) {
        if (!Objects.equals(component, expected)) {
            throw new AssertionError("Expected component <" + expected + "> but was <" + component + ">");
        }
        return this;
    }

    public InertiaPage assertUrl(String expected) {
        if (!Objects.equals(url, expected)) {
            throw new AssertionError("Expected url <" + expected + "> but was <" + url + ">");
        }
        return this;
    }

    public InertiaPage assertVersion(String expected) {
        if (!Objects.equals(version, expected)) {
            throw new AssertionError("Expected version <" + expected + "> but was <" + version + ">");
        }
        return this;
    }

    public InertiaPage assertProp(String key, Object expected) {
        if (!hasProp(key)) {
            throw new AssertionError("Expected prop <" + key + "> to be present but it was absent");
        }
        var actual = prop(key);
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Expected prop <" + key + "> to equal <" + expected + "> but was <" + actual + ">");
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

    public InertiaPage assertNoProp(String key) {
        if (hasProp(key)) {
            throw new AssertionError("Expected prop <" + key + "> to be absent but it was present with value: " + prop(key));
        }
        return this;
    }

    public InertiaPage assertDeferredProps(String... expectedProps) {
        if (!hasDeferredProps()) {
            throw new AssertionError("Expected deferred props <" + List.of(expectedProps) + "> but there were none");
        }
        var flattened = deferredProps.values().stream().flatMap(List::stream).toList();
        for (String prop : expectedProps) {
            if (!flattened.contains(prop)) {
                throw new AssertionError("Expected deferred prop <" + prop + "> in " + deferredProps);
            }
        }
        return this;
    }

    public InertiaPage assertDeferredPropsInGroup(String group, String... expectedProps) {
        var groupProps = deferredProps.get(group);
        if (groupProps == null) {
            throw new AssertionError("Expected deferred group <" + group + "> in " + deferredProps);
        }
        for (String prop : expectedProps) {
            if (!groupProps.contains(prop)) {
                throw new AssertionError("Expected deferred prop <" + prop + "> in group <" + group + "> but was in " + groupProps);
            }
        }
        return this;
    }

    public InertiaPage assertNoDeferredProps() {
        if (hasDeferredProps()) {
            throw new AssertionError("Expected no deferred props but were " + deferredProps);
        }
        return this;
    }

    public InertiaPage assertMergeProps(String... expectedProps) {
        for (String prop : expectedProps) {
            if (!mergeProps.contains(prop)) {
                throw new AssertionError("Expected mergeProps to contain <" + prop + "> but was " + mergeProps);
            }
        }
        return this;
    }

    public InertiaPage assertPrependProps(String... expectedProps) {
        for (String prop : expectedProps) {
            if (!prependProps.contains(prop)) {
                throw new AssertionError("Expected prependProps to contain <" + prop + "> but was " + prependProps);
            }
        }
        return this;
    }

    public InertiaPage assertDeepMergeProps(String... expectedProps) {
        for (String prop : expectedProps) {
            if (!deepMergeProps.contains(prop)) {
                throw new AssertionError("Expected deepMergeProps to contain <" + prop + "> but was " + deepMergeProps);
            }
        }
        return this;
    }

    public InertiaPage assertMatchPropsOn(String... expectedFields) {
        for (String field : expectedFields) {
            if (!matchPropsOn.contains(field)) {
                throw new AssertionError("Expected matchPropsOn to contain <" + field + "> but was " + matchPropsOn);
            }
        }
        return this;
    }

    public InertiaPage assertOnceProps(String... expectedProps) {
        if (onceProps.isEmpty()) {
            throw new AssertionError("Expected once props <" + List.of(expectedProps) + "> but there were none");
        }
        for (String prop : expectedProps) {
            if (!onceProps.containsKey(prop)) {
                throw new AssertionError("Expected once prop <" + prop + "> in " + onceProps);
            }
        }
        return this;
    }

    public InertiaPage assertNoOnceProps() {
        if (!onceProps.isEmpty()) {
            throw new AssertionError("Expected no once props but were " + onceProps);
        }
        return this;
    }

    public InertiaPage assertScrollProps(String... keys) {
        if (scrollProps.isEmpty()) {
            throw new AssertionError("Expected scroll props <" + List.of(keys) + "> but there were none");
        }
        for (String key : keys) {
            if (!scrollProps.containsKey(key)) {
                throw new AssertionError("Expected scroll prop <" + key + "> in " + scrollProps);
            }
        }
        return this;
    }

    public InertiaPage assertRescuedProps(String... keys) {
        if (rescuedProps.isEmpty()) {
            throw new AssertionError("Expected rescued props <" + List.of(keys) + "> but there were none");
        }
        for (String key : keys) {
            if (!rescuedProps.contains(key)) {
                throw new AssertionError("Expected rescued prop <" + key + "> in " + rescuedProps);
            }
        }
        return this;
    }

    public InertiaPage assertMeta(String key, Object expectedValue) {
        if (!meta.containsKey(key)) {
            throw new AssertionError("Expected meta <" + key + "> to be present but was absent");
        }
        if (!Objects.equals(meta.get(key), expectedValue)) {
            throw new AssertionError("Expected meta <" + key + "> to equal <" + expectedValue + "> but was <" + meta.get(key) + ">");
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