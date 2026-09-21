package io.github.diovamny.quarkus.inertia.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

@ApplicationScoped
public class TestSessionConfig {

    void setupSessionHandler(@Observes Router router, Vertx vertx) {
        var store = LocalSessionStore.create(vertx);
        var sessionHandler = SessionHandler.create(store);
        sessionHandler.setLazySession(true);
        router.route().order(-1000).handler(sessionHandler);
    }
}
