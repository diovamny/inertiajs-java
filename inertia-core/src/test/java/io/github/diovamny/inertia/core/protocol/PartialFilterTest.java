package io.github.diovamny.inertia.core.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class PartialFilterTest {

    @Test
    void matchesComponentRequiresEquality() {
        assertThat(PartialFilter.matchesComponent("Users/Index", "Users/Index")).isTrue();
        assertThat(PartialFilter.matchesComponent("Users/Index", "Users/Show")).isFalse();
        assertThat(PartialFilter.matchesComponent(null, "Users/Index")).isFalse();
        assertThat(PartialFilter.matchesComponent("Users/Index", null)).isFalse();
    }

    @Test
    void parseCsvTrimsDedupsAndDropsBlanks() {
        assertThat(PartialFilter.parseCsv("a, b,,a , ,c")).containsExactly("a", "b", "c");
        assertThat(PartialFilter.parseCsv(null)).isEmpty();
        assertThat(PartialFilter.parseCsv("  ")).isEmpty();
        assertThat(PartialFilter.parseCsvList("x,y")).isEqualTo(List.of("x", "y"));
    }

    @Test
    void emptyFiltersPassEverythingPlusAlways() {
        var props = Map.<String, Object>of("a", 1, "b", 2);
        var filtered = PartialFilter.filter(props, Map.of("errors", Map.of()), Set.of(), Set.of());
        assertThat(filtered).containsKeys("a", "b", "errors");
    }

    @Test
    void onlyNarrowsAndExceptRemoves() {
        var props = new LinkedHashMap<String, Object>();
        props.put("a", 1);
        props.put("b", 2);
        props.put("c", 3);
        var filtered = PartialFilter.filter(props, Map.of(), Set.of("a", "b"), Set.of("b"));
        assertThat(filtered).containsOnlyKeys("a");
    }

    @Test
    void dotNotationSelectsNestedPaths() {
        var props = new LinkedHashMap<String, Object>();
        props.put("users", new LinkedHashMap<>(Map.of("data", List.of(1), "meta", Map.of("page", 2))));
        props.put("other", 9);
        var filtered = PartialFilter.filter(props, Map.of(), Set.of("users.data"), Set.of());
        assertThat(filtered).containsOnlyKeys("users");
        assertThat(filtered.get("users")).isEqualTo(Map.of("data", List.of(1)));
    }

    @Test
    void dotNotationExceptPrunesNestedPaths() {
        var props = new LinkedHashMap<String, Object>();
        props.put("users", new LinkedHashMap<>(Map.of("data", List.of(1), "meta", Map.of("page", 2))));
        var filtered = PartialFilter.filter(props, Map.of(), Set.of(), Set.of("users.meta"));
        assertThat(filtered.get("users")).isEqualTo(Map.of("data", List.of(1)));
    }

    @Test
    void alwaysSurvivesExcept() {
        var props = Map.<String, Object>of("a", 1);
        var filtered = PartialFilter.filter(props, Map.of("errors", Map.of()), Set.of(), Set.of("errors", "a"));
        assertThat(filtered).containsKey("errors");
        assertThat(filtered).doesNotContainKey("a");
    }

    @Test
    void selectedNullIsKept() {
        var props = new LinkedHashMap<String, Object>();
        props.put("a", null);
        var filtered = PartialFilter.filter(props, Map.of(), Set.of("a"), Set.of());
        assertThat(filtered).containsKey("a");
        assertThat(filtered.get("a")).isNull();
    }

    @Test
    void matchingPrefixesAndSelection() {
        assertThat(PartialFilter.matchingPrefixes("users", Set.of("users.data", "other")))
            .containsExactly("data");
        assertThat(PartialFilter.isSelected("users", Set.of("users.data"))).isTrue();
        assertThat(PartialFilter.isSelected("users", Set.of("other"))).isFalse();
        assertThat(PartialFilter.isExcluded("users", Set.of("users"))).isTrue();
        assertThat(PartialFilter.isExcluded("users", Set.of("users.data"))).isFalse();
    }
}
