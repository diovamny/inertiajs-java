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
    /**
     * Single root placeholder for protocol-exact SSR assembly. Templates using
     * it get the sidecar body <em>in place of</em> the script tag and root
     * {@code div} on SSR success (one {@code data-page} script, one
     * {@code #app} carrying {@code data-server-rendered}), and the classic
     * CSR shell otherwise. Templates without it keep the legacy assembly
     * (body nested inside {@code #app}).
     */
    public static final String ROOT_PLACEHOLDER = "__INERTIA_ROOT__";
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

        var ssr = resolveSsr(page, rawJson);
        html = html.replace(SSR_HEAD_PLACEHOLDER, ssr.map(SsrClient.SsrResult::headHtml).orElse(""));
        if (html.contains(ROOT_PLACEHOLDER)) {
            html = html.replace(ROOT_PLACEHOLDER, rootHtml(rawJson, ssr.orElse(null)));
        } else {
            html = html.replace(SSR_BODY_PLACEHOLDER, ssr.map(SsrClient.SsrResult::body).orElse(""));
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
     * Resolve the SSR result for a page: empty when SSR is disabled, excluded
     * or the sidecar fails (CSR fallback, counted in metrics).
     *
     * @param page the page object
     * @param rawJson the serialized page JSON (SSR cache key)
     * @return the SSR result, or empty for client-side rendering
     */
    private java.util.Optional<SsrClient.SsrResult> resolveSsr(PageObject page, String rawJson) {
        if (!properties.isSsrEnabled() || isSsrExcluded(page.url())) {
            return java.util.Optional.empty();
        }
        var ttlMillis = ssrCacheTtlMillis();
        var cached = ttlMillis != null ? ssrCache.get(rawJson) : java.util.Optional
            .<io.github.diovamny.inertia.core.ssr.SsrResponseCache.Entry>empty();
        if (cached.isPresent()) {
            var entry = cached.get();
            return java.util.Optional.of(new SsrClient.SsrResult(entry.head(), entry.body()));
        }
        var ssr = ssrClient.render(page).orElse(null);
        if (ssr == null) {
            metrics.recordSsrFallback();
        } else if (ttlMillis != null && ssr.body() != null) {
            ssrCache.put(rawJson, ssr.body(), ssr.head(), ttlMillis,
                java.util.concurrent.TimeUnit.MILLISECONDS);
        }
        return java.util.Optional.ofNullable(ssr);
    }

    /**
     * Protocol-exact root region for {@link #ROOT_PLACEHOLDER} templates (see
     * the protocol SSR section: the sidecar body takes the place of the page
     * object script tag and root {@code div}). On SSR success the body is
     * embedded verbatim (single {@code data-page} script, single {@code #app}
     * with {@code data-server-rendered}, so every official client hydrates
     * instead of re-mounting). Otherwise the classic CSR shell, byte-identical
     * to the legacy template output.
     *
     * @param rawJson the serialized page JSON
     * @param ssr the SSR result, or {@code null} for client-side rendering
     * @return the HTML for the root placeholder
     */
    static String rootHtml(String rawJson, SsrClient.SsrResult ssr) {
        if (ssr != null && ssr.body() != null) {
            return ssr.body();
        }
        return "<div id=\"app\"></div>\n    <script type=\"application/json\" data-page=\"app\">"
            + SafeJsonEncoder.encodeForScript(rawJson) + "</script>";
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
