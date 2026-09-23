package io.github.diovamny.inertia.core.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class MergePlanUnitTest {

    @Test
    void qualifiesNestedAppendPrependAndMatchOn() {
        var plan = MergePlan.builder("posts", Map.of("data", java.util.List.of(), "pinned", java.util.List.of()))
            .append("data")
            .prepend("pinned")
            .matchOn("data.id")
            .build();
        assertThat(plan.qualifiedMergePaths()).containsExactly("posts.data");
        assertThat(plan.qualifiedPrependPaths()).containsExactly("posts.pinned");
        assertThat(plan.qualifiedMatchPaths()).containsExactly("posts.data.id");
    }

    @Test
    void lastOperationWinsOnDuplicatePaths() {
        var plan = MergePlan.builder("items", java.util.List.of())
            .append("data")
            .prepend("data")
            .build();
        assertThat(plan.qualifiedMergePaths()).isEmpty();
        assertThat(plan.qualifiedPrependPaths()).containsExactly("items.data");
    }

    @Test
    void matchOnResolvesAgainstRootWithoutPaths() {
        var plan = MergePlan.builder("items", java.util.List.of()).matchOn("id").build();
        assertThat(plan.qualifiedMatchPaths()).containsExactly("items.id");
    }

    @Test
    void multipleMatchOnFields() {
        var plan = MergePlan.builder("items", java.util.List.of())
            .append("data")
            .matchOn("data.id", "data.sku")
            .build();
        assertThat(plan.qualifiedMatchPaths()).containsExactlyInAnyOrder("items.data.id", "items.data.sku");
    }

    @Test
    void rejectsBlankAndMalformedPaths() {
        assertThatThrownBy(() -> MergePlan.builder("p", 1).append(" "))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MergePlan.builder("p", 1).append(".data"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MergePlan.builder(" ", 1))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
