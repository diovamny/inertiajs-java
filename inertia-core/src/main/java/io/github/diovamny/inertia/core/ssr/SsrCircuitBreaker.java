package io.github.diovamny.inertia.core.ssr;

/**
 * Lightweight circuit breaker for the SSR sidecar (Rails Puma-plugin
 * inspiration, SmallRye/Resilience4j semantics without the dependency).
 *
 * <p>After {@code failureThreshold} consecutive failures the breaker opens:
 * callers must fail fast (the adapters fall back to client-side rendering
 * immediately instead of paying another sidecar timeout). Once
 * {@code openCooldown} elapses the next call becomes a half-open probe: one
 * success closes the breaker, another failure re-opens it.</p>
 *
 * <p>Thread-safe.</p>
 */
public class SsrCircuitBreaker {

    /**
     * Breaker state for health reporting.
     */
    public enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    private final int failureThreshold;
    private final long openCooldownMillis;

    private int consecutiveFailures;
    private long openedAtMillis;
    private boolean halfOpenProbe;

    /**
     * @param failureThreshold consecutive failures before opening (at least 1)
     * @param openCooldown     cooldown before a half-open probe is allowed
     * @param openCooldownUnit unit of the cooldown
     */
    public SsrCircuitBreaker(int failureThreshold, long openCooldown,
            java.util.concurrent.TimeUnit openCooldownUnit) {
        if (failureThreshold < 1) {
            throw new IllegalArgumentException("failureThreshold must be at least 1");
        }
        this.failureThreshold = failureThreshold;
        this.openCooldownMillis = openCooldownUnit.toMillis(Math.max(0, openCooldown));
    }

    /**
     * Whether a sidecar call may proceed. Returns {@code false} while open
     * (caller falls back immediately); the first call after the cooldown is
     * the half-open probe.
     */
    public synchronized boolean allowRequest() {
        if (consecutiveFailures < failureThreshold) {
            return true;
        }
        if (now() - openedAtMillis >= openCooldownMillis) {
            halfOpenProbe = true;
            return true;
        }
        return false;
    }

    /**
     * Whether the current call is the half-open probe.
     */
    public synchronized boolean isHalfOpenProbe() {
        return halfOpenProbe;
    }

    /**
     * Record a sidecar success (closes the breaker).
     */
    public synchronized void recordSuccess() {
        consecutiveFailures = 0;
        halfOpenProbe = false;
    }

    /**
     * Record a sidecar failure (opens the breaker at the threshold).
     */
    public synchronized void recordFailure() {
        consecutiveFailures++;
        halfOpenProbe = false;
        if (consecutiveFailures >= failureThreshold) {
            openedAtMillis = now();
        }
    }

    /**
     * Current state for health reporting.
     */
    public synchronized State state() {
        if (consecutiveFailures < failureThreshold) {
            return State.CLOSED;
        }
        if (now() - openedAtMillis >= openCooldownMillis) {
            return State.HALF_OPEN;
        }
        return State.OPEN;
    }

    long now() {
        return System.currentTimeMillis();
    }
}
