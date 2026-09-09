package io.github.dg.quarkus.inertia.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.dg.quarkus.inertia.model.RawJson;

class RawJsonUnwrapperUnitTest {

    @Test
    void parsesRawJsonObject() {
        var result = RawJsonUnwrapper.unwrap(RawJson.of("{\"a\":1,\"b\":\"x\"}"));

        assertThat(result).isInstanceOf(Map.class);
        var parsed = (Map<String, Object>) result;
        assertThat(parsed)
            .containsEntry("a", 1L)
            .containsEntry("b", "x");
    }

    @Test
    void parsesRawJsonArray() {
        var result = RawJsonUnwrapper.unwrap(RawJson.of("[1,2,3]"));

        assertThat(result).isEqualTo(List.of(1L, 2L, 3L));
    }

    @Test
    void unwrapsNestedRawJsonInsideMap() {
        var nested = RawJson.of("{\"deep\":[true,false,null]}");
        var result = RawJsonUnwrapper.unwrap(Map.of("outer", nested));

        assertThat(result).isInstanceOf(Map.class);
        var outer = (Map<String, Object>) result;
        assertThat(outer.get("outer")).isInstanceOf(Map.class);
        var inner = (Map<String, Object>) outer.get("outer");
        assertThat(inner.get("deep")).isEqualTo(java.util.Arrays.asList(Boolean.TRUE, Boolean.FALSE, null));
    }

    @Test
    void detectsRawJsonPresence() {
        assertThat(RawJsonUnwrapper.containsRawJson(Map.of("k", RawJson.of("{}")))).isTrue();
        assertThat(RawJsonUnwrapper.containsRawJson(List.of("plain", 1))).isFalse();
        assertThat(RawJsonUnwrapper.containsRawJson("plain")).isFalse();
    }
}
