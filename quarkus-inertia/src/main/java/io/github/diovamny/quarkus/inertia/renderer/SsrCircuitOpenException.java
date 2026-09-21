package io.github.diovamny.quarkus.inertia.renderer;

/**
 * The SSR circuit breaker is open: callers must fall back to client-side
 * rendering immediately instead of paying another sidecar timeout.
 */
public class SsrCircuitOpenException extends IllegalStateException {

    public SsrCircuitOpenException() {
        super("SSR circuit breaker is OPEN: falling back to client-side rendering");
    }
}
