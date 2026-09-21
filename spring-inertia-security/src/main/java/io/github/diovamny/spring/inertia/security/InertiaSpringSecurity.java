package io.github.diovamny.spring.inertia.security;

/**
 * Classpath marker for the Spring Security integration.
 *
 * <p>Its presence (together with Spring Security itself) lets the core
 * adapter resolve {@code inertia.security.mode=auto} to {@code framework} and
 * skip its own CSRF filter, so there is exactly one CSRF owner.</p>
 */
public final class InertiaSpringSecurity {

    private InertiaSpringSecurity() {
    }
}
