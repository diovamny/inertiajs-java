package io.github.diovamny.quarkus.inertia.session;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;

/**
 * Auto-provisions a Vert.x {@link SessionHandler} so flash messages and
 * session-based CSRF storage work without manual wiring. The handler is
 * registered at order {@code -2}, strictly before {@code InertiaVertxHandler}
 * (order {@code -1}), whose CSRF pre-check reads the session.
 *
 * <p>Controlled by {@code inertia.session.enabled} (default {@code true}).
 * Set it to {@code false} when the application registers its own
 * {@code SessionHandler} or does not need flash/session CSRF.</p>
 */
@ApplicationScoped
public class InertiaSessionProducer {

    @Inject
    InertiaConfig config;

    @Inject
    Vertx vertx;

    void setup(@Observes Router router) {
        if (!config.sessionEnabled()) {
            return;
        }
        var store = LocalSessionStore.create(vertx);
        router.route().order(-2)
              .handler(SessionHandler.create(store));
    }
}
