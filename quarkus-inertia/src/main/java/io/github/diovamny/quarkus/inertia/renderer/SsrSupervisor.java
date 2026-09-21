package io.github.diovamny.quarkus.inertia.renderer;

import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import io.github.diovamny.inertia.core.ssr.NodeSupervisor;
import io.github.diovamny.quarkus.inertia.config.InertiaConfig;

/**
 * Supervises the Node.js SSR sidecar when
 * {@code inertia.ssr-supervisor-enabled=true}: starts it on boot, restarts
 * unexpected failures with exponential backoff, stops it on shutdown.
 * Disabled by default (no process is ever spawned unless opted in).
 */
@ApplicationScoped
public class SsrSupervisor {

    @Inject
    InertiaConfig config;

    private volatile NodeSupervisor supervisor;

    void onStart(@Observes StartupEvent event) {
        if (!config.ssrSupervisorEnabled()) {
            return;
        }
        supervisor = new NodeSupervisor(new NodeSupervisor.Config(
            config.ssrSupervisorCommand(),
            config.ssrSupervisorEntry(),
            config.ssrSupervisorWorkdir().orElse(null),
            Math.max(0, config.ssrSupervisorMaxRestarts()),
            1, TimeUnit.SECONDS));
        supervisor.start();
    }

    void onStop(@Observes ShutdownEvent event) {
        var active = supervisor;
        if (active != null) {
            active.stop();
        }
    }

    /**
     * The active supervisor, or {@code null} when supervision is disabled.
     */
    public NodeSupervisor supervisor() {
        return supervisor;
    }
}
