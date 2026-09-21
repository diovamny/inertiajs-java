package io.github.diovamny.quarkus.inertia.security;

/**
 * Classpath marker for the Quarkus Security integration.
 *
 * <p>Its presence (together with {@code quarkus-rest-csrf}) lets the core
 * adapter resolve {@code inertia.security.mode=auto} to {@code framework} and
 * stand down its own JAX-RS CSRF filter, so there is exactly one CSRF owner
 * per transport.</p>
 */
public final class InertiaQuarkusSecurity {

    private InertiaQuarkusSecurity() {
    }
}
