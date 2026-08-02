package com.quarkus.inertia.renderer;

import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SsrHandler {

    public boolean isSsrEnabled() {
        return false;
    }

    public String render(JsonObject page) {
        throw new UnsupportedOperationException("SSR is not supported yet");
    }
}
