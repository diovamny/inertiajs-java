package io.github.diovamny.spring.inertia.renderer;

import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import io.github.diovamny.spring.inertia.config.InertiaProperties;
import io.github.diovamny.spring.inertia.internal.JacksonJsonProvider;
import io.github.diovamny.spring.inertia.model.PageObject;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlRendererTest {

    @Test
    void rendersHtmlWithPageAndSafeJson() {
        var properties = new InertiaProperties();
        var jsonProvider = new JacksonJsonProvider(new ObjectMapper());
        var ssrClient = new SsrClient(properties, jsonProvider);
        var renderer = new HtmlRenderer(properties, jsonProvider, ssrClient);

        var page = new PageObject("Home", Map.of("message", "Hello"), "/", "v1", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        var html = renderer.render(page);

        assertNotNull(html);
        assertTrue(html.contains("data-page="));
        assertTrue(html.contains("Home"));
    }

    @Test
    void replacesViewDataPlaceholders() {
        var properties = new InertiaProperties();
        var jsonProvider = new JacksonJsonProvider(new ObjectMapper());
        var ssrClient = new SsrClient(properties, jsonProvider);
        var renderer = new HtmlRenderer(properties, jsonProvider, ssrClient);

        var page = new PageObject("Home", Map.of(), "/", "v1", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        var html = renderer.render(page, Map.of("title", "My Custom Title"));

        assertNotNull(html);
    }
}
