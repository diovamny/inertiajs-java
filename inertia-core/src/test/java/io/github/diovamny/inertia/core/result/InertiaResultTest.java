package io.github.diovamny.inertia.core.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;

class InertiaResultTest {

    @Test
    void pageResultNormalizesNulls() {
        var page = new InertiaPageResult("Home", null, null);
        assertThat(page.component()).isEqualTo("Home");
        assertThat(page.props()).isEmpty();
        assertThat(page.meta()).isEmpty();
    }

    @Test
    void pageResultFactories() {
        assertThat(InertiaPageResult.of("Home").props()).isEmpty();
        assertThat(InertiaPageResult.of("Home", Map.of("a", 1)).props())
            .containsEntry("a", 1);
    }

    @Test
    void pageResultRejectsBlankComponent() {
        assertThatThrownBy(() -> InertiaPageResult.of(" "))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void redirectResultFactories() {
        assertThat(InertiaRedirectResult.to("/a").fullPage()).isFalse();
        assertThat(InertiaRedirectResult.fullPage("/a").fullPage()).isTrue();
        assertThatThrownBy(() -> InertiaRedirectResult.to(" "))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void locationResultFactory() {
        assertThat(InertiaLocationResult.to("https://example.com").url())
            .isEqualTo("https://example.com");
        assertThatThrownBy(() -> InertiaLocationResult.to(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void hierarchyIsSealedAndExhaustive() {
        InertiaResult result = InertiaPageResult.of("Home");
        var label = switch (result) {
            case InertiaPageResult page -> "page:" + page.component();
            case InertiaRedirectResult redirect -> "redirect:" + redirect.url();
            case InertiaLocationResult location -> "location:" + location.url();
        };
        assertThat(label).isEqualTo("page:Home");
    }
}
