package io.github.dg.quarkus.inertia.csrf;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;

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
}