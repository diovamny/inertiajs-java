package io.github.dg.spring.inertia.testing;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InertiaPageTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    @Test
    void parsesPagePayload() {
        var json = """
            {"component":"Dashboard","props":{"title":"Home","count":3},"url":"/dashboard","version":"v1"}
            """;
        var page = InertiaPage.fromJson(json, MAPPER);
        assertEquals("Dashboard", page.component());
        assertEquals("/dashboard", page.url());
        assertEquals("v1", page.version());
        assertEquals("Home", page.prop("title"));
        assertEquals(3, page.prop("count"));
        assertTrue(page.hasProp("title"));
        assertFalse(page.hasProp("missing"));
    }

    @Test
    void toleratesMissingVersion() {
        var page = InertiaPage.fromJson("{\"component\":\"C\",\"props\":{},\"url\":\"/\"}", MAPPER);
        assertEquals(null, page.version());
    }

    @Test
    void rejectsInvalidPayload() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
            () -> InertiaPage.fromJson("not json", MAPPER));
    }
}