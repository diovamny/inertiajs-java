package io.github.diovamny.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import io.github.diovamny.quarkus.inertia.model.AlwaysProp;
import io.github.diovamny.quarkus.inertia.model.PageObject;
import io.github.diovamny.quarkus.inertia.protocol.PartialReloadProcessor.PartialReloadContext;

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

    @Test
    void shouldPruneNestedPropsWithDotNotationData() {
        var page = new PageObject("Users", Map.of(
            "auth", Map.of("user", Map.of("id", 1, "name", "John"), "email", "john@test.com"),
            "other", "value"
        ), "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of("auth.user"), Set.of(), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).containsOnlyKeys("auth");
        assertThat(result.props().get("auth")).isEqualTo(Map.of("user", Map.of("id", 1, "name", "John")));
    }

    @Test
    void shouldKeepWholeSubtreeWhenParentRequested() {
        var page = new PageObject("Users", Map.of(
            "auth", Map.of("user", Map.of("id", 1), "email", "john@test.com"),
            "other", "value"
        ), "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of("auth"), Set.of(), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).containsOnlyKeys("auth");
        assertThat(result.props().get("auth")).isEqualTo(Map.of("user", Map.of("id", 1), "email", "john@test.com"));
    }

    @Test
    void shouldPruneNestedPropsWithDotNotationExcept() {
        var page = new PageObject("Users", Map.of(
            "auth", Map.of("user", Map.of("id", 1), "email", "john@test.com"),
            "other", "value"
        ), "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of(), Set.of("auth.user"), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).containsKeys("auth", "other");
        assertThat(result.props().get("auth")).isEqualTo(Map.of("email", "john@test.com"));
    }

    @Test
    void shouldRemoveWholeSubtreeWhenParentExcepted() {
        var page = new PageObject("Users", Map.of(
            "auth", Map.of("user", Map.of("id", 1), "email", "john@test.com"),
            "other", "value"
        ), "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of(), Set.of("auth"), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).containsOnlyKeys("other");
    }

    @Test
    void shouldKeepErrorsAlwaysEvenInNestedPartial() {
        var page = new PageObject("Users", Map.of(
            "auth", Map.of("user", Map.of("id", 1), "email", "john@test.com"),
            "errors", AlwaysProp.of(Map.of("name", "Required"))
        ), "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of("auth.user"), Set.of(), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).containsKeys("auth", "errors");
        assertThat(result.props().get("errors")).isEqualTo(Map.of("name", "Required"));
    }

    @Test
    void shouldNotKeepUnrequestedSharedPropsOnPartialReload() {
        var page = new PageObject("Users", Map.of("name", "John", "email", "john@test.com"), "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of("name"), Set.of(), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).containsKey("name");
        assertThat(result.props()).doesNotContainKey("email");
    }

    @Test
    void shouldKeepExplicitNullPropsOnPartialReload() {
        // Null is a real prop value, not an omission signal: an explicitly
        // requested null prop must be present in the partial response.
        var props = new java.util.LinkedHashMap<String, Object>();
        props.put("status", null);
        props.put("count", 2);
        props.put("sidebar", "x");
        var page = new PageObject("Users", props, "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of("status", "count"), Set.of(), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).containsKey("status");
        assertThat(result.props().get("status")).isNull();
        assertThat(result.props().get("count")).isEqualTo(2);
        assertThat(result.props()).doesNotContainKey("sidebar");
    }

    @Test
    void shouldNotKeepSharedPropsWhenExcepted() {
        var page = new PageObject("Users", Map.of("name", "John", "auth", Map.of("user", Map.of("id", 1))), "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of(), Set.of("auth"), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).containsKey("name");
        assertThat(result.props()).doesNotContainKey("auth");
    }

    @Test
    void shouldExcludeNestedLeafOnlyNotParent() {
        var page = new PageObject("Users", Map.of(
            "auth", Map.of("user", Map.of("id", 1, "email", "john@test.com"), "role", "admin"),
            "other", "value"
        ), "/users", "v1");
        var context = new PartialReloadContext("Users", Set.of(), Set.of("auth.user.email"), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).containsKeys("auth", "other");
        assertThat(result.props().get("auth")).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        var auth = (Map<String, Object>) result.props().get("auth");
        assertThat(auth).containsKey("user");
        assertThat(auth).containsKey("role");
        assertThat(auth.get("role")).isEqualTo("admin");
        @SuppressWarnings("unchecked")
        var user = (Map<String, Object>) auth.get("user");
        assertThat(user).containsEntry("id", 1);
        assertThat(user).doesNotContainKey("email");
    }
}
