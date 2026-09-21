package io.github.diovamny.quarkus.inertia.renderer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.github.diovamny.inertia.core.ssr.SsrHealth;
import io.github.diovamny.quarkus.inertia.config.InertiaConfig;

class SsrSupervisorTest {

    private static String javaBinary() {
        var binary = System.getProperty("java.home") + "/bin/java";
        if (System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win")) {
            binary += ".exe";
        }
        return binary;
    }

    private static InertiaConfig config(boolean enabled) {
        var config = mock(InertiaConfig.class);
        when(config.ssrSupervisorEnabled()).thenReturn(enabled);
        when(config.ssrSupervisorCommand()).thenReturn(javaBinary());
        when(config.ssrSupervisorEntry()).thenReturn("-version");
        when(config.ssrSupervisorWorkdir()).thenReturn(Optional.empty());
        when(config.ssrSupervisorMaxRestarts()).thenReturn(2);
        when(config.ssrEnabled()).thenReturn(true);
        return config;
    }

    @Test
    void disabledSupervisorNeverSpawns() {
        var supervisor = new SsrSupervisor();
        supervisor.config = config(false);
        supervisor.onStart(null);
        assertThat(supervisor.supervisor()).isNull();
    }

    @Test
    void enabledSupervisorStartsAndStopsCleanCommand() throws Exception {
        var supervisor = new SsrSupervisor();
        supervisor.config = config(true);
        supervisor.onStart(null);
        try {
            var deadline = System.currentTimeMillis() + 10_000;
            while (supervisor.supervisor() != null
                    && supervisor.supervisor().attempts() == 0
                    && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            assertThat(supervisor.supervisor()).isNotNull();
            assertThat(supervisor.supervisor().attempts()).isEqualTo(1);
        } finally {
            supervisor.onStop(null);
        }
    }

    @Test
    void healthUnknownWhenSsrDisabled() {
        var config = mock(InertiaConfig.class);
        when(config.ssrEnabled()).thenReturn(false);
        var bean = new SsrHealthBean();
        bean.config = config;
        bean.ssrHandler = mock(SsrHandler.class);
        bean.supervisor = mock(SsrSupervisor.class);
        var snapshot = bean.snapshot();
        assertThat(snapshot.status()).isEqualTo(SsrHealth.Status.UNKNOWN);
    }

    @Test
    void healthDownWhenBreakerOpen() {
        var config = mock(InertiaConfig.class);
        when(config.ssrEnabled()).thenReturn(true);
        var handler = mock(SsrHandler.class);
        var breaker = new io.github.diovamny.inertia.core.ssr.SsrCircuitBreaker(
            1, 60, java.util.concurrent.TimeUnit.SECONDS);
        breaker.recordFailure();
        when(handler.circuitBreaker()).thenReturn(breaker);
        var bean = new SsrHealthBean();
        bean.config = config;
        bean.ssrHandler = handler;
        bean.supervisor = mock(SsrSupervisor.class);
        when(bean.supervisor.supervisor()).thenReturn(null);
        var snapshot = bean.snapshot();
        assertThat(snapshot.status()).isEqualTo(SsrHealth.Status.DOWN);
    }
}
