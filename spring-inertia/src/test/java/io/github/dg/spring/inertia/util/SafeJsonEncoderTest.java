package io.github.dg.spring.inertia.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeJsonEncoderTest {

    @Test
    void escapesForwardSlash() {
        var input = "\"url\":\"/events/80\"";
        var result = SafeJsonEncoder.encodeForScript(input);
        assertEquals("\"url\":\"\\/events\\/80\"", result);
    }

    @Test
    void escapesScriptTag() {
        var input = "</script><script>alert(1)</script>";
        var result = SafeJsonEncoder.encodeForScript(input);
        assertFalse(result.contains("</script>"));
        assertTrue(result.contains("\\u003c"));
        assertTrue(result.contains("\\u003e"));
    }

    @Test
    void escapesHtmlEntities() {
        var input = "<div class=\"x\">&amp;</div>";
        var result = SafeJsonEncoder.encodeForScript(input);
        assertTrue(result.contains("\\u003c"));
        assertTrue(result.contains("\\u003e"));
        assertTrue(result.contains("\\u0026"));
    }

    @Test
    void escapesUnicodeSeparators() {
        var input = "line\u2028separator\u2029paragraph";
        var result = SafeJsonEncoder.encodeForScript(input);
        assertTrue(result.contains("\\u2028"));
        assertTrue(result.contains("\\u2029"));
    }

    @Test
    void handlesEmptyString() {
        assertEquals("", SafeJsonEncoder.encodeForScript(""));
    }

    @Test
    void preservesValidJson() {
        var input = "{\"key\":\"value\",\"num\":42}";
        var result = SafeJsonEncoder.encodeForScript(input);
        assertEquals("{\"key\":\"value\",\"num\":42}", result);
    }

    @Test
    void urlWithPathEscapedCorrectly() {
        var input = "\"component\":\"User/Index\",\"url\":\"/users/42?page=2\"";
        var result = SafeJsonEncoder.encodeForScript(input);
        assertTrue(result.contains("\\/users\\/42?page=2"));
    }
}
