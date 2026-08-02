package com.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import com.quarkus.inertia.model.AlwaysProp;
import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.protocol.PartialReloadProcessor.PartialReloadContext;

class PartialReloadProcessorUnitTest {

    private final PartialReloadProcessor processor = new PartialReloadProcessor();

    @Test
    void shouldReturnAllPropsWhenComponentMismatch() {
        var page = new PageObject("Users", Map.of("name", "John", "email", "john@test.com"), "/users", "v1");
        var context = new PartialReloadContext("Posts", Set.of("name"), Set.of(), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).hasSize(2);
    }

    @Test
    void shouldFilterToOnlyDataProps() {
        var page = new PageObject("Users", Map.of("name", "John", "email", "john@test.com", "role", "admin"), "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of("name", "email"), Set.of(), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).hasSize(2);
        assertThat(result.props()).containsKeys("name", "email");
        assertThat(result.props()).doesNotContainKey("role");
    }

    @Test
    void shouldExcludeExceptProps() {
        var page = new PageObject("Users", Map.of("name", "John", "email", "john@test.com", "role", "admin"), "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of(), Set.of("role"), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).hasSize(2);
        assertThat(result.props()).containsKeys("name", "email");
    }

    @Test
    void shouldGivePriorityToExceptWhenBothPresent() {
        var page = new PageObject("Users", Map.of("name", "John", "email", "john@test.com", "role", "admin"), "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of("name", "email", "role"), Set.of("role"), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).hasSize(2);
        assertThat(result.props()).containsKeys("name", "email");
        assertThat(result.props()).doesNotContainKey("role");
    }

    @Test
    void shouldReturnAllPropsWhenDataAndExceptAreEmpty() {
        var page = new PageObject("Users", Map.of("a", "1", "b", "2"), "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of(), Set.of(), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).hasSize(2);
    }

    @Test
    void shouldKeepErrorsWhenWrappedInAlwaysProp() {
        var page = new PageObject("Users", Map.of(
            "name", "John",
            "errors", AlwaysProp.of(Map.of("name", "Required"))
        ), "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of(), Set.of("name"), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).doesNotContainKey("name");
        assertThat(result.props()).containsKey("errors");
        assertThat(result.props().get("errors")).isEqualTo(Map.of("name", "Required"));
    }

    @Test
    void shouldReturnAllPropsWhenContextIsNull() {
        var page = new PageObject("Users", Map.of("a", "1", "b", "2"), "/users", "v1");
        var result = processor.apply(page, null);
        assertThat(result.props()).hasSize(2);
    }

    @Test
    void shouldAlwaysIncludeAlwaysProp() {
        var page = new PageObject("Users", Map.of(
            "name", "John",
            "role", AlwaysProp.of("admin")
        ), "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of("name"), Set.of(), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).containsKey("role");
        assertThat(result.props()).containsEntry("role", "admin");
    }

    @Test
    void shouldNotFilterAlwaysPropEvenWithExcept() {
        var page = new PageObject("Users", Map.of(
            "name", "John",
            "role", AlwaysProp.of("admin")
        ), "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of(), Set.of("role"), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).containsKey("role");
    }
}
