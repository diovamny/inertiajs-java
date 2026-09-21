package io.github.diovamny.spring.inertia.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Micrometer instrumentation for the Inertia pipeline, all prefixed with
 * {@code inertia.}:
 * <ul>
 *   <li>{@code inertia.render.requests} (counter, tags {@code component},
 *   {@code type} = {@code html}/{@code json});</li>
 *   <li>{@code inertia.render.duration} (timer, same tags);</li>
 *   <li>{@code inertia.ssr.requests}, {@code inertia.ssr.failures},
 *   {@code inertia.ssr.fallback} (counters);</li>
 *   <li>{@code inertia.props.resolution.duration} (timer, tag
 *   {@code component}).</li>
 * </ul>
 *
 * <p>The registry is optional: without a {@code MeterRegistry} bean (e.g.
 * no Spring Boot Actuator) every method is a no-op, so applications that do
 * not use Micrometer pay nothing. Use {@link #noop()} in plain unit tests.
 * </p>
 */
public class InertiaMetrics {

    private final MeterRegistry registry;

    public InertiaMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * No-op recorder (unit tests, direct construction).
     */
    public static InertiaMetrics noop() {
        return new InertiaMetrics(null);
    }

    /**
     * Start timing a render; pass the sample to
     * {@link #stopRender(Timer.Sample, String, String)}.
     */
    public Timer.Sample startRender() {
        return registry != null ? Timer.start(registry) : null;
    }

    /**
     * Record a finished render.
     */
    public void stopRender(Timer.Sample sample, String component, String type) {
        if (registry == null || sample == null) {
            return;
        }
        var tags = new String[]{"component", component != null ? component : "unknown",
            "type", type != null ? type : "unknown"};
        sample.stop(Timer.builder("inertia.render.duration").tags(tags).register(registry));
        Counter.builder("inertia.render.requests").tags(tags).register(registry).increment();
    }

    /**
     * Start timing prop resolution; pass the sample to
     * {@link #stopPropsResolution(Timer.Sample, String)}.
     */
    public Timer.Sample startPropsResolution() {
        return registry != null ? Timer.start(registry) : null;
    }

    /**
     * Record finished prop resolution.
     */
    public void stopPropsResolution(Timer.Sample sample, String component) {
        if (registry == null || sample == null) {
            return;
        }
        sample.stop(Timer.builder("inertia.props.resolution.duration")
            .tags("component", component != null ? component : "unknown")
            .register(registry));
    }

    /**
     * Count an SSR sidecar request.
     */
    public void recordSsrRequest() {
        count("inertia.ssr.requests");
    }

    /**
     * Count an SSR sidecar failure.
     */
    public void recordSsrFailure() {
        count("inertia.ssr.failures");
    }

    /**
     * Count an SSR-to-CSR fallback.
     */
    public void recordSsrFallback() {
        count("inertia.ssr.fallback");
    }

    private void count(String name) {
        if (registry != null) {
            Counter.builder(name).register(registry).increment();
        }
    }
}
