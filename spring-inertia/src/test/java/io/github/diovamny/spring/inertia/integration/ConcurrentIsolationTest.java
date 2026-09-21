package io.github.diovamny.spring.inertia.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * G-14: 100 concurrent requests alternating partial-reload headers and
 * per-request prop values must show zero cross-request contamination
 * (ThreadLocal/request-scope isolation net).
 */
@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class ConcurrentIsolationTest {

    private static final int REQUESTS = 100;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void partialHeadersNeverLeakAcrossConcurrentRequests() throws Exception {
        assertConcurrent(REQUESTS, i -> {
            var want = i % 2 == 0 ? "a" : "b";
            var other = want.equals("a") ? "b" : "a";
            var expected = (want.equals("a") ? "A-" : "B-") + "t" + i;
            mockMvc.perform(get("/concurrent-echo")
                    .header("X-Inertia", "true")
                    .header("X-Inertia-Version", "test-version")
                    .header("X-Inertia-Partial-Component", "Echo")
                    .header("X-Inertia-Partial-Data", want)
                    .header("X-Echo", "t" + i))
                .andExpect(status().isOk())
                // The (always shared, possibly empty) errors bag is orthogonal;
                // what must never leak is the sibling data prop or its value.
                .andExpect(jsonPath("$.props." + want).value(expected))
                .andExpect(jsonPath("$.props." + other).doesNotExist());
        });
    }

    @Test
    void perRequestPropsNeverLeakAcrossConcurrentRequests() throws Exception {
        assertConcurrent(REQUESTS, i -> {
            mockMvc.perform(get("/concurrent-echo")
                    .header("X-Inertia", "true")
                    .header("X-Inertia-Version", "test-version")
                    .header("X-Echo", "user-" + i))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.props.a").value("A-user-" + i))
                .andExpect(jsonPath("$.props.b").value("B-user-" + i));
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
