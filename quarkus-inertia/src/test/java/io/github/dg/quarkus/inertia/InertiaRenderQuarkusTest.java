package io.github.dg.quarkus.inertia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

import io.github.dg.quarkus.inertia.api.Inertia;

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

    enum HomePage { HOME }

    @Test
    void shouldRenderEnumComponentByName() {
        var result = inertia.render(HomePage.HOME);
        assertThat(result).isNotNull();
    }

    @Test
    void shouldRedirectWithFullPageReturnUni() {
        var result = inertia.redirect("/external", true);
        assertThat(result).isNotNull();
    }

    @Test
    void shouldBackReturnChainableUni() {
        var result = inertia.back().with("success", "Registro actualizado correctamente.");
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(io.github.dg.quarkus.inertia.api.InertiaRedirect.class);
    }

    @Test
    void shouldRedirectReturnChainableUni() {
        var result = inertia.redirect("/login").with("success", "Registro actualizado correctamente.");
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(io.github.dg.quarkus.inertia.api.InertiaRedirect.class);
    }

    @Test
    void shouldHandleErrorUsingMapperReturnUni() {
        assertThatCode(() -> inertia.handleErrorUsing(error -> null)).doesNotThrowAnyException();
    }
}
