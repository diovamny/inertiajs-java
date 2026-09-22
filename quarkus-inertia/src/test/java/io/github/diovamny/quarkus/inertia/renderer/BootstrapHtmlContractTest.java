package io.github.diovamny.quarkus.inertia.renderer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.diovamny.inertia.core.model.PageObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * H11: structural bootstrap contract (PLAN v4 §5 fixtures), Quarkus side.
 * Same document-level guarantees as the Spring renderer: adversarial prop
 * values stay inside the {@code <script data-page="app">} element and the
 * payload is never duplicated into {@code <div id="app">}.
 */
class BootstrapHtmlContractTest {

    private static final Pattern SCRIPT =
        Pattern.compile("<script[^>]*data-page=\"app\"[^>]*>([\\s\\S]*?)</script>");

    @Test
    void fullDocumentKeepsAdversarialPropsInsideScriptOnly() throws Exception {
        var config = mock(io.github.diovamny.quarkus.inertia.config.InertiaConfig.class);
        when(config.maxPageBytes()).thenReturn(33554432L);
        when(config.rootTemplate()).thenReturn("bootstrap-test.html");
        when(config.rootView()).thenReturn(Optional.empty());
        when(config.useQute()).thenReturn(false);
        var ssrHandler = mock(SsrHandler.class);
        when(ssrHandler.isSsrEnabled()).thenReturn(false);
        var nonceProviders = mock(jakarta.enterprise.inject.Instance.class);
        when(nonceProviders.isUnsatisfied()).thenReturn(true);

        var renderer = new HtmlRenderer(null, null, mock(jakarta.enterprise.inject.Instance.class),
            config, ssrHandler, nonceProviders);

        var props = new LinkedHashMap<String, Object>();
        props.put("html_close_script", "</script>");
        props.put("html_open_script", "<script>alert(1)</script>");
        props.put("html_entities", "& < > \" '");
        props.put("json_escapes", "\n\r\t\\\"/");
        props.put("line_separator", "\u2028");
        props.put("paragraph_separator", "\u2029");
        props.put("emoji", "🇩🇴🚀🎉");
        props.put("large_prop", "x".repeat(1_000_000));

        var json = new ObjectMapper().writeValueAsString(Map.of(
            "component", "Adversarial", "props", props, "url", "/adversarial", "version", "v1"));
        var page = new PageObject("Adversarial", props, "/adversarial", "v1", null, null, null,
            null, null, null, null, null, null, null, null, null, null, null);
        var html = renderer.renderSync(page, json);

        assertTrue(html.contains("<div id=\"app\">"), "div must be payload-free");
        assertFalse(html.contains("<div id=\"app\" data-page"), "no legacy data-page attribute");
        var matcher = SCRIPT.matcher(html);
        assertTrue(matcher.find(), "script data-page=app element must exist");
        assertEquals(1, html.split("data-page=\"app\"", -1).length - 1,
            "payload must travel exactly once");

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
