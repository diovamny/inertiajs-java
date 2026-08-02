package com.quarkus.inertia.renderer;

import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Location;
import io.quarkus.qute.RawString;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.config.InertiaConfig;
import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.qute.QuteSerializer;

@ApplicationScoped
public class HtmlRenderer {

    private final Template defaultRootTemplate;
    private final QuteSerializer serializer;
    private final Engine engine;
    private final InertiaConfig config;
    private final SsrHandler ssrHandler;

    @Inject
    public HtmlRenderer(
            @Location("index.html") Template defaultRootTemplate,
            QuteSerializer serializer,
            Engine engine,
            InertiaConfig config,
            SsrHandler ssrHandler) {
        this.defaultRootTemplate = defaultRootTemplate;
        this.serializer = serializer;
        this.engine = engine;
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

    private Uni<String> renderWithSsr(PageObject page, String json) {
        return ssrHandler.render(new io.vertx.core.json.JsonObject(json))
            .map(ssr -> renderTemplate(page, json, ssr.getString("body"), ssrHead(ssr)))
            .onFailure()
            .recoverWithItem(renderTemplate(page, json, null, null));
    }

    private String renderTemplate(PageObject page, String json, String ssrBody, String ssrHead) {
        var template = resolveRootTemplate();
        var meta = page.meta() == null ? Map.of() : page.meta();
        return template
            .data("page", page)
            .data("pageMeta", meta)
            .data("pageTitle", meta.get("title"))
            .data("dataPage", new RawString(json))
            .data("ssrBody", ssrBody != null ? new RawString(ssrBody) : null)
            .data("ssrHead", ssrHead != null ? new RawString(ssrHead) : null)
            .render();
    }

    private String ssrHead(io.vertx.core.json.JsonObject ssr) {
        var raw = ssr.getValue("head");
        if (raw == null) return null;
        var sb = new StringBuilder();
        if (raw instanceof List<?> list) {
            for (var item : list) {
                if (item instanceof String s) {
                    sb.append(s).append("\n");
                } else if (item instanceof io.vertx.core.json.JsonArray arr) {
                    sb.append(renderHeadNode(arr)).append("\n");
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
                    sb.append(' ').append(entry.getKey()).append("=\"").append(value).append("\"");
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
            var template = engine.getTemplate(override);
            if (template == null) {
                template = engine.getTemplate(override + ".html");
            }
            if (template != null) return template;
        }

        var configured = config.rootTemplate();
        if (configured == null || configured.isBlank() || "index.html".equals(configured)) {
            return defaultRootTemplate;
        }

        var resolved = engine.getTemplate(configured);
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
}