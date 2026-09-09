package io.github.diovamny.quarkus.inertia.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AlwaysPropUnitTest {

    @Test
    void shouldWrapValue() {
        var prop = AlwaysProp.of("test");
        assertThat(prop.value()).isEqualTo("test");
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> AlwaysProp.of(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotDoubleWrap() {
        var inner = AlwaysProp.of("value");
        var wrapped = AlwaysProp.of(inner);
        assertThat(wrapped).isSameAs(inner);
    }

    @Test
    void shouldPreserveOriginalValueType() {
        var prop = AlwaysProp.of(42);
        assertThat(prop.value()).isEqualTo(42);
    }
}
