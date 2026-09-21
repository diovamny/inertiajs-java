package io.github.diovamny.quarkus.inertia.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.enterprise.inject.Instance;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class InertiaMetricsTest {

    @SuppressWarnings("unchecked")
    private static InertiaMetrics withRegistry(MeterRegistry registry) {
        var metrics = new InertiaMetrics();
        var registries = mock(Instance.class);
        when(registries.isUnsatisfied()).thenReturn(false);
        when(registries.get()).thenReturn(registry);
        metrics.registries = registries;
        return metrics;
    }

    @Test
    void renderRecordsCounterAndTimer() {
        var registry = new SimpleMeterRegistry();
        var metrics = withRegistry(registry);

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
        var metrics = withRegistry(registry);

        var sample = metrics.startPropsResolution();
        metrics.stopPropsResolution(sample, "Home");

        assertThat(registry.timer("inertia.props.resolution.duration",
            "component", "Home").count()).isEqualTo(1);
    }

    @Test
    void ssrCountersIncrement() {
        var registry = new SimpleMeterRegistry();
        var metrics = withRegistry(registry);

        metrics.recordSsrRequest();
        metrics.recordSsrRequest();
        metrics.recordSsrFailure();
        metrics.recordSsrFallback();

        assertThat(registry.counter("inertia.ssr.requests").count()).isEqualTo(2);
        assertThat(registry.counter("inertia.ssr.failures").count()).isEqualTo(1);
        assertThat(registry.counter("inertia.ssr.fallback").count()).isEqualTo(1);
    }

    @Test
    void missingRegistrySilentlyNoops() {
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
