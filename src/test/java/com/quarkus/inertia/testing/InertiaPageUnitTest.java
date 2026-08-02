package com.quarkus.inertia.testing;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InertiaPageUnitTest {

    private static final String PAGE = """
        {
          "component": "Persons/Index",
          "props": {
            "persons": [{"id": 1, "name": "Alice"}],
            "total": 10,
            "success": "creada correctamente"
          },
          "url": "/persons",
          "version": "1.0.0",
          "deferredProps": {"default": ["analytics"], "slow": ["statistics"]},
          "mergeProps": ["persons.data"],
          "prependProps": ["logs.data"],
          "deepMergeProps": ["settings.flags"],
          "matchPropsOn": ["persons.id"],
          "onceProps": {"flash": {"prop": "flash", "expiresAt": 123}},
          "scrollProps": {"persons": {"data": [1, 2], "pageName": "persons_page"}},
          "sharedProps": ["auth"],
          "rescuedProps": ["expensiveData"],
          "meta": {"title": "Persons"}
        }
        """;

    @Test
    void shouldExposeTopLevelFields() {
        var page = InertiaPage.fromJson(PAGE);
        assertThat(page.component()).isEqualTo("Persons/Index");
        assertThat(page.url()).isEqualTo("/persons");
        assertThat(page.version()).isEqualTo("1.0.0");
    }

    @Test
    void shouldExposePropsAndValues() {
        var page = InertiaPage.fromJson(PAGE);
        assertThat(page.hasProp("total")).isTrue();
        assertThat(page.prop("total")).isEqualTo(10);
        assertThat(page.props()).containsKeys("persons", "total", "success");
    }

    @Test
    void shouldExposeMetadataSections() {
        var page = InertiaPage.fromJson(PAGE);
        assertThat(page.hasDeferredProps()).isTrue();
        assertThat(page.deferredProps()).containsEntry("default", List.of("analytics"));
        assertThat(page.mergeProps()).containsExactly("persons.data");
        assertThat(page.prependProps()).containsExactly("logs.data");
        assertThat(page.deepMergeProps()).containsExactly("settings.flags");
        assertThat(page.matchPropsOn()).containsExactly("persons.id");
        assertThat(page.onceProps()).containsKey("flash");
        assertThat(page.scrollProps()).containsKey("persons");
        assertThat(page.sharedProps()).containsExactly("auth");
        assertThat(page.rescuedProps()).containsExactly("expensiveData");
        assertThat(page.meta()).containsEntry("title", "Persons");
    }

    @Test
    void shouldAssertComponent() {
        InertiaPage.fromJson(PAGE).assertComponent("Persons/Index");
        assertThatThrownBy(() -> InertiaPage.fromJson(PAGE).assertComponent("Other"))
            .isInstanceOf(AssertionError.class);
    }

    @Test
    void shouldAssertPropsPartialMatch() {
        InertiaPage.fromJson(PAGE)
            .assertHasProps("persons", "total")
            .assertHasProps(Map.of("total", 10));
        assertThatThrownBy(() -> InertiaPage.fromJson(PAGE).assertHasProps(Map.of("total", 99)))
            .isInstanceOf(AssertionError.class);
    }

    @Test
    void shouldAssertExactProps() {
        InertiaPage.fromJson(PAGE).assertHasExactProps(Map.of(
            "persons", List.of(Map.of("id", 1, "name", "Alice")),
            "total", 10,
            "success", "creada correctamente"));
        assertThatThrownBy(() -> InertiaPage.fromJson(PAGE).assertHasExactProps(Map.of("total", 10)))
            .isInstanceOf(AssertionError.class);
    }

    @Test
    void shouldAssertNoProp() {
        InertiaPage.fromJson(PAGE).assertNoProp("secret");
        assertThatThrownBy(() -> InertiaPage.fromJson(PAGE).assertNoProp("total"))
            .isInstanceOf(AssertionError.class);
    }

    @Test
    void shouldAssertDeferredProps() {
        InertiaPage.fromJson(PAGE)
            .assertDeferredProps("analytics", "statistics")
            .assertDeferredPropsInGroup("default", "analytics")
            .assertDeferredPropsInGroup("slow", "statistics");
        assertThatThrownBy(() -> InertiaPage.fromJson(PAGE).assertDeferredProps("missing"))
            .isInstanceOf(AssertionError.class);
        assertThatThrownBy(() -> InertiaPage.fromJson(PAGE).assertDeferredPropsInGroup("default", "statistics"))
            .isInstanceOf(AssertionError.class);
    }

    @Test
    void shouldAssertMergeFamilies() {
        InertiaPage.fromJson(PAGE)
            .assertMergeProps("persons.data")
            .assertPrependProps("logs.data")
            .assertDeepMergeProps("settings.flags")
            .assertMatchPropsOn("persons.id")
            .assertOnceProps("flash")
            .assertScrollProps("persons");
        assertThatThrownBy(() -> InertiaPage.fromJson(PAGE).assertOnceProps("nope"))
            .isInstanceOf(AssertionError.class);
        assertThatThrownBy(() -> InertiaPage.fromJson(PAGE).assertMergeProps("nope"))
            .isInstanceOf(AssertionError.class);
    }

    @Test
    void shouldAssertMeta() {
        InertiaPage.fromJson(PAGE).assertMeta("title", "Persons");
        assertThatThrownBy(() -> InertiaPage.fromJson(PAGE).assertMeta("title", "Other"))
            .isInstanceOf(AssertionError.class);
    }

    @Test
    void shouldAcceptPlainPageObject() {
        var pageObject = new com.quarkus.inertia.model.PageObject(
            "Home", Map.of("user", "alice"), "/", "v1");
        var page = InertiaPage.from(pageObject);
        assertThat(page.component()).isEqualTo("Home");
        assertThat(page.hasProp("user")).isTrue();
        assertThat(page.deferredProps()).isEmpty();
    }

    @Test
    void shouldRejectInvalidJson() {
        assertThatThrownBy(() -> InertiaPage.fromJson("not json"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}