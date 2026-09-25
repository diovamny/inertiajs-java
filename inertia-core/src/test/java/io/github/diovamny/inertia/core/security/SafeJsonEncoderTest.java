package io.github.diovamny.inertia.core.security;

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

    @Test
    void breaksOutOfScriptContext() {
        var input = "</script><script>alert(1)</script>";
        var result = SafeJsonEncoder.encodeForScript(input);
        assertFalse(result.contains("</script>"));
        assertFalse(result.contains("<script>"));
    }

    @Test
    void escapesHtmlCommentBreakout() {
        var input = "<!--<script>alert(1)</script>-->";
        var result = SafeJsonEncoder.encodeForScript(input);
        assertFalse(result.contains("<!--"));
        assertFalse(result.contains("</script>"));
    }

    @Test
    void escapesSvgAndImgEventHandlers() {
        var svg = "<svg/onload=alert(1)>";
        var img = "<img src=x onerror=alert(1)>";
        assertFalse(SafeJsonEncoder.encodeForScript(svg).contains("<svg"));
        assertFalse(SafeJsonEncoder.encodeForScript(img).contains("<img"));
        assertTrue(SafeJsonEncoder.encodeForScript(svg).contains("\\u003c"));
        assertTrue(SafeJsonEncoder.encodeForScript(img).contains("\\u003e"));
    }

    @Test
    void preservesSurrogatePairsAndEmoji() {
        var input = "{\"emoji\":\"\uD83D\uDE00\"}";
        var result = SafeJsonEncoder.encodeForScript(input);
        assertTrue(result.contains("\uD83D\uDE00"));
        assertFalse(result.contains("</script>"));
    }

    @Test
    void escapesComplexQuotedPayload() {
        var input = "{\"x\":\"\\\"</script>\\\"\"}";
        var result = SafeJsonEncoder.encodeForScript(input);
        assertFalse(result.contains("</script>"));
        assertTrue(result.contains("\\/script"));
    }

    @Test
    void handlesNullInput() {
        assertEquals("", SafeJsonEncoder.encodeForScript(null));
    }

    @Test
    void escapesLeadingLessThanPerInertia371() {
        // Parity with inertiajs/inertia v3.7.1 (#3253): no raw '<' may reach
        // the initial page JSON, even at the start of a prop value.
        var result = SafeJsonEncoder.encodeForScript("<div>hello</div>");
        assertFalse(result.contains("<"));
        assertTrue(result.startsWith("\\u003c"));
    }
}
