package io.github.diovamny.spring.inertia.ssr;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.SmartLifecycle;

import io.github.diovamny.inertia.core.ssr.NodeSupervisor;
import io.github.diovamny.spring.inertia.config.InertiaProperties;

/**
 * Supervises the Node.js SSR sidecar as a {@link SmartLifecycle}: starts it
 * when the context refreshes, restarts unexpected failures with exponential
 * backoff, stops it on shutdown. Only registered when
 * {@code inertia.ssr-supervisor-enabled=true} (disabled by default: no
 * process is ever spawned unless opted in).
 */
public class SsrSupervisorLifecycle implements SmartLifecycle {

    private final InertiaProperties properties;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile NodeSupervisor supervisor;

    public SsrSupervisorLifecycle(InertiaProperties properties) {
        this.properties = properties;
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            supervisor = new NodeSupervisor(new NodeSupervisor.Config(
                properties.getSsrSupervisorCommand(),
                properties.getSsrSupervisorEntry(),
                properties.getSsrSupervisorWorkdir(),
                Math.max(0, properties.getSsrSupervisorMaxRestarts()),
                1, TimeUnit.SECONDS));
            supervisor.start();
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            var active = supervisor;
            if (active != null) {
                active.stop();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * The active supervisor, or {@code null} before start.
     */
    public NodeSupervisor supervisor() {
        return supervisor;
    }
}
