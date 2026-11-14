package io.github.diovamny.inertia.core.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
            null, Map.of("default", List.of("lazyData")), List.of("items"), List.of("prependItem"),
            null, null, null, null, null, null, null,
            false, false, false, null, null);
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
            null, Map.of("default", List.of("data")), null, null, null,
            null, null, null, null, null, null,
            false, false, false, null, null);
        assertThat(withMeta.hasMetadata()).isTrue();
    }

    @Test
    void shouldDetectPrependPropsMetadata() {
        var page = new PageObject("Home", Map.of(), "/", "v1",
            null, null, null, List.of("items"), null,
            null, null, null, null, null, null,
            false, false, false, null, null);
        assertThat(page.hasMetadata()).isTrue();
        assertThat(page.prependProps()).containsExactly("items");
    }

    @Test
    void shouldDetectMatchPropsOnMetadata() {
        var page = new PageObject("Home", Map.of(), "/", "v1",
            null, null, null, null, null, List.of("items.id"),
            null, null, null, null, null,
            false, false, false, null, null);
        assertThat(page.hasMetadata()).isTrue();
        assertThat(page.matchPropsOn()).containsExactly("items.id");
    }

    @Test
    void shouldDetectRescuedPropsMetadata() {
        var page = new PageObject("Home", Map.of(), "/", "v1",
            null, null, null, null, null,
            null, null, null, null, List.of("failedProp"), null,
            false, false, false, null, null);
        assertThat(page.hasMetadata()).isTrue();
        assertThat(page.rescuedProps()).containsExactly("failedProp");
    }

    @Test
    void shouldAlwaysIncludeHistoryBooleans() {
        var page = new PageObject("Home", Map.of(), "/", "v1");
        assertThat(page.encryptHistory()).isFalse();
        assertThat(page.clearHistory()).isFalse();
        assertThat(page.preserveFragment()).isFalse();
        assertThat(page.preserveBigIntegers()).isNull();
    }

    @Test
    void shouldAcceptHistoryBooleansAsTrue() {
        var page = new PageObject("Home", Map.of(), "/", "v1",
            null, null, null, null, null,
            null, null, null, null, null, null,
            true, true, true, null, null);
        assertThat(page.encryptHistory()).isTrue();
        assertThat(page.clearHistory()).isTrue();
        assertThat(page.preserveFragment()).isTrue();
        assertThat(page.hasMetadata()).isTrue();
    }

    @Test
    void shouldDetectPreserveBigIntegersMetadata() {
        var plain = new PageObject("Home", Map.of(), "/", "v1");
        assertThat(plain.hasMetadata()).isFalse();

        var optedIn = plain.withPreserveBigIntegers(Boolean.TRUE);
        assertThat(optedIn.preserveBigIntegers()).isTrue();
        assertThat(optedIn.hasMetadata()).isTrue();
        assertThat(optedIn.withProps(Map.of("a", 1)).preserveBigIntegers()).isTrue();
        assertThat(optedIn.withVersion("v2").preserveBigIntegers()).isTrue();
        assertThat(optedIn.withUrl("/other").preserveBigIntegers()).isTrue();

        var optedOut = optedIn.withPreserveBigIntegers(null);
        assertThat(optedOut.preserveBigIntegers()).isNull();
        assertThat(optedOut.hasMetadata()).isFalse();
    }

    @Test
    void shouldIncludeErrorsInProps() {
        var page = new PageObject("Home", Map.of("errors", Map.of("name", "required")), "/", "v1");
        assertThat(page.props()).containsKey("errors");
        assertThat(page.props().get("errors")).isEqualTo(Map.of("name", "required"));
    }

    @Test
    void withPropsKeepsMetadata() {
        var page = new PageObject("Dashboard", Map.of("a", 1), "/", "v1",
            Map.of("m", "x"), Map.of("g", List.of("p")), List.of("p"),
            null, null, null, null, null, null, null, null, false, false, false, null, null);
        var updated = page.withProps(Map.of("b", 2));
        assertThat(updated.props()).containsEntry("b", 2);
        assertThat(updated.flash()).containsEntry("m", "x");
        assertThat(updated.hasDeferredProps()).isTrue();
    }

    @Test
    void withMergeMetadataRoundTrip() {
        var page = new PageObject("Home", Map.of(), "/a", "v1")
            .withMergeMetadata(List.of("users"), List.of(), List.of(), List.of());
        assertThat(page.mergeProps()).containsExactly("users");
    }

    @Test
    void oncePropsMetadata() {
        var page = new PageObject("Home", Map.of(), "/a", "v1", null,
            null, null, null, null, null, Map.of("n", new OnceProp("n", null)),
            null, null, null, null, false, false, false, null, null);
        assertThat(page.hasMetadata()).isTrue();
        assertThat(page.onceProps()).containsKey("n");
    }
}
