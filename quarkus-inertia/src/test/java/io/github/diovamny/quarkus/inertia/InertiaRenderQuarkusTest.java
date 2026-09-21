package io.github.diovamny.quarkus.inertia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

import io.github.diovamny.quarkus.inertia.api.Inertia;

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
        assertThat(result).isInstanceOf(io.github.diovamny.quarkus.inertia.api.InertiaRedirect.class);
    }

    @Test
    void shouldRedirectReturnChainableUni() {
        var result = inertia.redirect("/login").with("success", "Registro actualizado correctamente.");
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(io.github.diovamny.quarkus.inertia.api.InertiaRedirect.class);
    }

    @Test
    void shouldHandleErrorUsingMapperReturnUni() {
        assertThatCode(() -> inertia.handleErrorUsing(error -> null)).doesNotThrowAnyException();
    }

    @Test
    void shouldRenderReturnChainableRender() {
        var result = inertia.render("Home", java.util.Map.of("name", "World"));
        assertThat(result).isNotNull();
        assertThat(result)
            .isInstanceOf(io.github.diovamny.quarkus.inertia.api.InertiaRender.class);
    }

    @Test
    void shouldRenderTypedPageResult() {
        var result = inertia.render(
            io.github.diovamny.inertia.core.result.InertiaPageResult.of("Home",
                java.util.Map.of("name", "World")));
        assertThat(result).isNotNull();
        assertThat(result)
            .isInstanceOf(io.github.diovamny.quarkus.inertia.api.InertiaRender.class);
    }

    @Test
    void shouldRedirectTypedRedirectResult() {
        var result = inertia.redirect(
            io.github.diovamny.inertia.core.result.InertiaRedirectResult.to("/login"));
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(io.github.diovamny.quarkus.inertia.api.InertiaRedirect.class);
    }

    @Test
    void shouldLocateTypedLocationResult() {
        var result = inertia.location(
            io.github.diovamny.inertia.core.result.InertiaLocationResult.to("https://example.com"));
        assertThat(result).isNotNull();
    }
}
