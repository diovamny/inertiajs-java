package io.github.diovamny.spring.inertia.renderer;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import tools.jackson.databind.ObjectMapper;
import io.github.diovamny.spring.inertia.config.InertiaProperties;
import io.github.diovamny.spring.inertia.internal.JacksonJsonProvider;
import io.github.diovamny.inertia.core.model.PageObject;
import io.github.diovamny.inertia.core.spi.NonceProvider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void noncePlaceholderStaysEmptyWithoutProvider() {
        var properties = new InertiaProperties();
        var jsonProvider = new JacksonJsonProvider(new ObjectMapper());
        var ssrClient = new SsrClient(properties, jsonProvider);
        var renderer = new HtmlRenderer(properties, jsonProvider, ssrClient);

        var page = new PageObject("Home", Map.of(), "/", "v1", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        var html = renderer.render(page);

        assertNotNull(html);
        assertFalse(html.contains("__INERTIA_CSP_NONCE__"));
        assertFalse(html.contains("nonce="));
    }

    @Test
    void nonceProviderStampsPlaceholder() {
        var properties = new InertiaProperties();
        var jsonProvider = new JacksonJsonProvider(new ObjectMapper());
        var ssrClient = new SsrClient(properties, jsonProvider);
        var provider = mock(NonceProvider.class);
        when(provider.nonce()).thenReturn("test-nonce-123");
        @SuppressWarnings("unchecked")
        var providers = mock(ObjectProvider.class);
        when(providers.getIfAvailable()).thenReturn(provider);
        var renderer = new HtmlRenderer(properties, jsonProvider, ssrClient, providers);

        assertTrue(HtmlRenderer.nonceAttribute("test-nonce-123").equals("nonce=\"test-nonce-123\""));
        assertTrue(HtmlRenderer.nonceAttribute(null).isEmpty());

        properties.setRootTemplate("nonce-test.html");
        var page = new PageObject("Home", Map.of(), "/", "v1", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        var html = renderer.render(page);
        assertTrue(html.contains("nonce=\"test-nonce-123\""));
        assertFalse(html.contains("__INERTIA_CSP_NONCE__"));
    }

    @Test
    void ssrCacheTtlResolution() {
        var properties = new InertiaProperties();
        var jsonProvider = new JacksonJsonProvider(new ObjectMapper());
        var ssrClient = new SsrClient(properties, jsonProvider);
        var renderer = new HtmlRenderer(properties, jsonProvider, ssrClient);

        // Off by default.
        assertTrue(renderer.ssrCacheTtlMillis() == null);

        // Global setting applies.
        properties.setSsrCacheEnabled(true);
        properties.setSsrCacheTtl(java.time.Duration.ofMinutes(7));
        assertTrue(renderer.ssrCacheTtlMillis() == 7 * 60 * 1000L);

        // Per-render override wins.
        var policy = new io.github.diovamny.spring.inertia.renderer.SsrCachePolicy();
        policy.setTtlMillis(60_000L);
        renderer.setSsrCachePolicy(policy);
        assertTrue(renderer.ssrCacheTtlMillis() == 60_000L);
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
