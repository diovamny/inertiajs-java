package com.quarkus.inertia;

import static org.assertj.core.api.Assertions.*;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

import com.quarkus.inertia.api.Inertia;

@QuarkusTest
class InertiaRenderQuarkusTest {

    @Inject
    Inertia inertia;

    @Test
    void shouldRenderReturnUni() {
        var result = inertia.render("Home", java.util.Map.of("name", "World"));
        assertThat(result).isNotNull();
    }

    @Test
    void shouldRedirectReturnUni() {
        var result = inertia.redirect("/login");
        assertThat(result).isNotNull();
    }

    @Test
    void shouldLocationReturnUni() {
        var result = inertia.location("https://example.com");
        assertThat(result).isNotNull();
    }
}
