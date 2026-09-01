package io.github.dg.quarkus.inertia.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.enterprise.inject.Instance;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Location;
import io.quarkus.qute.RawString;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import io.github.dg.quarkus.inertia.config.InertiaConfig;
import io.github.dg.quarkus.inertia.model.PageObject;
import io.github.dg.quarkus.inertia.qute.QuteSerializer;
import io.github.dg.quarkus.inertia.util.SafeJsonEncoder;

import io.vertx.core.Vertx;

/**
 * Renders the full HTML document for non-Inertia requests by injecting the
 * serialized page object into the root template.
 * Default implementation uses fast placeholder replacement (no Qute required).
 * Qute support is optional and enabled via {@code inertia.use-qute=true}.
 */
@ApplicationScoped
public class HtmlRenderer {

    private static final Logger LOG = Logger.getLogger(HtmlRenderer.class);

    private static final String DEFAULT_TEMPLATE = "<!DOCTYPE html>\n<html>\n<head>\n    <meta charset=\"utf-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n    <title>__INERTIA_PAGE_TITLE__</title>\n    @vite('resources/js/app.ts')\n    __INERTIA_SSR_HEAD__\n</head>\n<body>\n    <div id=\"app\" data-page=\"__INERTIA_PAGE_JSON__\">__INERTIA_SSR_BODY__</div>\n    <script type=\"application/json\" id=\"inertia-page\">__INERTIA_PAGE__</script>\n</body>\n</html>";

    private static final String DEFAULT_TEMPLATE_PATH = "templates/index.html";

    private final Template defaultRootTemplate;
    private final QuteSerializer serializer;
    private final Instance<Engine> quteEngine;
    private final InertiaConfig config;
    private final SsrHandler ssrHandler;

    private volatile String cachedTemplate;

    @Inject
    public HtmlRenderer(
            @Location("index.html") Template defaultRootTemplate,
            QuteSerializer serializer,
            Instance<Engine> quteEngine,
            InertiaConfig config,
            SsrHandler ssrHandler) {
        this.defaultRootTemplate = defaultRootTemplate;
        this.serializer = serializer;
        this.quteEngine = quteEngine;
        this.config = config;
        this.ssrHandler = ssrHandler;
    }

    public Uni<String> render(PageObject page) {
        return serializer.serialize(page)
            .chain(json -> {
                if (ssrHandler.isSsrEnabled()) {
                    return renderWithSsr(page, json);
                }
                return Uni.createFrom().item(renderTemplate(page, json, null, null));
            });
    }

    public String renderSync(PageObject page, String json) {
        String ssrBody = null;
        String ssrHead = null;
        if (ssrHandler.isSsrEnabled()) {
            try {
                var timeout = config.ssrReadTimeout() != null ? config.ssrReadTimeout() : java.time.Duration.ofSeconds(10);
                var ssr = ssrHandler.render(new io.vertx.core.json.JsonObject(json))
                    .await().atMost(timeout);
                ssrBody = ssr.getString("body");
                ssrHead = ssrHead(ssr);
            } catch (Exception e) {
                LOG.warnf(e, "SSR render failed (sync), falling back to client-side rendering");
            }
        }
        return renderTemplate(page, json, ssrBody, ssrHead);
    }

    private Uni<String> renderWithSsr(PageObject page, String json) {
        return ssrHandler.render(new io.vertx.core.json.JsonObject(json))
            .map(ssr -> renderTemplate(page, json, ssr.getString("body"), ssrHead(ssr)))
            .onFailure()
            .recoverWithItem(failure -> {
                LOG.warnf(failure, "SSR render failed (async), falling back to client-side rendering");
                return renderTemplate(page, json, null, null);
            });
    }

    private String renderTemplate(PageObject page, String json, String ssrBody, String ssrHead) {
        if (useQute()) {
            return renderWithQute(page, json, ssrBody, ssrHead);
        }
        return renderWithPlaceholders(page, json, ssrBody, ssrHead);
    }

    private String renderWithQute(PageObject page, String json, String ssrBody, String ssrHead) {
        var template = resolveRootTemplate();
        var meta = page.meta() == null ? Map.of() : page.meta();
        var viewData = getViewData();
        var quteData = template
            .data("page", page)
            .data("pageMeta", meta)
            .data("pageTitle", meta.get("title"))
            .data("dataPage", new RawString(SafeJsonEncoder.encodeForScript(json)))
            .data("dataPageAttr", new RawString(escapeHtmlAttribute(json)))
            .data("ssrBody", ssrBody != null ? new RawString(ssrBody) : null)
            .data("ssrHead", ssrHead != null ? new RawString(ssrHead) : null);
        if (viewData != null) {
            for (var entry : viewData.entrySet()) {
                quteData = quteData.data(entry.getKey(), entry.getValue());
            }
            quteData = quteData.data("viewData", viewData);
        }
        return quteData.render();
    }

    private String renderWithPlaceholders(PageObject page, String json, String ssrBody, String ssrHead) {
        String template = getTemplateForCurrentRequest();
        String jsonEscaped = escapeHtmlAttribute(json);
        String jsonRaw = SafeJsonEncoder.encodeForScript(json);
        String pageTitle = "Inertia App";
        var meta = page.meta();
        if (meta != null && meta.containsKey("title")) {
            pageTitle = String.valueOf(meta.get("title"));
        }
        pageTitle = escapeHtml(pageTitle);
        String ssrBodySafe = ssrBody != null ? ssrBody : "";
        String ssrHeadSafe = ssrHead != null ? ssrHead : "";
        String rendered = template
            .replace("__INERTIA_PAGE__", jsonRaw)
            .replace("__INERTIA_PAGE_JSON__", jsonEscaped)
            .replace("__INERTIA_SSR_HEAD__", ssrHeadSafe)
            .replace("__INERTIA_SSR_BODY__", ssrBodySafe)
            .replace("__INERTIA_PAGE_TITLE__", pageTitle);

        var viewData = getViewData();
        if (viewData != null && !viewData.isEmpty()) {
            for (var entry : viewData.entrySet()) {
                var placeholder = "__VIEW_" + entry.getKey().toUpperCase() + "__";
                var val = entry.getValue() != null ? escapeHtmlAttribute(String.valueOf(entry.getValue())) : "";
                rendered = rendered.replace(placeholder, val);
            }
        }
        return rendered;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getViewData() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal("inertia-view-data");
            if (val instanceof Map<?, ?> map) {
                return (Map<String, Object>) map;
            }
        }
        return null;
    }

    private String getTemplateForCurrentRequest() {
        var override = rootViewOverride();
        if (override != null && !override.isBlank()) {
            String template = loadTemplate(override);
            if (template != null) return template;
            template = loadTemplate(override + ".html");
            if (template != null) return template;
        }
        var rootView = config.rootView();
        if (rootView.isPresent() && !rootView.get().isBlank()) {
            String template = loadTemplate(rootView.get());
            if (template != null) return template;
            template = loadTemplate(rootView.get() + ".html");
            if (template != null) return template;
        }
        var configured = config.rootTemplate();
        if (configured == null || configured.isBlank() || "index.html".equals(configured)) {
            return getCachedTemplate();
        }
        String template = loadTemplate(configured);
        return template != null ? template : getCachedTemplate();
    }

    private String loadTemplate(String name) {
        // Try multiple locations: templates/, META-INF/resources/, and root
        String[] paths = {
            "templates/" + name,
            "META-INF/resources/" + name,
            name
        };
        for (String path : paths) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                if (is != null) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
            }
        }
        return null;
    }

    private String getCachedTemplate() {
        if (cachedTemplate != null) {
            return cachedTemplate;
        }
        synchronized (this) {
            if (cachedTemplate != null) {
                return cachedTemplate;
            }
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(DEFAULT_TEMPLATE_PATH)) {
                if (is != null) {
                    cachedTemplate = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    return cachedTemplate;
                }
            } catch (IOException e) {
            }
            cachedTemplate = DEFAULT_TEMPLATE.replace("__INERTIA_PAGE_TITLE__", "Inertia App");
            return cachedTemplate;
        }
    }

    private boolean useQute() {
        return config.useQute() && quteEngine.isResolvable();
    }

    private String ssrHead(io.vertx.core.json.JsonObject ssr) {
        var raw = ssr.getValue("head");
        if (raw == null) return null;
        var sb = new StringBuilder();
        if (raw instanceof io.vertx.core.json.JsonArray arr) {
            for (var item : arr) {
                if (item instanceof String s) {
                    sb.append(s).append("\n");
                } else if (item instanceof io.vertx.core.json.JsonArray node) {
                    sb.append(renderHeadNode(node)).append("\n");
                }
            }
        } else if (raw instanceof java.util.List<?> list) {
            for (var item : list) {
                if (item instanceof String s) {
                    sb.append(s).append("\n");
                } else if (item instanceof io.vertx.core.json.JsonArray node) {
                    sb.append(renderHeadNode(node)).append("\n");
                }
            }
        } else if (raw instanceof String s) {
            sb.append(s).append("\n");
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String renderHeadNode(io.vertx.core.json.JsonArray arr) {
        if (arr.isEmpty() || !(arr.getValue(0) instanceof String tag)) return "";
        var sb = new StringBuilder("<").append(tag);
        if (arr.size() > 1 && arr.getValue(1) instanceof Map<?, ?> attrs) {
            for (var entry : ((Map<String, Object>) attrs).entrySet()) {
                var value = entry.getValue();
                if (value != null) {
                    sb.append(' ').append(entry.getKey()).append("=").append("\"").append(value).append("\"");
                }
            }
        }
        if (arr.size() > 2 && arr.getValue(2) != null) {
            sb.append(">").append(arr.getValue(2)).append("</").append(tag).append(">");
        } else {
            sb.append(">");
        }
        return sb.toString();
    }

    private Template resolveRootTemplate() {
        var override = rootViewOverride();
        if (override != null && !override.isBlank()) {
            var template = quteEngine.get().getTemplate(override);
            if (template == null) {
                template = quteEngine.get().getTemplate(override + ".html");
            }
            if (template != null) return template;
        }
        var rootView = config.rootView();
        if (rootView.isPresent() && !rootView.get().isBlank()) {
            var template = quteEngine.get().getTemplate(rootView.get());
            if (template == null) {
                template = quteEngine.get().getTemplate(rootView.get() + ".html");
            }
            if (template != null) return template;
        }
        var configured = config.rootTemplate();
        if (configured == null || configured.isBlank() || "index.html".equals(configured)) {
            return defaultRootTemplate;
        }
        var resolved = quteEngine.get().getTemplate(configured);
        return resolved != null ? resolved : defaultRootTemplate;
    }

    private String rootViewOverride() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            var override = (String) ctx.getLocal("inertia-root-view");
            if (override != null) return override;
        }
        return null;
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&') sb.append("\u0026amp;");
            else if (c == '<') sb.append("\u0026lt;");
            else if (c == '>') sb.append("\u0026gt;");
            else if (c == '"') sb.append("\u0026quot;");
            else if (c == '\'') sb.append("'");
            else sb.append(c);
        }
        return sb.toString();
    }

    private static String escapeHtmlAttribute(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&') sb.append("\u0026amp;");
            else if (c == '"') sb.append("\u0026quot;");
            else if (c == '\'') sb.append("'");
            else if (c == '<') sb.append("\u0026lt;");
            else if (c == '>') sb.append("\u0026gt;");
            else sb.append(c);
        }
        return sb.toString();
    }
}