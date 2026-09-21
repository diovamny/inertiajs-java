package io.github.diovamny.inertia.core.ssr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class SsrResilienceTest {

    // Circuit breaker -------------------------------------------------------

    @Test
    void breakerOpensAfterThreshold() {
        var breaker = new SsrCircuitBreaker(3, 60, TimeUnit.SECONDS);
        assertThat(breaker.state()).isEqualTo(SsrCircuitBreaker.State.CLOSED);
        breaker.recordFailure();
        breaker.recordFailure();
        assertThat(breaker.allowRequest()).isTrue();
        breaker.recordFailure();
        assertThat(breaker.state()).isEqualTo(SsrCircuitBreaker.State.OPEN);
        assertThat(breaker.allowRequest()).isFalse();
    }

    @Test
    void breakerHalfOpensAfterCooldownAndClosesOnSuccess() throws Exception {
        var breaker = new SsrCircuitBreaker(1, 50, TimeUnit.MILLISECONDS);
        breaker.recordFailure();
        assertThat(breaker.allowRequest()).isFalse();
        Thread.sleep(80);
        assertThat(breaker.state()).isEqualTo(SsrCircuitBreaker.State.HALF_OPEN);
        assertThat(breaker.allowRequest()).isTrue();
        assertThat(breaker.isHalfOpenProbe()).isTrue();
        breaker.recordSuccess();
        assertThat(breaker.state()).isEqualTo(SsrCircuitBreaker.State.CLOSED);
        assertThat(breaker.allowRequest()).isTrue();
    }

    @Test
    void breakerReopensWhenProbeFails() throws Exception {
        var breaker = new SsrCircuitBreaker(1, 50, TimeUnit.MILLISECONDS);
        breaker.recordFailure();
        Thread.sleep(80);
        assertThat(breaker.allowRequest()).isTrue();
        breaker.recordFailure();
        assertThat(breaker.state()).isEqualTo(SsrCircuitBreaker.State.OPEN);
    }

    @Test
    void breakerRejectsBadThreshold() {
        assertThatThrownBy(() -> new SsrCircuitBreaker(0, 1, TimeUnit.SECONDS))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // Response cache --------------------------------------------------------

    @Test
    void cacheHitsMissAndExpires() throws Exception {
        var cache = new SsrResponseCache(10);
        assertThat(cache.get("{\"a\":1}")).isEmpty();
        cache.put("{\"a\":1}", "<div/>", List.of("<title/>"), 100, TimeUnit.MILLISECONDS);
        var hit = cache.get("{\"a\":1}");
        assertThat(hit).isPresent();
        assertThat(hit.get().body()).isEqualTo("<div/>");
        assertThat(hit.get().head()).containsExactly("<title/>");
        assertThat(cache.size()).isEqualTo(1);
        Thread.sleep(150);
        assertThat(cache.get("{\"a\":1}")).isEmpty();
        assertThat(cache.size()).isZero();
    }

    @Test
    void cacheKeysByContentNotIdentity() {
        var cache = new SsrResponseCache(10);
        cache.put(new String("{\"a\":1}"), "<div/>", List.of(), 5, TimeUnit.MINUTES);
        assertThat(cache.get(new String("{\"a\":1}"))).isPresent();
        assertThat(cache.get("{\"a\":2}")).isEmpty();
    }

    @Test
    void cacheEvictsOldestBeyondCapacity() {
        var cache = new SsrResponseCache(2);
        cache.put("{\"a\":1}", "one", List.of(), 5, TimeUnit.MINUTES);
        cache.put("{\"a\":2}", "two", List.of(), 5, TimeUnit.MINUTES);
        cache.put("{\"a\":3}", "three", List.of(), 5, TimeUnit.MINUTES);
        assertThat(cache.get("{\"a\":1}")).isEmpty();
        assertThat(cache.get("{\"a\":3}")).isPresent();
    }

    @Test
    void cacheKeyIsStableMd5() {
        assertThat(SsrResponseCache.key("{\"a\":1}"))
            .isEqualTo(SsrResponseCache.key("{\"a\":1}"));
        assertThat(SsrResponseCache.key("{\"a\":1}")).hasSize(32);
    }

    // Supervisor ------------------------------------------------------------

    @Test
    void backoffDoubles() {
        assertThat(NodeSupervisor.backoffMillis(100, TimeUnit.MILLISECONDS, 1)).isEqualTo(100);
        assertThat(NodeSupervisor.backoffMillis(100, TimeUnit.MILLISECONDS, 2)).isEqualTo(200);
        assertThat(NodeSupervisor.backoffMillis(100, TimeUnit.MILLISECONDS, 3)).isEqualTo(400);
    }

    private static String javaBinary() {
        var binary = System.getProperty("java.home") + "/bin/java";
        if (System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win")) {
            binary += ".exe";
        }
        return binary;
    }

    @Test
    void cleanExitStopsWithoutRestart() throws Exception {
        var java = javaBinary();
        var supervisor = new NodeSupervisor(
            new NodeSupervisor.Config(java, "-version", null, 3, 10, TimeUnit.MILLISECONDS));
        supervisor.start();
        var deadline = System.currentTimeMillis() + 10_000;
        while (supervisor.attempts() == 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        assertThat(supervisor.attempts()).isEqualTo(1);
        while (supervisor.isRunning() && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        Thread.sleep(200);
        supervisor.stop();
        assertThat(supervisor.attempts()).isEqualTo(1);
    }

    @Test
    void failingCommandRestartsBounded() throws Exception {
        var java = javaBinary();
        var supervisor = new NodeSupervisor(
            new NodeSupervisor.Config(java, "-Xbadflag-for-tck", null, 2, 10, TimeUnit.MILLISECONDS));
        supervisor.start();
        var deadline = System.currentTimeMillis() + 15_000;
        while (supervisor.attempts() < 3 && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        supervisor.stop();
        assertThat(supervisor.attempts()).isEqualTo(3);
    }

    // Health -----------------------------------------------------------------

    @Test
    void healthUnknownWhenDisabled() {
        var snapshot = SsrHealth.check(false, null, null);
        assertThat(snapshot.status()).isEqualTo(SsrHealth.Status.UNKNOWN);
    }

    @Test
    void healthDownWhenBreakerOpen() {
        var breaker = new SsrCircuitBreaker(1, 60, TimeUnit.SECONDS);
        breaker.recordFailure();
        var snapshot = SsrHealth.check(true, breaker, null);
        assertThat(snapshot.status()).isEqualTo(SsrHealth.Status.DOWN);
        assertThat(snapshot.details()).containsEntry("breaker", "OPEN");
    }

    @Test
    void healthUpWhenClosed() {
        var snapshot = SsrHealth.check(true, new SsrCircuitBreaker(5, 30, TimeUnit.SECONDS), null);
        assertThat(snapshot.status()).isEqualTo(SsrHealth.Status.UP);
    }
}
