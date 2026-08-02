package com.quarkus.inertia.renderer;

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

    @Inject
    public HtmlRenderer(
            @Location("index.html") Template defaultRootTemplate,
            QuteSerializer serializer,
            Engine engine,
            InertiaConfig config) {
        this.defaultRootTemplate = defaultRootTemplate;
        this.serializer = serializer;
        this.engine = engine;
        this.config = config;
    }

    public Uni<String> render(PageObject page) {
        return serializer.serialize(page)
            .chain(json -> Uni.createFrom().item(() -> {
                var template = resolveRootTemplate();
                var meta = page.meta() == null ? Map.of() : page.meta();
                return template
                    .data("page", page)
                    .data("pageMeta", meta)
                    .data("pageTitle", meta.get("title"))
                    .data("dataPage", new RawString(json))
                    .render();
            }));
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