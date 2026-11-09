package io.github.diovamny.spring.inertia.internal;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.diovamny.inertia.core.model.PageObject;
import io.github.diovamny.inertia.core.model.RawJson;
import io.github.diovamny.spring.inertia.protocol.PageObjectBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JacksonJsonProviderTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();
    private final JacksonJsonProvider provider = new JacksonJsonProvider(mapper);
    private final JacksonJsonProvider bigintProvider = new JacksonJsonProvider(mapper, true);

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

    @Test
    void bigintDisabledLeavesLongsPlain() {
        var page = new PageObject("Index", Map.of("id", Long.MAX_VALUE), "/", "v1");
        var json = provider.toJson(page);
        assertTrue(json.contains("\"id\":9223372036854775807"));
        assertFalse(json.contains("preserveBigIntegers"));
    }

    @Test
    void bigintEnabledMarksUnsafeLongsAndSetsPageFlag() {
        var page = new PageObject("Index", Map.of("id", Long.MAX_VALUE), "/", "v1");
        var json = bigintProvider.toJson(page);
        assertTrue(json.contains("\"id\":{\"$bigint\":\"9223372036854775807\"}"));
        assertTrue(json.contains("\"preserveBigIntegers\":true"));
    }

    @Test
    void bigintEnabledKeepsSafeValuesPlain() {
        assertEquals("{\"n\":42}", bigintProvider.toJson(Map.of("n", 42L)));
    }

    @Test
    void bigintEnabledWithoutPageDoesNotAddFlag() {
        var json = bigintProvider.toJson(Map.of("id", Long.MAX_VALUE));
        assertTrue(json.contains("\"id\":{\"$bigint\":\"9223372036854775807\"}"));
        assertFalse(json.contains("preserveBigIntegers"));
    }

    @Test
    void pageFlagOptsInWhenGlobalDisabled() {
        var page = new PageObject("Index", Map.of("id", Long.MAX_VALUE), "/", "v1")
            .withPreserveBigIntegers(Boolean.TRUE);
        var json = provider.toJson(page);
        assertTrue(json.contains("\"id\":{\"$bigint\":\"9223372036854775807\"}"));
        assertTrue(json.contains("\"preserveBigIntegers\":true"));
    }

    @Test
    void pageFlagFalseOverridesGlobalEnabled() {
        var page = new PageObject("Index", Map.of("id", Long.MAX_VALUE), "/", "v1")
            .withPreserveBigIntegers(Boolean.FALSE);
        var json = bigintProvider.toJson(page);
        assertTrue(json.contains("\"id\":9223372036854775807"));
        assertFalse(json.contains("$bigint"));
        assertFalse(json.contains("preserveBigIntegers"));
    }

    @Test
    void requestOverrideWinsOverPageFlagAndGlobal() {
        var request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            InertiaRequestContext.set(PageObjectBuilder.CONTEXT_PRESERVE_BIG_INTEGERS, Boolean.TRUE);
            var json = provider.toJson(new PageObject("Index", Map.of("id", Long.MAX_VALUE), "/", "v1"));
            assertTrue(json.contains("\"id\":{\"$bigint\":\"9223372036854775807\"}"));
            assertTrue(json.contains("\"preserveBigIntegers\":true"));

            InertiaRequestContext.set(PageObjectBuilder.CONTEXT_PRESERVE_BIG_INTEGERS, Boolean.FALSE);
            var optedOut = bigintProvider.toJson(
                new PageObject("Index", Map.of("id", Long.MAX_VALUE), "/", "v1")
                    .withPreserveBigIntegers(Boolean.TRUE));
            assertTrue(optedOut.contains("\"id\":9223372036854775807"));
            assertFalse(optedOut.contains("$bigint"));
            assertFalse(optedOut.contains("preserveBigIntegers"));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void unsafeBigIntegersTravelAsMarkers() {
        var huge = new BigInteger("123456789012345678901234567890");
        var page = new PageObject("Orders", Map.of("id", huge), "/", "v1")
            .withPreserveBigIntegers(Boolean.TRUE);
        assertTrue(provider.toJson(page)
            .contains("\"id\":{\"$bigint\":\"123456789012345678901234567890\"}"));

        var safe = new PageObject("Orders", Map.of("id", BigInteger.valueOf(7)), "/", "v1")
            .withPreserveBigIntegers(Boolean.TRUE);
        assertTrue(provider.toJson(safe).contains("\"id\":7"));
        assertFalse(provider.toJson(safe).contains("$bigint"));
    }
}
