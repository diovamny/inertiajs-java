package io.github.diovamny.quarkus.inertia.tck;

import java.util.List;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

import io.github.diovamny.inertia.tck.InertiaTckRunner;

/**
 * G-01: certifies the Quarkus reactive-routes transport against the same
 * normative cases (path-rewritten onto {@code /tck-reactive/*}).
 */
@QuarkusTest
class TckReactiveTest {

    /**
     * Full normative suite except {@code version-mismatch-is-409}: that case
     * asserts the exact {@code X-Inertia-Location: /tck/versioned} path, which
     * is a harness path-mapping artifact (reactive serves the same semantics
     * at {@code /tck-reactive/versioned}, proven by
     * {@link #staleVersionIs409WithReactivePath} below).
     */
    private static final List<String> REACTIVE_CASES = List.of(
        "inertia-visit-returns-json-page",
        "plain-visit-returns-html-root",
        "partial-component-mismatch-renders-full-page",
        "partial-only-filters-props",
        "partial-only-multiple-keys",
        "partial-except-filters-props",
        "shared-props-included-on-full-visit",
        "shared-props-survive-partial-base",
        "deferred-excluded-on-full-visit",
        "deferred-resolves-on-partial",
        "once-present-on-first-visit",
        "once-excluded-with-except-header",
        "once-keyed-custom-tracking-key",
        "once-keyed-suppressed-by-custom-key",
        "once-expired-excluded-from-metadata",
        "once-combos-full-visit",
        "internal-redirect-on-get-visit",
        "state-changing-redirect-is-303",
        "put-redirect-normalized-to-303",
        "delete-redirect-normalized-to-303",
        "external-redirect-is-409-challenge",
        "plain-external-redirect-is-302",
        "back-uses-referer",
        "current-visit-renders-200",
        "get-issues-xsrf-cookie",
        "inertia-post-without-token-is-rejected",
        "inertia-post-with-wrong-token-is-rejected",
        "plain-post-without-token-diverges",
        "invalid-submit-redirects-back",
        "invalid-submit-flashes-errors",
        "named-bag-submit-redirects-back",
        "named-bag-errors-nested-on-reload",
        "multi-message-submit-redirects-back",
        "multi-message-errors-are-ordered-arrays-on-reload",
        "merge-metadata-emitted",
        "merge-reset-clears-metadata",
        "merge-partial-keeps-metadata",
        "merge-nested-append-prepend-metadata",
        "merge-nested-reset-clears-child-path",
        "scroll-metadata-emitted",
        "scroll-prepend-intent",
        "multipart-upload-redirects");

    @Test
    void normativeProtocolSubsetIsGreenOnReactiveRoutes() {
        InertiaTckRunner.assertSubsetGreen(
            "http://localhost:" + io.restassured.RestAssured.port,
            "quarkus",
            REACTIVE_CASES,
            path -> path.replaceFirst("^/tck/", "/tck-reactive/"));
    }

    @Test
    void staleVersionIs409WithReactivePath() throws Exception {
        var client = java.net.http.HttpClient.newBuilder()
            .followRedirects(java.net.http.HttpClient.Redirect.NEVER)
            .build();
        var request = java.net.http.HttpRequest.newBuilder(
                java.net.URI.create("http://localhost:" + io.restassured.RestAssured.port
                    + "/tck-reactive/versioned"))
            .header("X-Inertia", "true")
            .header("X-Inertia-Version", "stale-version-for-tck")
            .GET()
            .build();
        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        org.junit.jupiter.api.Assertions.assertEquals(409, response.statusCode());
        org.junit.jupiter.api.Assertions.assertEquals("/tck-reactive/versioned",
            response.headers().firstValue("X-Inertia-Location").orElse(null));
        org.junit.jupiter.api.Assertions.assertTrue(
            response.headers().firstValue("X-Inertia-Version").isPresent());
    }
}
