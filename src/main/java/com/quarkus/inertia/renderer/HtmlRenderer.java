package com.quarkus.inertia.renderer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.qute.RawString;
import io.quarkus.qute.Template;
import io.quarkus.qute.Location;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.qute.QuteSerializer;

@ApplicationScoped
public class HtmlRenderer {

    private final Template rootTemplate;
    private final QuteSerializer serializer;

    @Inject
    public HtmlRenderer(
            @Location("index.html") Template rootTemplate,
            com.quarkus.inertia.qute.QuteSerializer serializer) {
        this.rootTemplate = rootTemplate;
        this.serializer = serializer;
    }

    public Uni<String> render(PageObject page) {
        return serializer.serialize(page)
            .chain(json -> Uni.createFrom().item(
                rootTemplate
                    .data("page", page)
                    .data("dataPage", new RawString(json))
                    .render()
            ));
    }
}
