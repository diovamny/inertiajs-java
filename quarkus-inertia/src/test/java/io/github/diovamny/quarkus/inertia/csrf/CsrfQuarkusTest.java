package io.github.diovamny.quarkus.inertia.csrf;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class CsrfQuarkusTest {

    @Test
    void getResponseIssuesXsrfTokenCookie() {
        var response = given()
            .when().get("/csrf-test")
            .then()
                .statusCode(200)
                .extract();
        var token = response.cookie("XSRF-TOKEN");
        assertThat(token).isNotBlank();
    }

    @Test
    void postWithoutTokenIsRejectedWithTeapot() {
        given()
            .when().post("/csrf-test")
            .then()
                .statusCode(419);
    }

    @Test
    void postWithValidTokenSucceeds() {
        var response = given()
            .when().get("/csrf-test")
            .then()
                .statusCode(200)
                .extract();
        var token = response.cookie("XSRF-TOKEN");

        given()
            .cookies(response.cookies())
            .header("X-XSRF-TOKEN", token)
            .when().post("/csrf-test")
            .then()
                .statusCode(200);
    }

    @Test
    void postWithMismatchedTokenIsRejected() {
        var response = given()
            .when().get("/csrf-test")
            .then()
                .statusCode(200)
                .extract();

        given()
            .cookies(response.cookies())
            .header("X-XSRF-TOKEN", "wrong-token")
            .when().post("/csrf-test")
            .then()
                .statusCode(419);
    }

    @Test
    void inertiaPostWithMismatchedTokenRedirectsToSameOriginReferer() {
        var response = given()
            .when().get("/csrf-test")
            .then()
                .statusCode(200)
                .extract();
        var referer = "http://localhost:" + io.restassured.RestAssured.port + "/csrf-test";

        given()
            .redirects().follow(false)
            .cookies(response.cookies())
            .header("X-Inertia", "true")
            .header("X-XSRF-TOKEN", "wrong-token")
            .header("Referer", referer)
            .when().post("/csrf-test")
            .then()
                .statusCode(303)
                .header("Location", referer);
    }

    @Test
    void inertiaPostWithWrongTokenAndNoRefererIs303ToFallback() {
        var response = given()
            .when().get("/csrf-test")
            .then()
                .statusCode(200)
                .extract();

        given()
            .redirects().follow(false)
            .cookies(response.cookies())
            .header("X-Inertia", "true")
            .header("X-XSRF-TOKEN", "wrong-token")
            .when().post("/csrf-test")
            .then()
                .statusCode(303)
                .header("Location", "/");
    }

    @Test
    void nonInertiaPostWithWrongTokenAndRefererIs419() {
        var response = given()
            .when().get("/csrf-test")
            .then()
                .statusCode(200)
                .extract();

        given()
            .cookies(response.cookies())
            .header("X-XSRF-TOKEN", "wrong-token")
            .header("Referer", "http://localhost:8081/csrf-test")
            .when().post("/csrf-test")
            .then()
                .statusCode(419);
    }

    @Test
    void inertiaPostWithExternalRefererFallsBackToSafePath() {
        var response = given()
            .when().get("/csrf-test")
            .then()
                .statusCode(200)
                .extract();

        given()
            .redirects().follow(false)
            .cookies(response.cookies())
            .header("X-Inertia", "true")
            .header("X-XSRF-TOKEN", "wrong-token")
            .header("Referer", "https://evil.example/phish")
            .when().post("/csrf-test")
            .then()
                .statusCode(303)
                .header("Location", "/");
    }
}
