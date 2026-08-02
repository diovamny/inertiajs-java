package com.quarkus.inertia.qute;

import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.spi.JsonProvider;

@ApplicationScoped
public class QuteSerializer {

    private final JsonProvider jsonProvider;

    @Inject
    public QuteSerializer(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
    }

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
