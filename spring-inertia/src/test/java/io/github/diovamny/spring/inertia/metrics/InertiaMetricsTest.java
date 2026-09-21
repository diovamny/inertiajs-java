package io.github.diovamny.spring.inertia.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class InertiaMetricsTest {

    @Test
    void renderRecordsCounterAndTimer() {
        var registry = new SimpleMeterRegistry();
        var metrics = new InertiaMetrics(registry);

        var sample = metrics.startRender();
        metrics.stopRender(sample, "Home", "json");

        assertThat(registry.counter("inertia.render.requests",
            "component", "Home", "type", "json").count()).isEqualTo(1);
        assertThat(registry.timer("inertia.render.duration",
            "component", "Home", "type", "json").count()).isEqualTo(1);
    }

    @Test
    void propsResolutionRecordsTimer() {
        var registry = new SimpleMeterRegistry();
        var metrics = new InertiaMetrics(registry);

        var sample = metrics.startPropsResolution();
        metrics.stopPropsResolution(sample, "Home");

        assertThat(registry.timer("inertia.props.resolution.duration",
            "component", "Home").count()).isEqualTo(1);
    }

    @Test
    void ssrCountersIncrement() {
        var registry = new SimpleMeterRegistry();
        var metrics = new InertiaMetrics(registry);

        metrics.recordSsrRequest();
        metrics.recordSsrRequest();
        metrics.recordSsrFailure();
        metrics.recordSsrFallback();

        assertThat(registry.counter("inertia.ssr.requests").count()).isEqualTo(2);
        assertThat(registry.counter("inertia.ssr.failures").count()).isEqualTo(1);
        assertThat(registry.counter("inertia.ssr.fallback").count()).isEqualTo(1);
    }

    @Test
    void noopRecorderSilentlyIgnoresEverything() {
        var metrics = InertiaMetrics.noop();
        assertThatNoException().isThrownBy(() -> {
            Timer.Sample sample = metrics.startRender();
            metrics.stopRender(sample, "Home", "json");
            Timer.Sample props = metrics.startPropsResolution();
            metrics.stopPropsResolution(props, "Home");
            metrics.recordSsrRequest();
            metrics.recordSsrFailure();
            metrics.recordSsrFallback();
        });
    }
}
