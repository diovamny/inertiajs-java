package io.github.diovamny.inertia.core.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ValidationErrorsUnitTest {

    @Test
    void legacyWireKeepsFirstMessagePerField() {
        var errors = ValidationErrors.builder()
            .add("email", "required")
            .add("email", "must be valid")
            .add("name", "required")
            .build();
        assertThat(errors.toWireMap(false)).isEqualTo(Map.of("email", "required", "name", "required"));
    }

    @Test
    void allErrorsWireEmitsOrderedArrays() {
        var errors = ValidationErrors.builder()
            .add("email", "required")
            .add("email", "must be valid")
            .add("name", "required")
            .build();
        assertThat(errors.toWireMap(true)).isEqualTo(Map.of(
            "email", List.of("required", "must be valid"),
            "name", List.of("required")));
    }

    @Test
    void factoriesPreserveOrder() {
        var errors = ValidationErrors.ofLists(Map.of("email", List.of("a", "b")));
        assertThat(errors.messagesFor("email")).containsExactly("a", "b");
        assertThat(ValidationErrors.of(Map.of("email", "a")).firstMessageFor("email")).isEqualTo("a");
    }

    @Test
    void filterKeepsOnlyRequestedFields() {
        var errors = ValidationErrors.builder().add("a", "1").add("b", "2").build();
        assertThat(errors.filter(List.of("b")).fields()).containsExactly("b");
    }

    @Test
    void mergeAppendsDuplicateFieldsInOrder() {
        var left = ValidationErrors.builder().add("email", "required").build();
        var right = ValidationErrors.builder().add("email", "must be valid").add("name", "x").build();
        assertThat(left.merge(right).messagesFor("email")).containsExactly("required", "must be valid");
    }

    @Test
    void rejectsNullFieldOrMessage() {
        assertThatThrownBy(() -> ValidationErrors.builder().add(null, "x"))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ValidationErrors.builder().add("x", null))
            .isInstanceOf(NullPointerException.class);
    }
}
