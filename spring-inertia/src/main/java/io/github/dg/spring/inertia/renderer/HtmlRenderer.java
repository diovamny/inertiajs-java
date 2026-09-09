package io.github.dg.spring.inertia.renderer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;

import io.github.dg.spring.inertia.config.InertiaProperties;
import io.github.dg.spring.inertia.model.PageObject;
import io.github.dg.spring.inertia.spi.JsonProvider;
import io.github.dg.spring.inertia.util.SafeJsonEncoder;

/**
 * Renders the full HTML page for non-Inertia visits.
 *
 * <p>The root template ({@code inertia.root-template}, default
 * {@code templates/index.html}) is resolved from the classpath and must
 * contain the placeholder {@code __INERTIA_PAGE__} inside the
 * {@code data-page} attribute of the app element, plus the placeholder
 * {@code __INERTIA_PAGE_JSON__} inside a {@code data-page} script tag that
 * the Inertia v3 client reads on boot:</p>
 *
 * <pre>{@code
 * <div id="app" data-page="__INERTIA_PAGE__"></div>
 * <script type="application/json" data-page="app">__INERTIA_PAGE_JSON__</script>
 * }</pre>
 *
 * <p>The attribute value is injected HTML-escaped while the script tag
 * receives the raw JSON. When SSR is enabled and the visit matches, the
 * optional placeholders {@code __INERTIA_SSR_HEAD__} and
 * {@code __INERTIA_SSR_BODY__} are replaced with the SSR payload.</p>
 *
 * <p>Custom view-only data passed via {@code inertia.viewData(key, val)} is
 * injected into matching {@code __VIEW_<KEY_UPPERCASE>__} placeholders.</p>
 */
public class HtmlRenderer {

    public static final String PAGE_PLACEHOLDER = "__INERTIA_PAGE__";
    public static final String PAGE_JSON_PLACEHOLDER = "__INERTIA_PAGE_JSON__";
    public static final String SSR_HEAD_PLACEHOLDER = "__INERTIA_SSR_HEAD__";
    public static final String SSR_BODY_PLACEHOLDER = "__INERTIA_SSR_BODY__";
    public static final String VIEW_DATA_PLACEHOLDER_PREFIX = "__VIEW_";

    private final InertiaProperties properties;
    private final JsonProvider jsonProvider;
    private final SsrClient ssrClient;

    private volatile String cachedTemplate;

    public HtmlRenderer(InertiaProperties properties, JsonProvider jsonProvider, SsrClient ssrClient) {
        this.properties = properties;
        this.jsonProvider = jsonProvider;
        this.ssrClient = ssrClient;
    }

    /**
     * Render the HTML document for a page object.
     *
     * @param page the page object
     * @return the complete HTML document
     */
    public String render(PageObject page) {
        return render(page, null);
    }

    /**
     * Render the HTML document for a page object and optional view data.
     *
     * @param page     the page object
     * @param viewData view-only data for root template placeholders
     * @return the complete HTML document
     */
    public String render(PageObject page, Map<String, Object> viewData) {
        var template = loadTemplate();
        var rawJson = jsonProvider.toJson(page);
        var escapedJson = escapeForHtmlAttribute(rawJson);

        String html = template.replace(PAGE_PLACEHOLDER, escapedJson);

        if (properties.isSsrEnabled() && !isSsrExcluded(page.url())) {
            var ssr = ssrClient.render(page);
            html = html.replace(SSR_HEAD_PLACEHOLDER, ssr.map(SsrClient.SsrResult::headHtml).orElse(""));
            html = html.replace(SSR_BODY_PLACEHOLDER, ssr.map(SsrClient.SsrResult::body).orElse(""));
        } else {
            html = html.replace(SSR_HEAD_PLACEHOLDER, "");
            html = html.replace(SSR_BODY_PLACEHOLDER, "");
        }

        html = html.replace(PAGE_JSON_PLACEHOLDER, SafeJsonEncoder.encodeForScript(rawJson));

        if (viewData != null && !viewData.isEmpty()) {
            for (var entry : viewData.entrySet()) {
                var placeholder = VIEW_DATA_PLACEHOLDER_PREFIX + entry.getKey().toUpperCase() + "__";
                var val = entry.getValue() != null ? escapeForHtmlAttribute(String.valueOf(entry.getValue())) : "";
                html = html.replace(placeholder, val);
            }
        }

        return html;
    }

    /**
     * Whether the given path is excluded from SSR.
     *
     * @param url the request URL
     * @return {@code true} when the path matches an excluded prefix
     */
    public boolean isSsrExcluded(String url) {
        for (var path : properties.getSsrExcludePaths()) {
            if (url != null && url.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clear the in-memory cached template (useful in tests or when reload is triggered).
     */
    public void clearTemplateCache() {
        this.cachedTemplate = null;
    }

    private String loadTemplate() {
        if (properties.isTemplateCacheEnabled() && cachedTemplate != null) {
            return cachedTemplate;
        }
        synchronized (this) {
            if (properties.isTemplateCacheEnabled() && cachedTemplate != null) {
                return cachedTemplate;
            }
            var location = "templates/" + properties.getRootTemplate();
            try (var in = new ClassPathResource(location).getInputStream()) {
                var content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                if (properties.isTemplateCacheEnabled()) {
                    cachedTemplate = content;
                }
                return content;
            } catch (IOException e) {
                throw new UncheckedIOException(
                    "Inertia root template not found on the classpath: " + location, e);
            }
        }
    }

    private static String escapeForHtmlAttribute(String json) {
        return json
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }
}
