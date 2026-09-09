package io.github.dg.quarkus.inertia.reactive;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

/**
 * End-to-end tests for the reactive routes transport ({@code @Route}):
 * the Inertia protocol must work exactly like the JAX-RS transport,
 * including redirects, 409 conflicts, versioning, error pages and CSRF.
 */
@QuarkusTest
class ReactiveRouteInertiaTest {

    @Test
    void rendersJsonForInertiaRequests() {
        given()
            .header("X-Inertia", "true")
            .when().get("/reactive/hello")
            .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .header("X-Inertia", equalTo("true"))
                .body("component", equalTo("Reactive/Hello"))
                .body("props.name", equalTo("World"));
    }

    @Test
    void rendersHtmlForFullPageRequests() {
        given()
            .when().get("/reactive/hello")
            .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("<div id=\"app\""));
    }

    @Test
    void partialReloadWithDuplicateKeysReturns200() {
        given()
            .header("X-Inertia", "true")
            .header("X-Inertia-Partial-Component", "Reactive/Hello")
            .header("X-Inertia-Partial-Data", "name,name")
            .when().get("/reactive/hello")
            .then()
                .statusCode(200)
                .body("component", equalTo("Reactive/Hello"))
                .body("props.name", equalTo("World"));
    }

    @Test
    void getRedirectsWith302() {
        given()
            .header("X-Inertia", "true")
            .redirects().follow(false)
            .when().get("/reactive/redirect")
            .then()
                .statusCode(302)
                .header("Location", containsString("/reactive/hello"));
    }

    @Test
    void postRedirectsWith303() {
        var response = given()
            .when().get("/reactive/hello")
            .then().statusCode(200).extract();

        given()
            .cookies(response.cookies())
            .header("X-XSRF-TOKEN", response.cookie("XSRF-TOKEN"))
            .header("X-Inertia", "true")
            .redirects().follow(false)
            .when().post("/reactive/redirect")
            .then()
                .statusCode(303)
                .header("Location", containsString("/reactive/hello"));
    }

    @Test
    void hashRedirectReturns409WithXInertiaRedirect() {
        given()
            .header("X-Inertia", "true")
            .header("X-Inertia-Version", "v")
            .redirects().follow(false)
            .when().get("/reactive/hash-redirect")
            .then()
                .statusCode(409)
                .header("X-Inertia-Redirect", containsString("/reactive/hello#section"));
    }

    @Test
    void externalRedirectReturns409ForInertia() {
        given()
            .header("X-Inertia", "true")
            .redirects().follow(false)
            .when().get("/reactive/external")
            .then()
                .statusCode(409)
                .header("X-Inertia-Location", equalTo("https://example.com"));
    }

    @Test
    void externalRedirectReturns302ForBrowser() {
        given()
            .redirects().follow(false)
            .when().get("/reactive/external")
            .then()
                .statusCode(302)
                .header("Location", equalTo("https://example.com"));
    }

    @Test
    void versionMismatchReturns409() {
        given()
            .header("X-Inertia", "true")
            .header("X-Inertia-Version", "definitely-wrong")
            .redirects().follow(false)
            .when().get("/reactive/hello")
            .then()
                .statusCode(409)
                .header("X-Inertia-Location", containsString("/reactive/hello"));
    }

    @Test
    void validationErrorRenders422ForInertia() {
        given()
            .header("X-Inertia", "true")
            .when().get("/reactive/error")
            .then()
                .statusCode(422);
    }

    @Test
    void getIssuesXsrfCookie() {
        var response = given()
            .when().get("/reactive/hello")
            .then().statusCode(200).extract();
        assertThat(response.cookie("XSRF-TOKEN")).isNotBlank();
    }

    @Test
    void postWithoutTokenIsRejectedWith419() {
        given()
            .header("X-Inertia", "true")
            .redirects().follow(false)
            .when().post("/reactive/back")
            .then()
                .statusCode(419);
    }

    @Test
    void postWithTokenRedirectsBackWith303() {
        var response = given()
            .when().get("/reactive/hello")
            .then().statusCode(200).extract();
        var token = response.cookie("XSRF-TOKEN");

        given()
            .cookies(response.cookies())
            .header("X-XSRF-TOKEN", token)
            .header("X-Inertia", "true")
            .header("Referer", "/reactive/hello")
            .redirects().follow(false)
            .when().post("/reactive/back")
            .then()
                .statusCode(303)
                .header("Location", containsString("/reactive/hello"));

        given()
            .cookies(response.cookies())
            .header("X-Inertia", "true")
            .when().get("/reactive/hello")
            .then()
                .statusCode(200)
                .body("props.success", equalTo("backed"));
    }
}
