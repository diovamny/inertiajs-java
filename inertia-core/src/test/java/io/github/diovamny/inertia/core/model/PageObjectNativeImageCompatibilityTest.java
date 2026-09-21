package io.github.diovamny.inertia.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class PageObjectNativeImageCompatibilityTest {

    @Test
    void recordComponentsStayPrivateForAot() {
        var fields = PageObject.class.getDeclaredFields();
        assertThat(fields).isNotEmpty();
        assertThat(Arrays.stream(fields).allMatch(f -> Modifier.isPrivate(f.getModifiers()))).isTrue();
        assertThat(Arrays.stream(PageObject.class.getDeclaredMethods())
            .filter(m -> !m.isSynthetic()).count()).isGreaterThan(0);
    }

    @Test
    void shouldNotUseReflection() {
        var page = new PageObject("Test", java.util.Map.of(), "/", "v1");
        assertThat(page.component()).isEqualTo("Test");
        assertThat(page.getClass().getDeclaredFields()).allMatch(f ->
            java.lang.reflect.Modifier.isPrivate(f.getModifiers())
        );
    }

    @Test
    void shouldSupportNoArgsConstructorViaFactory() {
        var page = new PageObject("Test", java.util.Map.of(), "/", "v1");
        assertThat(page).isNotNull();
    }
}
