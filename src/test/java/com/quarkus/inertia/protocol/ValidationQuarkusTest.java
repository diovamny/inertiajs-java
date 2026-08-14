package com.quarkus.inertia.protocol;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ValidationQuarkusTest {

    @Test
    void validationFailureRedirectsBackAndFlashesErrors() {
        var response = given()
            .when().get("/validation-test")
            .then()
                .statusCode(200)
                .extract();
        var cookies = response.cookies();
        var token = response.cookie("XSRF-TOKEN");

        given()
            .cookies(cookies)
            .header("X-XSRF-TOKEN", token)
            .header("X-Inertia", "true")
            .header("Referer", "http://localhost:8081/validation-test")
            .formParam("name", "")
            .redirects().follow(false)
            .when().post("/validation-test")
            .then()
                .log().ifValidationFails()
                .statusCode(303)
                .header("Location", endsWith("/validation-test"));

        given()
            .cookies(cookies)
            .header("X-Inertia", "true")
            .when().get("/validation-test")
            .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("props.errors.name", equalTo("required"));
    }

    @Test
    void validSubmissionRedirectsWithoutErrors() {
        var response = given()
            .when().get("/validation-test")
            .then()
                .statusCode(200)
                .extract();
        var cookies = response.cookies();
        var token = response.cookie("XSRF-TOKEN");

        given()
            .cookies(cookies)
            .header("X-XSRF-TOKEN", token)
            .header("X-Inertia", "true")
            .formParam("name", "John")
            .redirects().follow(false)
            .when().post("/validation-test")
            .then()
                .log().ifValidationFails()
                .statusCode(303)
                .header("Location", equalTo("/validation-test"));

        given()
            .cookies(cookies)
            .header("X-Inertia", "true")
            .when().get("/validation-test")
            .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("props.errors", not(hasKey("name")));
    }

    @Test
    void validationFailureWithoutInertiaIsPlain400() {
        var response = given()
            .when().get("/validation-test")
            .then()
                .statusCode(200)
                .extract();
        var cookies = response.cookies();
        var token = response.cookie("XSRF-TOKEN");

        given()
            .cookies(cookies)
            .header("X-XSRF-TOKEN", token)
            .formParam("name", "")
            .when().post("/validation-test")
            .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    void precognitionFailureReturns422WithWrappedErrors() {
        var response = given()
            .when().get("/validation-test")
            .then()
                .statusCode(200)
                .extract();
        var cookies = response.cookies();
        var token = response.cookie("XSRF-TOKEN");

        given()
            .cookies(cookies)
            .header("X-XSRF-TOKEN", token)
            .header("X-Inertia", "true")
            .header("Precognition", "true")
            .header("Precognition-Validate-Only", "name")
            .formParam("name", "")
            .when().post("/validation-test")
            .then()
                .log().ifValidationFails()
                .statusCode(422)
                .header("Precognition", equalTo("true"))
                .body("errors.name", equalTo("required"));
    }

    @Test
    void precognitionFailureIgnoresUnrequestedFields() {
        var response = given()
            .when().get("/validation-test")
            .then()
                .statusCode(200)
                .extract();
        var cookies = response.cookies();
        var token = response.cookie("XSRF-TOKEN");

        given()
            .cookies(cookies)
            .header("X-XSRF-TOKEN", token)
            .header("X-Inertia", "true")
            .header("Precognition", "true")
            .header("Precognition-Validate-Only", "other")
            .formParam("name", "")
            .when().post("/validation-test")
            .then()
                .log().ifValidationFails()
                .statusCode(422)
                .header("Precognition", equalTo("true"))
                .body("errors", not(hasKey("name")));
    }
}
