package io.github.dg.quarkus.inertia.csrf;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class PrecognitionQuarkusTest {

    @Test
    void precognitionSuccessIs204() {
        given()
            .header("Precognition", "true")
            .when().get("/precog-test")
            .then()
                .statusCode(204)
                .header("Precognition-Success", equalTo("true"))
                .header("X-Inertia", equalTo("true"))
                .header("Vary", containsString("Precognition"));
    }

    @Test
    void precognitionWithErrorsIs422() {
        given()
            .header("Precognition", "true")
            .when().get("/precog-test?withErrors=true")
            .then()
                .log().ifValidationFails()
                .statusCode(422)
                .header("X-Inertia", equalTo("true"))
                .header("Precognition", equalTo("true"))
                .header("Vary", containsString("Precognition"))
                .body("errors.name", equalTo("required"));
    }

    @Test
    void withoutPrecognitionRendersHtml() {
        given()
            .when().get("/precog-test")
            .then()
                .statusCode(200)
                .contentType(containsString("text/html"));
    }
}
