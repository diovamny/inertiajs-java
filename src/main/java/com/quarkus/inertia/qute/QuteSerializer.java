package com.quarkus.inertia.qute;

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
        return Uni.createFrom().item(() -> jsonProvider.toJson(page));
    }
}
