package io.github.dg.quarkus.inertia.config;

import static org.assertj.core.api.Assertions.*;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class InertiaConfigQuarkusTest {

    @Inject
    InertiaConfig config;

    @Inject
    InertiaConfigValidator validator;

    @Test
    void shouldInjectConfig() {
        assertThat(config).isNotNull();
        assertThat(config.rootTemplate()).isEqualTo("index.html");
        assertThat(config.versionStrategy()).isEqualTo("sha256");
    }

    @Test
    void shouldInjectValidator() {
        assertThat(validator).isNotNull();
    }
}
