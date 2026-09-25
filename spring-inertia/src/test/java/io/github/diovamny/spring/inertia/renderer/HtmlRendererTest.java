package io.github.diovamny.spring.inertia.renderer;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import tools.jackson.databind.ObjectMapper;
import io.github.diovamny.spring.inertia.config.InertiaProperties;
import io.github.diovamny.spring.inertia.internal.JacksonJsonProvider;
import io.github.diovamny.inertia.core.model.PageObject;
import io.github.diovamny.inertia.core.spi.NonceProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HtmlRendererTest {

    @Test
    void rendersV3PureBootstrapWithSinglePayload() {
        var properties = new InertiaProperties();
        var jsonProvider = new JacksonJsonProvider(new ObjectMapper());
        var ssrClient = new SsrClient(properties, jsonProvider);
        var renderer = new HtmlRenderer(properties, jsonProvider, ssrClient);

        var page = new PageObject("Home", Map.of("message", "Hello"), "/", "v1", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        var html = renderer.render(page);

        assertNotNull(html);
        // v3-pure: the div carries no payload.
        assertFalse(html.contains("<div id=\"app\" data-page"));
        // The page object travels exactly once, in the script tag.
        assertTrue(html.contains("type=\"application/json\" data-page=\"app\""));
        assertTrue(html.contains("Home"));
        var occurrences = html.split("data-page=\"app\"", -1).length - 1;
        assertTrue(occurrences == 1, "page payload must appear exactly once, found " + occurrences);
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
    void rootPlaceholderCsrShellMatchesLegacyBytes() {
        var properties = new InertiaProperties();
        var jsonProvider = new JacksonJsonProvider(new ObjectMapper());
        var ssrClient = new SsrClient(properties, jsonProvider);
        properties.setRootTemplate("root-test.html");
        var renderer = new HtmlRenderer(properties, jsonProvider, ssrClient);

        var page = new PageObject("Home", Map.of("message", "Hello"), "/", "v1", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        var html = renderer.render(page);

        var expectedShell = "<div id=\"app\"></div>\n    <script type=\"application/json\" data-page=\"app\">"
            + io.github.diovamny.inertia.core.security.SafeJsonEncoder.encodeForScript(
                jsonProvider.toJson(page))
            + "</script>";
        assertTrue(html.contains(expectedShell), "CSR shell must be byte-identical to the legacy template output");
        assertEquals(1, occurrences(html, "data-page=\"app\""));
        assertEquals(1, occurrences(html, "id=\"app\""));
        assertFalse(html.contains("__INERTIA_"));
    }

    @Test
    void rootPlaceholderSsrSuccessIsProtocolExact() {
        var properties = new InertiaProperties();
        properties.setSsrEnabled(true);
        properties.setRootTemplate("root-test.html");
        var jsonProvider = new JacksonJsonProvider(new ObjectMapper());
        var ssrClient = mock(SsrClient.class);
        var body = "<script data-page=\"app\" type=\"application/json\">{\"component\":\"Welcome\"}</script>"
            + "<div data-server-rendered=\"true\" id=\"app\">Hi</div>";
        when(ssrClient.render(any())).thenReturn(
            java.util.Optional.of(new SsrClient.SsrResult(java.util.List.of(), body)));
        var renderer = new HtmlRenderer(properties, jsonProvider, ssrClient);

        var page = new PageObject("Home", Map.of(), "/", "v1", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        var html = renderer.render(page);

        // Single data-page script, single #app carrying data-server-rendered,
        // body embedded verbatim: every official client hydrates.
        assertEquals(1, occurrences(html, "data-page=\"app\""));
        assertEquals(1, occurrences(html, "id=\"app\""));
        assertTrue(html.contains("<div data-server-rendered=\"true\" id=\"app\">Hi</div>"));
        assertFalse(html.contains("__INERTIA_"));
    }

    @Test
    void legacyTemplateKeepsNestedAssembly() {
        var properties = new InertiaProperties();
        properties.setSsrEnabled(true);
        properties.setRootTemplate("legacy-ssr-test.html");
        var jsonProvider = new JacksonJsonProvider(new ObjectMapper());
        var ssrClient = mock(SsrClient.class);
        var body = "<script data-page=\"app\" type=\"application/json\">{\"component\":\"Welcome\"}</script>"
            + "<div data-server-rendered=\"true\" id=\"app\">Hi</div>";
        when(ssrClient.render(any())).thenReturn(
            java.util.Optional.of(new SsrClient.SsrResult(java.util.List.of(), body)));
        var renderer = new HtmlRenderer(properties, jsonProvider, ssrClient);

        var page = new PageObject("Home", Map.of(), "/", "v1", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        var html = renderer.render(page);

        // Templates without __INERTIA_ROOT__ keep the previous behavior.
        assertTrue(html.contains("<div id=\"app\">" + body + "</div>"));
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

    private static int occurrences(String html, String needle) {
        return html.split(java.util.regex.Pattern.quote(needle), -1).length - 1;
    }
}
