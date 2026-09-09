package io.github.diovamny.spring.inertia.testing;

import java.util.Map;
import org.hamcrest.Matcher;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * Assertions over the Inertia page payload of a MockMvc response.
 *
 * <pre>{@code
 * mockMvc.perform(get("/users"))
 *     .andExpect(inertia().component("Users/Index"))
 *     .andExpect(inertia().prop("users", hasSize(2)))
 *     .andExpect(inertia().hasProp("filters"))
 *     .andExpect(inertia().missing("secret"))
 *     .andExpect(inertia().hasDeferredProps("metrics"));
 * }</pre>
 */
public final class InertiaResultMatchers {

    private InertiaResultMatchers() {
    }

    /**
     * Entry point for Inertia page assertions.
     *
     * @return the matchers facade
     */
    public static InertiaResultMatchers inertia() {
        return new InertiaResultMatchers();
    }

    /**
     * Assert the component name of the page.
     *
     * @param component the expected component
     * @return the matcher
     */
    public ResultMatcher component(String component) {
        return result -> pageOf(result).assertComponent(component);
    }

    /**
     * Assert a prop value with a Hamcrest matcher.
     *
     * @param name   the prop name
     * @param matcher the matcher
     * @return the matcher
     */
    public ResultMatcher prop(String name, Matcher<?> matcher) {
        return result -> {
            var page = pageOf(result);
            var value = page.prop(name);
            if (!matcher.matches(value)) {
                throw new AssertionError(
                    "Prop '" + name + "' with value " + value + " does not match " + matcher);
            }
        };
    }

    /**
     * Assert a prop equals a value.
     *
     * @param name  the prop name
     * @param value the expected value
     * @return the matcher
     */
    public ResultMatcher prop(String name, Object value) {
        return result -> pageOf(result).assertProp(name, value);
    }

    /**
     * Assert the page carries the given prop.
     *
     * @param name the prop name
     * @return the matcher
     */
    public ResultMatcher hasProp(String name) {
        return result -> {
            var page = pageOf(result);
            if (!page.hasProp(name)) {
                throw new AssertionError("Expected page to carry prop '" + name + "'");
            }
        };
    }

    /**
     * Assert multiple props exist.
     *
     * @param names the prop names
     * @return the matcher
     */
    public ResultMatcher hasProps(String... names) {
        return result -> pageOf(result).assertHasProps(names);
    }

    /**
     * Assert multiple props match key/value pairs.
     *
     * @param expected expected props map
     * @return the matcher
     */
    public ResultMatcher hasProps(Map<String, Object> expected) {
        return result -> pageOf(result).assertHasProps(expected);
    }

    /**
     * Assert exact props map matches.
     *
     * @param expected exact expected props map
     * @return the matcher
     */
    public ResultMatcher exactProps(Map<String, Object> expected) {
        return result -> pageOf(result).assertHasExactProps(expected);
    }

    /**
     * Assert that a prop is missing/absent.
     *
     * @param name the prop name
     * @return the matcher
     */
    public ResultMatcher missing(String name) {
        return result -> pageOf(result).assertNoProp(name);
    }

    /**
     * Assert that a collection, map or array prop has the given item count.
     *
     * @param name  the prop name
     * @param count the expected count
     * @return the matcher
     */
    public ResultMatcher propCount(String name, int count) {
        return result -> pageOf(result).assertPropCount(name, count);
    }

    /**
     * Assert that a prop is missing/absent.
     *
     * @param name the prop name
     * @return the matcher
     */
    public ResultMatcher noProp(String name) {
        return missing(name);
    }

    /**
     * Assert the page URL.
     *
     * @param url the expected URL
     * @return the matcher
     */
    public ResultMatcher url(String url) {
        return result -> pageOf(result).assertUrl(url);
    }

    /**
     * Assert the asset version of the page.
     *
     * @param version the expected version
     * @return the matcher
     */
    public ResultMatcher version(String version) {
        return result -> pageOf(result).assertVersion(version);
    }

    /**
     * Assert a top-level flash value with a Hamcrest matcher.
     *
     * @param key     the flash key
     * @param matcher the matcher
     * @return the matcher
     */
    public ResultMatcher flash(String key, Matcher<?> matcher) {
        return result -> {
            var page = pageOf(result);
            var value = page.flash(key);
            if (!matcher.matches(value)) {
                throw new AssertionError(
                    "Flash '" + key + "' with value " + value + " does not match " + matcher);
            }
        };
    }

    /**
     * Assert a top-level flash value equals a value.
     *
     * @param key   the flash key
     * @param value the expected value
     * @return the matcher
     */
    public ResultMatcher flash(String key, Object value) {
        return result -> pageOf(result).assertFlash(key, value);
    }

    /**
     * Assert that the page declares deferred props.
     *
     * @param names deferred prop names
     * @return the matcher
     */
    public ResultMatcher hasDeferredProps(String... names) {
        return result -> pageOf(result).assertDeferredProps(names);
    }

    /**
     * Assert that the page declares deferred props in a group.
     *
     * @param group group name
     * @param names deferred prop names
     * @return the matcher
     */
    public ResultMatcher hasDeferredGroup(String group, String... names) {
        return result -> pageOf(result).assertDeferredPropsInGroup(group, names);
    }

    /**
     * Assert that the page declares no deferred props.
     *
     * @return the matcher
     */
    public ResultMatcher hasNoDeferredProps() {
        return result -> pageOf(result).assertNoDeferredProps();
    }

    /**
     * Assert that the page declares merge props.
     *
     * @param names merge prop names
     * @return the matcher
     */
    public ResultMatcher hasMergeProps(String... names) {
        return result -> pageOf(result).assertMergeProps(names);
    }

    /**
     * Assert that the page declares prepend props.
     *
     * @param names prepend prop names
     * @return the matcher
     */
    public ResultMatcher hasPrependProps(String... names) {
        return result -> pageOf(result).assertPrependProps(names);
    }

    /**
     * Assert that the page declares deep-merge props.
     *
     * @param names deep-merge prop names
     * @return the matcher
     */
    public ResultMatcher hasDeepMergeProps(String... names) {
        return result -> pageOf(result).assertDeepMergeProps(names);
    }

    /**
     * Assert that the page declares matchPropsOn fields.
     *
     * @param fields matchOn fields
     * @return the matcher
     */
    public ResultMatcher hasMatchPropsOn(String... fields) {
        return result -> pageOf(result).assertMatchPropsOn(fields);
    }

    /**
     * Assert that the page declares once props.
     *
     * @param names once prop names
     * @return the matcher
     */
    public ResultMatcher hasOnceProps(String... names) {
        return result -> pageOf(result).assertOnceProps(names);
    }

    /**
     * Assert that the page declares no once props.
     *
     * @return the matcher
     */
    public ResultMatcher hasNoOnceProps() {
        return result -> pageOf(result).assertNoOnceProps();
    }

    /**
     * Assert that the page declares scroll props.
     *
     * @param keys scroll prop keys
     * @return the matcher
     */
    public ResultMatcher hasScrollProps(String... keys) {
        return result -> pageOf(result).assertScrollProps(keys);
    }

    /**
     * Assert that the page declares rescued props.
     *
     * @param keys rescued prop keys
     * @return the matcher
     */
    public ResultMatcher hasRescuedProps(String... keys) {
        return result -> pageOf(result).assertRescuedProps(keys);
    }

    /**
     * Assert encryptHistory flag value.
     *
     * @param expected expected value
     * @return the matcher
     */
    public ResultMatcher encryptHistory(boolean expected) {
        return result -> pageOf(result).assertEncryptHistory(expected);
    }

    /**
     * Assert clearHistory flag value.
     *
     * @param expected expected value
     * @return the matcher
     */
    public ResultMatcher clearHistory(boolean expected) {
        return result -> pageOf(result).assertClearHistory(expected);
    }

    /**
     * Assert preserveFragment flag value.
     *
     * @param expected expected value
     * @return the matcher
     */
    public ResultMatcher preserveFragment(boolean expected) {
        return result -> pageOf(result).assertPreserveFragment(expected);
    }

    /**
     * Assert page meta value.
     *
     * @param key meta key
     * @param expectedValue expected value
     * @return the matcher
     */
    public ResultMatcher meta(String key, Object expectedValue) {
        return result -> pageOf(result).assertMeta(key, expectedValue);
    }

    /**
     * Dump the parsed Inertia page payload to stdout for test debugging.
     *
     * @return the matcher
     */
    public ResultMatcher dump() {
        return result -> pageOf(result).dump();
    }

    /**
     * Dump a specific prop from the parsed page payload to stdout.
     *
     * @param key prop key
     * @return the matcher
     */
    public ResultMatcher dump(String key) {
        return result -> pageOf(result).dump(key);
    }

    private static InertiaPage pageOf(MvcResult result) {
        var body = new String(result.getResponse().getContentAsByteArray(),
            java.nio.charset.StandardCharsets.UTF_8);
        try {
            return InertiaPage.fromJson(body);
        } catch (IllegalArgumentException e) {
            throw new AssertionError("Response body is not an Inertia page: " + body, e);
        }
    }
}
