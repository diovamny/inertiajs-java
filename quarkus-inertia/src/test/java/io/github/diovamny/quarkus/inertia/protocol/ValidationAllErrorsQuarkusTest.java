package io.github.diovamny.quarkus.inertia.protocol;

import static io.restassured.RestAssured.given;

import java.util.Map;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.QuarkusTestProfile;

@QuarkusTest
@TestProfile(ValidationAllErrorsQuarkusTest.AllErrors.class)
class ValidationAllErrorsQuarkusTest {

    public static class AllErrors implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("inertia.validation.all-errors", "true");
        }
    }

    @Test
    void flashedErrorsAreArraysWhenAllErrorsEnabled() {
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
            .header("Referer", "/validation-test")
            .formParam("name", "")
            .redirects().follow(false)
            .when().post("/validation-test")
            .then()
                .log().ifValidationFails()
                .statusCode(303);

        given()
            .cookies(cookies)
            .header("X-Inertia", "true")
            .when().get("/validation-test")
            .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("props.errors.name[0]", org.hamcrest.Matchers.equalTo("required"));
    }

    @Test
    void precognitionEmitsArraysWhenAllErrorsEnabled() {
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
                .body("errors.name[0]", org.hamcrest.Matchers.equalTo("required"));
    }
}
