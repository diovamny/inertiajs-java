package com.quarkus.inertia.qute;

import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.spi.JsonProvider;

/**
 * Serializes a {@link PageObject} to its JSON string for embedding in the
 * root template or serving directly; unwraps {@code RawJson} values so raw
 * documents are embedded as-is.
 */
@ApplicationScoped
public class QuteSerializer {

    private final JsonProvider jsonProvider;

    @Inject
    public QuteSerializer(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
    }

    /**
     * Serialize the page object to JSON, embedding {@code RawJson} values
     * as-is.
     *
     * @param page the page object
     * @return the JSON document as a Uni
     */
    public Uni<String> serialize(PageObject page) {
        return Uni.createFrom().item(() -> {
            if (!com.quarkus.inertia.internal.RawJsonUnwrapper.containsRawJson(page.props())) {
                return jsonProvider.toJson(page);
            }
            var unwrapped = com.quarkus.inertia.internal.RawJsonUnwrapper.unwrap(page.props());
            @SuppressWarnings("unchecked")
            var props = (Map<String, Object>) unwrapped;
            return jsonProvider.toJson(page.withProps(props));
        });
    }
}
