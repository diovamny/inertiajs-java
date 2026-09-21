package io.github.diovamny.inertia.core.ssr;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aggregated SSR sidecar health for Actuator / SmallRye Health endpoints.
 * Adapters expose this through their health integrations; the computation
 * itself is framework-agnostic.
 */
public final class SsrHealth {

    /**
     * Overall status.
     */
    public enum Status {
        UP, DOWN, UNKNOWN
    }

    /**
     * A point-in-time health snapshot.
     *
     * @param status  the overall status
     * @param details diagnostic details (breaker state, supervisor state,
     *                last probe outcome)
     */
    public record Snapshot(Status status, Map<String, Object> details) {
    }

    private SsrHealth() {
    }

    /**
     * Compute health from the breaker, the supervisor and whether SSR is
     * enabled at all.
     *
     * @param ssrEnabled whether SSR rendering is enabled
     * @param breaker    the sidecar circuit breaker (may be {@code null})
     * @param supervisor the sidecar supervisor (may be {@code null})
     * @return the snapshot ({@code UNKNOWN} when SSR is disabled)
     */
    public static Snapshot check(boolean ssrEnabled, SsrCircuitBreaker breaker,
            NodeSupervisor supervisor) {
        var details = new LinkedHashMap<String, Object>();
        if (!ssrEnabled) {
            details.put("ssr", "disabled");
            return new Snapshot(Status.UNKNOWN, details);
        }
        var breakerState = breaker != null ? breaker.state().name() : "absent";
        details.put("breaker", breakerState);
        var supervised = supervisor != null;
        details.put("supervised", supervised);
        if (supervised) {
            details.put("processAlive", supervisor.isRunning());
            details.put("attempts", supervisor.attempts());
        }
        if (breaker != null && breaker.state() == SsrCircuitBreaker.State.OPEN) {
            return new Snapshot(Status.DOWN, details);
        }
        if (supervised && supervisor != null && !supervisor.isRunning()
                && supervisor.attempts() > 0) {
            return new Snapshot(Status.DOWN, details);
        }
        return new Snapshot(Status.UP, details);
    }
}
