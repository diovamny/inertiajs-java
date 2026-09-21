package com.example.kitchensink.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

@ApplicationScoped
public class SessionConfig {

    void setupSessionHandler(@Observes Router router, Vertx vertx) {
        var store = LocalSessionStore.create(vertx);
        var sessionHandler = SessionHandler.create(store);
        sessionHandler.setLazySession(false);
        // Before authentication: the session login mechanism reads the
        // session during Quarkus HTTP-policy enforcement, which runs before
        // user routes (including the adapter pre-handler at order -1).
        router.route().order(-1000).handler(sessionHandler);
    }
}
