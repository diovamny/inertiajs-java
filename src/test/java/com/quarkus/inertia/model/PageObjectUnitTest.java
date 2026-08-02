package com.quarkus.inertia.model;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PageObjectUnitTest {

    @Test
    void shouldBuildWithRequiredFields() {
        var page = new PageObject("Home", Map.of("name", "John"), "/", "v1");
        assertThat(page.component()).isEqualTo("Home");
        assertThat(page.props()).containsEntry("name", "John");
        assertThat(page.url()).isEqualTo("/");
        assertThat(page.version()).isEqualTo("v1");
    }

    @Test
    void shouldUseEmptyMapForNullProps() {
        var page = new PageObject("Home", null, "/", "v1");
        assertThat(page.props()).isEmpty();
    }

    @Test
    void shouldRejectBlankComponent() {
        assertThatThrownBy(() -> new PageObject("", Map.of(), "/", "v1"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnImmutableProps() {
        var page = new PageObject("Home", Map.of("key", "value"), "/", "v1");
        assertThatThrownBy(() -> page.props().put("new", "value"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldCreateWithPropsUsingWithProps() {
        var page = new PageObject("Home", Map.of("a", "1"), "/", "v1");
        var updated = page.withProps(Map.of("b", "2"));
        assertThat(updated.props()).containsEntry("b", "2");
        assertThat(page.props()).containsEntry("a", "1");
    }

    @Test
    void shouldCreateWithVersionUsingWithVersion() {
        var page = new PageObject("Home", Map.of(), "/", "v1");
        var updated = page.withVersion("v2");
        assertThat(updated.version()).isEqualTo("v2");
        assertThat(page.version()).isEqualTo("v1");
    }

    @Test
    void shouldCreateWithUrlUsingWithUrl() {
        var page = new PageObject("Home", Map.of(), "/old", "v1");
        var updated = page.withUrl("/new");
        assertThat(updated.url()).isEqualTo("/new");
        assertThat(page.url()).isEqualTo("/old");
    }

    @Test
    void shouldAcceptMetadataFields() {
        var page = new PageObject("Home", Map.of(), "/", "v1",
            Map.of("default", List.of("lazyData")), List.of("items"), List.of("prependItem"),
            null, null, null, null, null, null, null,
            false, false, false);
        assertThat(page.deferredProps()).containsKey("default");
        assertThat(page.deferredProps().get("default")).containsExactly("lazyData");
        assertThat(page.mergeProps()).containsExactly("items");
        assertThat(page.prependProps()).containsExactly("prependItem");
    }

    @Test
    void shouldDetectHasMetadata() {
        var plain = new PageObject("Home", Map.of(), "/", "v1");
        assertThat(plain.hasMetadata()).isFalse();
        var withMeta = new PageObject("Home", Map.of(), "/", "v1",
            Map.of("default", List.of("data")), null, null, null,
            null, null, null, null, null, null,
            false, false, false);
        assertThat(withMeta.hasMetadata()).isTrue();
    }

    @Test
    void shouldDetectPrependPropsMetadata() {
        var page = new PageObject("Home", Map.of(), "/", "v1",
            null, null, List.of("items"), null,
            null, null, null, null, null, null,
            false, false, false);
        assertThat(page.hasMetadata()).isTrue();
        assertThat(page.prependProps()).containsExactly("items");
    }

    @Test
    void shouldDetectMatchPropsOnMetadata() {
        var page = new PageObject("Home", Map.of(), "/", "v1",
            null, null, null, null, List.of("items.id"),
            null, null, null, null, null,
            false, false, false);
        assertThat(page.hasMetadata()).isTrue();
        assertThat(page.matchPropsOn()).containsExactly("items.id");
    }

    @Test
    void shouldDetectRescuedPropsMetadata() {
        var page = new PageObject("Home", Map.of(), "/", "v1",
            null, null, null, null,
            null, null, null, null, Map.of("failedProp", "error"), null,
            false, false, false);
        assertThat(page.hasMetadata()).isTrue();
        assertThat(page.rescuedProps()).containsEntry("failedProp", "error");
    }

    @Test
    void shouldAlwaysIncludeHistoryBooleans() {
        var page = new PageObject("Home", Map.of(), "/", "v1");
        assertThat(page.encryptHistory()).isFalse();
        assertThat(page.clearHistory()).isFalse();
        assertThat(page.preserveFragment()).isFalse();
    }

    @Test
    void shouldAcceptHistoryBooleansAsTrue() {
        var page = new PageObject("Home", Map.of(), "/", "v1",
            null, null, null, null,
            null, null, null, null, null, null,
            true, true, true);
        assertThat(page.encryptHistory()).isTrue();
        assertThat(page.clearHistory()).isTrue();
        assertThat(page.preserveFragment()).isTrue();
        assertThat(page.hasMetadata()).isTrue();
    }

    @Test
    void shouldIncludeErrorsInProps() {
        var page = new PageObject("Home", Map.of("errors", Map.of("name", "required")), "/", "v1");
        assertThat(page.props()).containsKey("errors");
        assertThat(page.props().get("errors")).isEqualTo(Map.of("name", "required"));
    }
}
