package io.github.diovamny.spring.inertia.ssr;

import org.springframework.beans.factory.ObjectProvider;

import io.github.diovamny.inertia.core.ssr.NodeSupervisor;
import io.github.diovamny.inertia.core.ssr.SsrHealth;
import io.github.diovamny.spring.inertia.config.InertiaProperties;
import io.github.diovamny.spring.inertia.renderer.SsrClient;

/**
 * Aggregated SSR sidecar health. Wire the {@link #snapshot()} into an
 * Actuator {@code HealthIndicator} in three lines when
 * {@code spring-boot-starter-actuator} is present:
 *
 * <pre>{@code
 * &#64;Component
 * public class InertiaSsrHealthIndicator implements HealthIndicator {
 *     private final SsrHealthBean ssr;
 *     public InertiaSsrHealthIndicator(SsrHealthBean ssr) { this.ssr = ssr; }
 *     &#64;Override
 *     public Health health() {
 *         var snapshot = ssr.snapshot();
 *         var builder = snapshot.status() == SsrHealth.Status.DOWN
 *             ? Health.down() : Health.up();
 *         snapshot.details().forEach(builder::withDetail);
 *         return builder.build();
 *     }
 * }
 * }</pre>
 */
public class SsrHealthBean {

    private final InertiaProperties properties;
    private final SsrClient ssrClient;
    private final ObjectProvider<SsrSupervisorLifecycle> supervisorLifecycle;

    public SsrHealthBean(InertiaProperties properties, SsrClient ssrClient,
            ObjectProvider<SsrSupervisorLifecycle> supervisorLifecycle) {
        this.properties = properties;
        this.ssrClient = ssrClient;
        this.supervisorLifecycle = supervisorLifecycle;
    }

    /**
     * Current SSR health snapshot.
     */
    public SsrHealth.Snapshot snapshot() {
        var lifecycle = supervisorLifecycle != null ? supervisorLifecycle.getIfAvailable() : null;
        NodeSupervisor supervised = lifecycle != null ? lifecycle.supervisor() : null;
        return SsrHealth.check(properties.isSsrEnabled(), ssrClient.circuitBreaker(), supervised);
    }
}
