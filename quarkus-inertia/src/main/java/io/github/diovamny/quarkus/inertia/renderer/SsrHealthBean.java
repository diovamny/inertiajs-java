package io.github.diovamny.quarkus.inertia.renderer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.github.diovamny.inertia.core.ssr.SsrHealth;
import io.github.diovamny.quarkus.inertia.config.InertiaConfig;

/**
 * Aggregated SSR sidecar health. Wire the {@link #snapshot()} into a
 * SmallRye {@code HealthCheck} in three lines when
 * {@code quarkus-smallrye-health} is present:
 *
 * <pre>{@code
 * &#64;Readiness
 * &#64;ApplicationScoped
 * public class InertiaSsrHealthCheck implements HealthCheck {
 *     &#64;Inject SsrHealthBean ssr;
 *     &#64;Override
 *     public HealthCheckResponse call() {
 *         var snapshot = ssr.snapshot();
 *         return HealthCheckResponse.named("inertia-ssr")
 *             .status(snapshot.status() != SsrHealth.Status.DOWN)
 *             .withData(snapshot.details()).build();
 *     }
 * }
 * }</pre>
 */
@ApplicationScoped
public class SsrHealthBean {

    @Inject
    InertiaConfig config;

    @Inject
    SsrHandler ssrHandler;

    @Inject
    SsrSupervisor supervisor;

    /**
     * Current SSR health snapshot.
     */
    public SsrHealth.Snapshot snapshot() {
        var supervised = supervisor.supervisor();
        return SsrHealth.check(config.ssrEnabled(), ssrHandler.circuitBreaker(), supervised);
    }
}
