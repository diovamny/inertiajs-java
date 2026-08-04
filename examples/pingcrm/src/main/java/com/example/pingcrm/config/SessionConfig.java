package com.example.pingcrm.config;

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
        router.route().order(0).handler(sessionHandler);
    }
}
