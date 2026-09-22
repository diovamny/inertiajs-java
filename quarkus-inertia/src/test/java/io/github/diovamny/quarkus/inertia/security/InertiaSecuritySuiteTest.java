package io.github.diovamny.quarkus.inertia.security;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * §3.17 adapter attack suite (Quarkus side, JAX-RS + Reactive): redirect
 * targets fail closed before any header is emitted, and oversized defaults
 * stay permissive (the 413 closedown is pinned by
 * {@code OversizedPageProfileTest}).
 */
@QuarkusTest
class InertiaSecuritySuiteTest {

    @Test
    void javascriptRedirectIsRejectedWithoutLocation() {
        given().queryParam("target", "javascript:alert(1)")
            .when().get("/tck/redirect-to")
            .then().statusCode(org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.greaterThanOrEqualTo(400),
                org.hamcrest.Matchers.lessThan(600)))
            .header("Location", nullValue())
            .header("X-Inertia-Location", nullValue());
    }

    @Test
    void crlfRedirectIsRejectedWithoutSplit() {
        given().urlEncodingEnabled(false)
            .queryParam("target", "/ok%0D%0AX:%201")
            .when().get("/tck/redirect-to")
            .then().statusCode(org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.greaterThanOrEqualTo(400),
                org.hamcrest.Matchers.lessThan(600)))
            .header("Location", nullValue());
    }

    @Test
    void reactiveJavascriptRedirectIsRejected() {
        given().queryParam("target", "javascript:alert(1)")
            .when().get("/tck-reactive/redirect-to")
            .then().statusCode(org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.greaterThanOrEqualTo(400),
                org.hamcrest.Matchers.lessThan(600)))
            .header("Location", nullValue())
            .header("X-Inertia-Location", nullValue());
    }

    @Test
    void legitimateRedirectStillWorks() {
        given().redirects().follow(false)
            .queryParam("target", "/tck/page")
            .when().get("/tck/redirect-to")
            .then().statusCode(302)
            .header("Location", "/tck/page");
    }

    @Test
    void largePagePassesDefaultCap() {
        given().header("X-Inertia", "true")
            .when().get("/tck/big-page")
            .then().statusCode(200)
            .body("props.bulk", notNullValue());
    }

    @Test
    void jsonResponsesCarryVary() {
        given().header("X-Inertia", "true")
            .when().get("/tck/page")
            .then().statusCode(200)
            .header("Vary", containsString("X-Inertia"));
    }
}
