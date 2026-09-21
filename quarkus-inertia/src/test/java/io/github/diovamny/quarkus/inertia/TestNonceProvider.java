package io.github.diovamny.quarkus.inertia;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.github.diovamny.inertia.core.spi.NonceProvider;

/**
 * Test CSP nonce source: only answers the nonce-probe route, so every other
 * test keeps rendering byte-identical output without a nonce.
 */
@ApplicationScoped
public class TestNonceProvider implements NonceProvider {

    @Inject
    io.quarkus.vertx.http.runtime.CurrentVertxRequest currentVertxRequest;

    @Override
    public String nonce() {
        try {
            var rc = currentVertxRequest.getCurrent();
            if (rc != null && rc.request().path() != null
                    && rc.request().path().endsWith("/root-view/nonce")) {
                return "test-nonce-123";
            }
        } catch (Exception ignored) {
            // no request in scope
        }
        return null;
    }
}
