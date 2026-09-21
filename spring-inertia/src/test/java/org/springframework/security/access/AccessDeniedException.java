package org.springframework.security.access;

/**
 * Minimal test stub mirroring Spring Security's
 * {@code AccessDeniedException} hierarchy (class-name based detection only).
 * Present so {@code InertiaValidationHandler} can be tested with security
 * semantics without depending on Spring Security in this module.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
