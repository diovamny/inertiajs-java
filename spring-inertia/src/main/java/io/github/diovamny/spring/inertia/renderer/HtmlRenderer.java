package io.github.diovamny.spring.inertia.renderer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;

import org.springframework.beans.factory.ObjectProvider;

import io.github.diovamny.spring.inertia.config.InertiaProperties;
import io.github.diovamny.inertia.core.model.PageObject;
import io.github.diovamny.inertia.core.spi.JsonProvider;
import io.github.diovamny.inertia.core.spi.NonceProvider;
import io.github.diovamny.inertia.core.security.SafeJsonEncoder;

/**
 * Renders the full HTML page for non-Inertia visits (Inertia v3 pure bootstrap).
 *
 * <p>The root template ({@code inertia.root-template}, default
 * {@code templates/index.html}) is resolved from the classpath and must
 * contain the placeholder {@code __INERTIA_PAGE_JSON__} inside a
 * {@code data-page} script tag that the Inertia v3 client reads on boot.
 * The app element carries no payload:</p>
 *
 * <pre>{@code
 * <div id="app">__INERTIA_SSR_BODY__</div>
 * <script type="application/json" data-page="app">__INERTIA_PAGE_JSON__</script>
 * }</pre>
 *
 * <p>The page object travels exactly once (script tag, script-safe JSON).
 * The legacy {@code __INERTIA_PAGE__} placeholder (v1/v2 {@code data-page}
 * attribute) is no longer supported: if still present it is replaced with an
 * empty string so no payload leaks into markup. When SSR is enabled and the
 * visit matches, the optional placeholders {@code __INERTIA_SSR_HEAD__} and
 * {@code __INERTIA_SSR_BODY__} are replaced with the SSR payload.</p>
 *
 * <p>Custom view-only data passed via {@code inertia.viewData(key, val)} is
 * injected into matching {@code __VIEW_<KEY_UPPERCASE>__} placeholders.</p>
 */
public class HtmlRenderer {

    /**
     * Legacy v1/v2 placeholder for the {@code data-page} attribute.
     * Kept as a constant so custom templates referencing it fail visibly
     * (replaced with an empty string) instead of leaking placeholders.
     * New templates must use {@link #PAGE_JSON_PLACEHOLDER} only.
     *
     * @deprecated since 0.0.4: v3-pure bootstrap carries no {@code data-page} attribute.
     */
    @Deprecated(since = "0.0.4", forRemoval = false)
    public static final String PAGE_PLACEHOLDER = "__INERTIA_PAGE__";
    public static final String PAGE_JSON_PLACEHOLDER = "__INERTIA_PAGE_JSON__";
    public static final String SSR_HEAD_PLACEHOLDER = "__INERTIA_SSR_HEAD__";
    public static final String SSR_BODY_PLACEHOLDER = "__INERTIA_SSR_BODY__";
    public static final String VIEW_DATA_PLACEHOLDER_PREFIX = "__VIEW_";
    public static final String CSP_NONCE_PLACEHOLDER = "__INERTIA_CSP_NONCE__";

    private final InertiaProperties properties;
    private final JsonProvider jsonProvider;
    private final SsrClient ssrClient;
    private final ObjectProvider<NonceProvider> nonceProviders;
    private io.github.diovamny.spring.inertia.metrics.InertiaMetrics metrics
        = io.github.diovamny.spring.inertia.metrics.InertiaMetrics.noop();
    private SsrCachePolicy ssrCachePolicy;

    /**
     * Attach the metrics recorder (called by auto-configuration; defaults to
     * a no-op so plain unit tests stay silent).
     */
    public void setMetrics(io.github.diovamny.spring.inertia.metrics.InertiaMetrics metrics) {
        if (metrics != null) {
            this.metrics = metrics;
        }
    }

    /**
     * Attach the per-request SSR cache policy (called by auto-configuration;
     * may stay {@code null} in plain unit tests, disabling per-render TTLs).
     */
    public void setSsrCachePolicy(SsrCachePolicy ssrCachePolicy) {
        this.ssrCachePolicy = ssrCachePolicy;
    }

    private final io.github.diovamny.inertia.core.ssr.SsrResponseCache ssrCache =
        new io.github.diovamny.inertia.core.ssr.SsrResponseCache(200);

    /**
     * Effective SSR cache TTL in millis: per-render override first, then the
     * global setting when caching is enabled, otherwise {@code null} (off).
     */
    Long ssrCacheTtlMillis() {
        if (ssrCachePolicy != null && ssrCachePolicy.ttlMillis() != null) {
            return ssrCachePolicy.ttlMillis();
        }
        if (properties.isSsrCacheEnabled()) {
            var ttl = properties.getSsrCacheTtl();
            return ttl != null ? ttl.toMillis() : null;
        }
        return null;
    }

    private volatile String cachedTemplate;

    public HtmlRenderer(InertiaProperties properties, JsonProvider jsonProvider, SsrClient ssrClient) {
        this(properties, jsonProvider, ssrClient, null);
    }

    public HtmlRenderer(InertiaProperties properties, JsonProvider jsonProvider, SsrClient ssrClient,
            ObjectProvider<NonceProvider> nonceProviders) {
        this.properties = properties;
        this.jsonProvider = jsonProvider;
        this.ssrClient = ssrClient;
        this.nonceProviders = nonceProviders;
    }

    /**
     * The CSP nonce for the current render, or {@code null} when no
     * {@link NonceProvider} is registered or none applies.
     */
    String currentNonce() {
        if (nonceProviders == null) {
            return null;
        }
        var provider = nonceProviders.getIfAvailable();
        if (provider == null) {
            return null;
        }
        var nonce = provider.nonce();
        return nonce != null && !nonce.isBlank() ? nonce.trim() : null;
    }

    static String nonceAttribute(String nonce) {
        if (nonce == null || nonce.isBlank()) {
            return "";
        }
        return "nonce=\"" + nonce.replace("\"", "&quot;") + "\"";
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
        var rawJson = io.github.diovamny.inertia.core.security.PageSizeGuard.check(
            jsonProvider.toJson(page), properties.getMaxPageBytes());

        // v3-pure: the page object lives only in the script tag. A legacy
        // __INERTIA_PAGE__ placeholder (v1/v2 data-page attribute) is blanked
        // so no payload is duplicated into markup.
        String html = template.replace(PAGE_PLACEHOLDER, "");

        if (properties.isSsrEnabled() && !isSsrExcluded(page.url())) {
            var ttlMillis = ssrCacheTtlMillis();
            var cached = ttlMillis != null ? ssrCache.get(rawJson) : java.util.Optional
                .<io.github.diovamny.inertia.core.ssr.SsrResponseCache.Entry>empty();
            SsrClient.SsrResult ssr;
            if (cached.isPresent()) {
                var entry = cached.get();
                ssr = new SsrClient.SsrResult(entry.head(), entry.body());
            } else {
                ssr = ssrClient.render(page).orElse(null);
                if (ssr == null) {
                    metrics.recordSsrFallback();
                } else if (ttlMillis != null && ssr.body() != null) {
                    ssrCache.put(rawJson, ssr.body(), ssr.head(), ttlMillis,
                        java.util.concurrent.TimeUnit.MILLISECONDS);
                }
            }
            var result = java.util.Optional.ofNullable(ssr);
            html = html.replace(SSR_HEAD_PLACEHOLDER, result.map(SsrClient.SsrResult::headHtml).orElse(""));
            html = html.replace(SSR_BODY_PLACEHOLDER, result.map(SsrClient.SsrResult::body).orElse(""));
        } else {
            html = html.replace(SSR_HEAD_PLACEHOLDER, "");
            html = html.replace(SSR_BODY_PLACEHOLDER, "");
        }

        html = html.replace(PAGE_JSON_PLACEHOLDER, SafeJsonEncoder.encodeForScript(rawJson));
        html = html.replace(CSP_NONCE_PLACEHOLDER, nonceAttribute(currentNonce()));

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
