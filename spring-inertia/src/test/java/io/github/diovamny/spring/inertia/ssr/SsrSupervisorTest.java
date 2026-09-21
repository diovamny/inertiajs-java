package io.github.diovamny.spring.inertia.ssr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import io.github.diovamny.inertia.core.ssr.SsrCircuitBreaker;
import io.github.diovamny.inertia.core.ssr.SsrHealth;
import io.github.diovamny.spring.inertia.config.InertiaProperties;
import io.github.diovamny.spring.inertia.renderer.SsrClient;

class SsrSupervisorTest {

    private static String javaBinary() {
        var binary = System.getProperty("java.home") + "/bin/java";
        if (System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win")) {
            binary += ".exe";
        }
        return binary;
    }

    private static InertiaProperties properties() {
        var properties = new InertiaProperties();
        properties.setSsrSupervisorCommand(javaBinary());
        properties.setSsrSupervisorEntry("-version");
        properties.setSsrSupervisorMaxRestarts(2);
        properties.setSsrEnabled(true);
        return properties;
    }

    @Test
    void lifecycleStartsAndStopsCleanCommand() throws Exception {
        var lifecycle = new SsrSupervisorLifecycle(properties());
        lifecycle.start();
        try {
            assertThat(lifecycle.isRunning()).isTrue();
            var deadline = System.currentTimeMillis() + 10_000;
            while (lifecycle.supervisor() != null
                    && lifecycle.supervisor().attempts() == 0
                    && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            assertThat(lifecycle.supervisor()).isNotNull();
            assertThat(lifecycle.supervisor().attempts()).isEqualTo(1);
        } finally {
            lifecycle.stop();
        }
        assertThat(lifecycle.isRunning()).isFalse();
    }

    @Test
    void healthUnknownWhenSsrDisabled() {
        var properties = new InertiaProperties();
        var bean = new SsrHealthBean(properties, mock(SsrClient.class), emptyProvider());
        assertThat(bean.snapshot().status()).isEqualTo(SsrHealth.Status.UNKNOWN);
    }

    @Test
    void healthDownWhenBreakerOpen() {
        var properties = properties();
        var client = mock(SsrClient.class);
        var breaker = new SsrCircuitBreaker(1, 60, TimeUnit.SECONDS);
        breaker.recordFailure();
        when(client.circuitBreaker()).thenReturn(breaker);
        var bean = new SsrHealthBean(properties, client, emptyProvider());
        var snapshot = bean.snapshot();
        assertThat(snapshot.status()).isEqualTo(SsrHealth.Status.DOWN);
        assertThat(snapshot.details()).containsEntry("breaker", "OPEN");
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<SsrSupervisorLifecycle> emptyProvider() {
        var provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        return provider;
    }
}
