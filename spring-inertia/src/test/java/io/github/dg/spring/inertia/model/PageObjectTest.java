package io.github.dg.spring.inertia.model;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageObjectTest {

    @Test
    void buildsCorePage() {
        var page = new PageObject("Dashboard", Map.of("title", "Home"), "/dashboard", "v1");
        assertEquals("Dashboard", page.component());
        assertEquals(Map.of("title", "Home"), page.props());
        assertEquals("/dashboard", page.url());
        assertEquals("v1", page.version());
        assertFalse(page.hasMetadata());
    }

    @Test
    void rejectsBlankComponent() {
        assertThrows(IllegalArgumentException.class,
            () -> new PageObject("", Map.of(), "/", "v1"));
    }

    @Test
    void normalizesNullProps() {
        var page = new PageObject("Dashboard", null, "/", "v1");
        assertEquals(Map.of(), page.props());
    }

    @Test
    void withPropsKeepsMetadata() {
        var page = new PageObject("Dashboard", Map.of("a", 1), "/", "v1",
            Map.of("m", "x"), Map.of("g", java.util.List.of("p")), java.util.List.of("p"),
            null, null, null, null, null, null, null, null, false, false, false);
        var updated = page.withProps(Map.of("b", 2));
        assertEquals(Map.of("b", 2), updated.props());
        assertEquals("x", updated.flash().get("m"));
        assertTrue(updated.hasDeferredProps());
    }

    @Test
    void withVersionAndUrl() {
        var page = new PageObject("Home", Map.of(), "/a", "v1");
        assertEquals("v2", page.withVersion("v2").version());
        assertEquals("/b", page.withUrl("/b").url());
    }

    @Test
    void mergeMetadataRoundTrip() {
        var page = new PageObject("Home", Map.of(), "/a", "v1")
            .withMergeMetadata(java.util.List.of("users"), java.util.List.of(), java.util.List.of(), java.util.List.of());
        assertEquals(java.util.List.of("users"), page.mergeProps());
    }

    @Test
    void metadataFlags() {
        var page = new PageObject("Home", Map.of(), "/a", "v1", null,
            null, null, null, null, null, Map.of("n", new OnceProp("n", null)),
            null, null, null, null, false, false, false);
        assertTrue(page.hasMetadata());
        assertTrue(page.onceProps().containsKey("n"));
    }
}
