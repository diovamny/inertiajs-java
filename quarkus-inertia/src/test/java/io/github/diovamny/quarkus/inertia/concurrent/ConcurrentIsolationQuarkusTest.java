package io.github.diovamny.quarkus.inertia.concurrent;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

/**
 * G-14: 100 concurrent requests alternating partial-reload headers and
 * per-request prop values must show zero cross-request contamination
 * (regression net for the G-17 EventLoop context leak).
 */
@QuarkusTest
class ConcurrentIsolationQuarkusTest {

    private static final int REQUESTS = 100;

    @Test
    void partialHeadersNeverLeakAcrossConcurrentRequests() throws Exception {
        assertConcurrent(REQUESTS, i -> {
            var want = i % 2 == 0 ? "a" : "b";
            var page = given()
                .header("X-Inertia", "true")
                .header("X-Inertia-Partial-Component", "Echo")
                .header("X-Inertia-Partial-Data", want)
                .header("X-Echo", "t" + i)
                .when().get("/concurrent-echo")
                .then()
                    .statusCode(200)
                    .extract().jsonPath();
            var props = page.getMap("props");
            // The (always shared, possibly empty) errors bag is orthogonal;
            // what must never leak is the sibling data prop or its value.
            var other = want.equals("a") ? "b" : "a";
            assertThat(props).containsKey(want);
            assertThat(props).doesNotContainKey(other);
            assertThat(props.get(want))
                .isEqualTo((want.equals("a") ? "A-" : "B-") + "t" + i);
        });
    }

    @Test
    void perRequestPropsNeverLeakAcrossConcurrentRequests() throws Exception {
        assertConcurrent(REQUESTS, i -> {
            var page = given()
                .header("X-Inertia", "true")
                .header("X-Echo", "user-" + i)
                .when().get("/concurrent-echo")
                .then()
                    .statusCode(200)
                    .extract().jsonPath();
            assertThat(page.getString("props.a")).isEqualTo("A-user-" + i);
            assertThat(page.getString("props.b")).isEqualTo("B-user-" + i);
        });
    }

    @FunctionalInterface
    private interface RequestTask {
        void run(int index) throws Exception;
    }

    private static void assertConcurrent(int count, RequestTask task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(count, 32));
        try {
            var gate = new CountDownLatch(1);
            var futures = new ArrayList<Future<?>>(count);
            for (int i = 0; i < count; i++) {
                final int index = i;
                futures.add(pool.submit(() -> {
                    gate.await(30, TimeUnit.SECONDS);
                    task.run(index);
                    return null;
                }));
            }
            gate.countDown();
            for (var future : futures) {
                future.get(120, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
