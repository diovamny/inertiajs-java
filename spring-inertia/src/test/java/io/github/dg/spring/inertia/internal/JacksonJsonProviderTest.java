package io.github.dg.spring.inertia.internal;

import java.util.List;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import io.github.dg.spring.inertia.model.RawJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JacksonJsonProviderTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();
    private final JacksonJsonProvider provider = new JacksonJsonProvider(mapper);

    @Test
    void serializesPlainValues() {
        assertEquals("{\"a\":1}", provider.toJson(Map.of("a", 1)));
        assertEquals("\"text\"", provider.toJson("text"));
    }

    @Test
    void embedsRawJsonVerbatim() {
        var json = provider.toJson(Map.of(
            "widget", RawJson.of("{\"kind\":\"chart\",\"data\":[1,2,3]}"),
            "title", "t"));
        assertTrue(json.contains("\"kind\":\"chart\""));
        assertTrue(json.contains("\"title\":\"t\""));
        assertFalse(json.contains("\\\\\""));
    }

    @Test
    void unwrapsNestedRawJson() {
        var json = provider.toJson(Map.of(
            "nested", Map.of("deep", RawJson.of("[1,2]")),
            "list", List.of(RawJson.of("{\"x\":true}"))));
        assertTrue(json.contains("[1,2]"));
        assertTrue(json.contains("\"x\":true"));
    }

    @Test
    void detectsRawJsonPresence() {
        assertTrue(RawJsonUnwrapper.containsRawJson(Map.of("a", RawJson.of("{}"))));
        assertFalse(RawJsonUnwrapper.containsRawJson(Map.of("a", 1)));
    }

    @Test
    void deserializes() {
        var value = provider.fromJson("{\"a\":1}", Map.class);
        assertEquals(Map.of("a", 1), value);
    }

    @Test
    void malformedRawJsonFallsBackToString() {
        var json = provider.toJson(Map.of("broken", RawJson.of("not-json{")));
        assertTrue(json.contains("not-json{"));
    }
}
