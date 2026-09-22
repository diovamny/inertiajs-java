package io.github.diovamny.spring.inertia.renderer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import io.github.diovamny.inertia.core.model.PageObject;
import io.github.diovamny.spring.inertia.config.InertiaProperties;
import io.github.diovamny.spring.inertia.internal.JacksonJsonProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * H11: structural bootstrap contract (PLAN v4 §5 fixtures). The full HTML
 * document — not just the encoder — must keep every adversarial prop value
 * inside the {@code <script type="application/json" data-page="app">} element
 * and must not duplicate the payload into {@code <div id="app">}.
 */
class BootstrapHtmlContractTest {

    private static final Pattern SCRIPT =
        Pattern.compile("<script[^>]*data-page=\"app\"[^>]*>([\\s\\S]*?)</script>");

    static Map<String, Object> adversarialProps() {
        var props = new LinkedHashMap<String, Object>();
        props.put("html_close_script", "</script>");
        props.put("html_open_script", "<script>alert(1)</script>");
        props.put("html_entities", "& < > \" '");
        props.put("json_escapes", "\n\r\t\\\"/");
        props.put("line_separator", "\u2028");
        props.put("paragraph_separator", "\u2029");
        props.put("emoji", "🇩🇴🚀🎉");
        props.put("large_prop", "x".repeat(1_000_000));
        return props;
    }

    @Test
    void fullDocumentKeepsAdversarialPropsInsideScriptOnly() throws Exception {
        var properties = new InertiaProperties();
        var jsonProvider = new JacksonJsonProvider(new ObjectMapper());
        var renderer = new HtmlRenderer(properties, jsonProvider,
            new SsrClient(properties, jsonProvider));

        var props = adversarialProps();
        var page = new PageObject("Adversarial", props, "/adversarial", "v1", null, null, null,
            null, null, null, null, null, null, null, null, null, null, null);
        var html = renderer.render(page);

        // v3-pure: clean div, single script payload.
        assertTrue(html.contains("<div id=\"app\">"), "div must be payload-free");
        assertFalse(html.contains("<div id=\"app\" data-page"), "no legacy data-page attribute");
        var matcher = SCRIPT.matcher(html);
        assertTrue(matcher.find(), "script data-page=app element must exist");
        assertEquals(1, html.split("data-page=\"app\"", -1).length - 1,
            "payload must travel exactly once");

        // The script content must be parseable JSON that round-trips every fixture.
        var parsed = new ObjectMapper().readTree(matcher.group(1));
        assertEquals("Adversarial", parsed.get("component").asText());
        var parsedProps = parsed.get("props");
        assertNotNull(parsedProps);
        for (var entry : props.entrySet()) {
            assertTrue(parsedProps.has(entry.getKey()), "missing prop " + entry.getKey());
            assertEquals(String.valueOf(entry.getValue()),
                parsedProps.get(entry.getKey()).asText(), "fixture broken: " + entry.getKey());
        }
    }
}
