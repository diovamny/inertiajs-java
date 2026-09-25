package io.github.diovamny.quarkus.inertia.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class ErrorBagsTest {

    @Test
    void wrapWithoutRequestPassesErrorsThrough() {
        var errors = Map.of("email", "required");
        assertThat(ErrorBags.wrap(errors)).isSameAs(errors);
    }

    @Test
    void wrapWithExplicitBagName() {
        var errors = Map.of("title", "required");
        assertThat(ErrorBags.wrap(errors, "probeSecondary"))
            .isEqualTo(Map.of("probeSecondary", Map.of("title", "required")));
    }

    @Test
    void wrapWithBlankOrNullBagPassesThrough() {
        var errors = Map.of("title", "required");
        assertThat(ErrorBags.wrap(errors, null)).isSameAs(errors);
        assertThat(ErrorBags.wrap(errors, "  ")).isSameAs(errors);
        assertThat(ErrorBags.wrap(null, "bag")).isNull();
    }
}
