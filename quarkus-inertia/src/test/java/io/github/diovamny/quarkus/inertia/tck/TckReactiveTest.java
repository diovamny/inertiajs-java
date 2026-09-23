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

    private static final List<String> REACTIVE_CASES = List.of(
        "inertia-visit-returns-json-page",
        "plain-visit-returns-html-root",
        "partial-only-filters-props",
        "partial-except-filters-props",
        "internal-redirect-on-get-visit",
        "external-redirect-is-409-challenge",
        "plain-external-redirect-is-302",
        "shared-props-included-on-full-visit",
        "shared-props-survive-partial-base",
        "once-keyed-custom-tracking-key",
        "once-keyed-suppressed-by-custom-key",
        "once-expired-excluded-from-metadata",
        "merge-metadata-emitted",
        "merge-reset-clears-metadata",
        "merge-partial-keeps-metadata",
        "scroll-metadata-emitted",
        "scroll-prepend-intent",
        "named-bag-submit-redirects-back",
        "named-bag-errors-nested-on-reload",
        "multi-message-submit-redirects-back",
        "multi-message-errors-are-ordered-arrays-on-reload",
        "multipart-upload-redirects",
        "once-combos-full-visit");

    @Test
    void normativeProtocolSubsetIsGreenOnReactiveRoutes() {
        InertiaTckRunner.assertSubsetGreen(
            "http://localhost:" + io.restassured.RestAssured.port,
            "quarkus",
            REACTIVE_CASES,
            path -> path.replaceFirst("^/tck/", "/tck-reactive/"));
    }
}
