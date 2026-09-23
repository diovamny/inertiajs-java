package io.github.diovamny.quarkus.inertia.tck;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * M7 stabilization profile for Quarkus Reactive Routes: concurrent,
 * deterministic repetition of session/flash isolation, CSRF valid/invalid,
 * 303/409, error bags (named + multi-message), once/deferred/partial/reset
 * and simultaneous distinct identities. No sleeps, no shared cookies.
 */
@QuarkusTest
class ReactiveStressTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int IDENTITIES = 8;
    private static final int ROUNDS = 12;

    private String base() {
        return "http://localhost:" + io.restassured.RestAssured.port;
    }

    private HttpClient client(CookieManager jar) {
        return HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(10))
            .cookieHandler(jar)
            .build();
    }

    private String xsrfToken(HttpClient client, String base) throws Exception {
        var response = client.send(HttpRequest.newBuilder(URI.create(base + "/tck-reactive/page"))
            .GET().build(), HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        for (var cookie : response.headers().allValues("Set-Cookie")) {
            if (cookie.startsWith("XSRF-TOKEN=")) {
                return cookie.substring("XSRF-TOKEN=".length(), cookie.indexOf(';'));
            }
        }
        Assertions.fail("XSRF-TOKEN cookie missing");
        return null;
    }

    private void assertFlashIsolation(int identity) throws Exception {
        var base = base();
        var jar = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        var http = client(jar);
        var token = xsrfToken(http, base);

        // This identity flashes two ordered messages...
        var post = HttpRequest.newBuilder(URI.create(base + "/tck-reactive/submit-multi"))
            .header("X-Inertia", "true")
            .header("X-XSRF-TOKEN", token)
            .header("Referer", base + "/tck-reactive/page")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{}"))
            .build();
        var postResponse = http.send(post, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(303, postResponse.statusCode(), "identity " + identity);

        var get = HttpRequest.newBuilder(URI.create(base + "/tck-reactive/page"))
            .header("X-Inertia", "true")
            .GET().build();
        var page = http.send(get, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, page.statusCode());
        var errors = JSON.readTree(page.body()).path("props").path("errors").path("name");
        Assertions.assertTrue(errors.isArray(),
            () -> "identity " + identity + " must see an array, got: " + page.body());
        Assertions.assertEquals("required", errors.get(0).asText());
        Assertions.assertEquals("must be valid", errors.get(1).asText());

        // ...while a fresh identity concurrently sees no leaked flash.
        var otherJar = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        var other = client(otherJar);
        xsrfToken(other, base);
        var otherPage = other.send(get, HttpResponse.BodyHandlers.ofString());
        var otherErrors = JSON.readTree(otherPage.body()).path("props").path("errors");
        Assertions.assertTrue(otherErrors.isMissingNode() || !otherErrors.has("name"),
            "identity " + identity + " leaked flash across sessions");
    }

    private void assertCsrfContract() throws Exception {
        var base = base();
        var jar = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        var http = client(jar);
        var token = xsrfToken(http, base);

        // Valid token reaches the controller (303 to the form target).
        var ok = http.send(HttpRequest.newBuilder(URI.create(base + "/tck-reactive/submit"))
            .header("X-Inertia", "true")
            .header("X-XSRF-TOKEN", token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"Acme\"}"))
            .build(), HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(303, ok.statusCode());

        // Missing token never reaches the controller (303 to the failure path).
        var denied = http.send(HttpRequest.newBuilder(URI.create(base + "/tck-reactive/submit"))
            .header("X-Inertia", "true")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"Acme\"}"))
            .build(), HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(303, denied.statusCode());
        Assertions.assertEquals("/", denied.headers().firstValue("Location").orElse(null));
    }

    private void assertVersionOnceDeferredPartialReset() throws Exception {
        var base = base();
        var jar = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        var http = client(jar);
        xsrfToken(http, base);

        // 409 on stale version with reactive path + version header.
        var stale = http.send(HttpRequest.newBuilder(
                URI.create(base + "/tck-reactive/versioned"))
            .header("X-Inertia", "true")
            .header("X-Inertia-Version", "stale-version-for-tck")
            .GET().build(), HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(409, stale.statusCode());
        Assertions.assertEquals("/tck-reactive/versioned",
            stale.headers().firstValue("X-Inertia-Location").orElse(null));

        // Once: delivered, then suppressed by tracking header.
        var once = http.send(HttpRequest.newBuilder(URI.create(base + "/tck-reactive/once-keyed"))
            .header("X-Inertia", "true")
            .GET().build(), HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals("Keyed",
            JSON.readTree(once.body()).path("props").path("notice").asText());
        var suppressed = http.send(HttpRequest.newBuilder(URI.create(base + "/tck-reactive/once-keyed"))
            .header("X-Inertia", "true")
            .header("X-Inertia-Except-Once-Props", "tckNotice")
            .GET().build(), HttpResponse.BodyHandlers.ofString());
        var suppressedNotice = JSON.readTree(suppressed.body()).path("props").path("notice");
        Assertions.assertTrue(suppressedNotice.isMissingNode() || suppressedNotice.isNull(),
            () -> "suppressed response was: " + suppressed.statusCode() + " " + suppressed.body());

        // Deferred resolves only on partial.
        var deferred = http.send(HttpRequest.newBuilder(URI.create(base + "/tck-reactive/deferred"))
            .header("X-Inertia", "true")
            .header("X-Inertia-Partial-Component", "Tck/Deferred")
            .header("X-Inertia-Partial-Data", "data")
            .GET().build(), HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals("lazy-value",
            JSON.readTree(deferred.body()).path("props").path("data").asText());

        // Nested merge metadata + child-path reset.
        var nested = http.send(HttpRequest.newBuilder(URI.create(base + "/tck-reactive/merge-nested"))
            .header("X-Inertia", "true")
            .GET().build(), HttpResponse.BodyHandlers.ofString());
        JsonNode root = JSON.readTree(nested.body());
        Assertions.assertTrue(root.path("mergeProps").toString().contains("posts.data"));
        var reset = http.send(HttpRequest.newBuilder(URI.create(base + "/tck-reactive/merge-nested"))
            .header("X-Inertia", "true")
            .header("X-Inertia-Reset", "posts.data")
            .GET().build(), HttpResponse.BodyHandlers.ofString());
        JsonNode resetRoot = JSON.readTree(reset.body());
        Assertions.assertFalse(resetRoot.path("mergeProps").toString().contains("posts.data"));
        Assertions.assertTrue(resetRoot.path("prependProps").toString().contains("posts.pinned"));
    }

    @Test
    void reactiveTransportIsStableUnderConcurrency() throws Exception {
        var gate = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(IDENTITIES);
        try {
            List<Callable<String>> jobs = new ArrayList<>();
            for (int identity = 0; identity < IDENTITIES; identity++) {
                final int id = identity;
                jobs.add(() -> {
                    Assertions.assertTrue(gate.await(30, TimeUnit.SECONDS), "start gate");
                    for (int round = 0; round < ROUNDS; round++) {
                        assertFlashIsolation(id);
                        assertCsrfContract();
                        assertVersionOnceDeferredPartialReset();
                    }
                    return "identity-" + id + "-ok";
                });
            }
            var futures = jobs.stream().map(pool::submit).toList();
            gate.countDown();
            for (Future<String> future : futures) {
                future.get(10, TimeUnit.MINUTES);
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
