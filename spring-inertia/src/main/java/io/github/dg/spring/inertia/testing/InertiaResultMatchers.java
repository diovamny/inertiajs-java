package io.github.dg.spring.inertia.testing;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * Assertions over the Inertia page payload of a MockMvc response.
 *
 * <pre>{@code
 * mockMvc.perform(get("/users"))
 *     .andExpect(inertia().component("Users"))
 *     .andExpect(inertia().prop("users", hasSize(2)))
 *     .andExpect(inertia().hasProp("filters"));
 * }</pre>
 */
public class InertiaResultMatchers {

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
        return result -> {
            var page = pageOf(result);
            if (!component.equals(page.component())) {
                throw new AssertionError(
                    "Expected component '" + component + "' but was '" + page.component() + "'");
            }
        };
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
        return prop(name, Matchers.equalTo(value));
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
     * Assert the page URL.
     *
     * @param url the expected URL
     * @return the matcher
     */
    public ResultMatcher url(String url) {
        return result -> {
            var page = pageOf(result);
            if (!url.equals(page.url())) {
                throw new AssertionError("Expected url '" + url + "' but was '" + page.url() + "'");
            }
        };
    }

    /**
     * Assert the asset version of the page.
     *
     * @param version the expected version
     * @return the matcher
     */
    public ResultMatcher version(String version) {
        return result -> {
            var page = pageOf(result);
            if (!version.equals(page.version())) {
                throw new AssertionError(
                    "Expected version '" + version + "' but was '" + page.version() + "'");
            }
        };
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
        return flash(key, Matchers.equalTo(value));
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