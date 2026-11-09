package io.github.diovamny.quarkus.inertia.session;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class InertiaSessionProducerTest {

    private static final String SESSION_COOKIE = "vertx-web.session";

    @Test
    void sessionCookieReturnedByDefault() {
        var response = given()
            .when().get("/csrf-test")
            .then()
                .statusCode(200)
                .extract();
        assertThat(response.cookie(SESSION_COOKIE)).isNotBlank();
    }

    @Test
    void flashSurvivesPostRedirectGet() {
        var base = "http://localhost:" + io.restassured.RestAssured.port;
        var page = given()
            .header("X-Inertia", "true")
            .when().get("/tck-reactive/page")
            .then()
                .statusCode(200)
                .extract();
        var token = page.cookie("XSRF-TOKEN");

        given()
            .redirects().follow(false)
            .cookies(page.cookies())
            .header("X-Inertia", "true")
            .header("X-XSRF-TOKEN", token)
            .header("Referer", base + "/tck-reactive/page")
            .header("Content-Type", "application/json")
            .body("{}")
            .when().post("/tck-reactive/submit-multi")
            .then()
                .statusCode(303);

        given()
            .cookies(page.cookies())
            .header("X-Inertia", "true")
            .when().get("/tck-reactive/page")
            .then()
                .statusCode(200)
                .body("props.errors.name[0]", org.hamcrest.Matchers.equalTo("required"))
                .body("props.errors.name[1]", org.hamcrest.Matchers.equalTo("must be valid"));
    }

    @Test
    void csrfTokenRoundTripsThroughAutoSession() {
        var response = given()
            .when().get("/csrf-test")
            .then()
                .statusCode(200)
                .extract();
        var token = response.cookie("XSRF-TOKEN");
        assertThat(token).isNotBlank();

        given()
            .cookies(response.cookies())
            .header("X-XSRF-TOKEN", token)
            .when().post("/csrf-test")
            .then()
                .statusCode(200);
    }
}
