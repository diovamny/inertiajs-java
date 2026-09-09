package io.github.diovamny.quarkus.inertia.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import io.quarkus.runtime.annotations.RegisterForReflection;

class PageObjectNativeImageCompatibilityTest {

    @Test
    void shouldHaveRegisterForReflectionAnnotation() {
        var annotation = PageObject.class.getAnnotation(RegisterForReflection.class);
        assertThat(annotation).as("PageObject must be annotated with @RegisterForReflection").isNotNull();
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
