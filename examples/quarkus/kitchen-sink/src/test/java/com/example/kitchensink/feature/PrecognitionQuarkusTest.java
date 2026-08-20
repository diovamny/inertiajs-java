package com.example.kitchensink.feature;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

@QuarkusTest
class PrecognitionQuarkusTest {

    private Map<String, String> cookies;

    @BeforeEach
    void login() {
        var response = given()
            .contentType(ContentType.JSON)
            .body(Map.of("email", "test@example.com", "password", "password"))
            .redirects().follow(false)
            .when().post("/login")
            .then()
                .log().ifValidationFails()
                .statusCode(303)
                .extract();
        cookies = response.cookies();
    }

    private ValidatableResponse precognition(String validateOnly, Map<String, Object> body) {
        var request = given()
            .cookies(cookies)
            .contentType(ContentType.JSON)
            .header("X-Inertia", "true")
            .header("Precognition", "true")
            .body(body)
            .redirects().follow(false);
        if (validateOnly != null) {
            request.header("Precognition-Validate-Only", validateOnly);
        }
        return request.when().post("/features/forms/precognition").then();
    }

    private Map<String, Object> form(String username, String email, String password, String confirmation) {
        var body = new LinkedHashMap<String, Object>();
        body.put("username", username);
        body.put("email", email);
        body.put("password", password);
        body.put("password_confirmation", confirmation);
        return body;
    }

    @Test
    void validFieldReturns204WithoutFlashedData() {
        var response = precognition("username", form("alice", "", "", ""));

        response.log().ifValidationFails()
            .statusCode(204)
            .header("Precognition", equalTo("true"))
            .header("Precognition-Success", equalTo("true"));

        given()
            .cookies(cookies)
            .header("X-Inertia", "true")
            .when().get("/features/forms/precognition")
            .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("props.errors", not(hasKey("username")))
                .body("props.message", nullValue());
    }

    @Test
    void invalidFieldReturnsOnlyThatFieldError() {
        var response = precognition("username", form("a", "", "", ""));

        response.log().ifValidationFails()
            .statusCode(422)
            .header("Precognition", equalTo("true"))
            .body("errors.username", equalTo("The username field must be between 3 and 20 characters."))
            .body("errors", not(hasKey("email")))
            .body("errors", not(hasKey("password")))
            .body("errors", not(hasKey("password_confirmation")));
    }

    @Test
    void formComponentPrecognitionOnlyReportsRequestedField() {
        var response = given()
            .cookies(cookies)
            .contentType(ContentType.JSON)
            .header("X-Inertia", "true")
            .header("Precognition", "true")
            .header("Precognition-Validate-Only", "name")
            .body(Map.of("name", "", "email", "not-an-email", "bio", "", "role", "developer"))
            .redirects().follow(false)
            .when().post("/features/forms/form-component")
            .then()
                .log().ifValidationFails()
                .statusCode(422)
                .header("Precognition", equalTo("true"))
                .body("errors.name", notNullValue())
                .body("errors", not(hasKey("email")))
                .body("errors", not(hasKey("bio")));
    }

    @Test
    void validEmailIgnoresEmptySiblings() {
        var response = precognition("email", form("", "alice@example.com", "", ""));

        response.log().ifValidationFails()
            .statusCode(204)
            .header("Precognition-Success", equalTo("true"));
    }

    @Test
    void invalidEmailReturnsOnlyEmailError() {
        var response = precognition("email", form("", "nope", "", ""));

        response.log().ifValidationFails()
            .statusCode(422)
            .header("Precognition", equalTo("true"))
            .body("errors.email", equalTo("The email field must be a valid email address."))
            .body("errors", not(hasKey("username")));
    }

    @Test
    void passwordMismatchReturnsOnlyConfirmationError() {
        var response = precognition("password", form("", "", "secret12", "different"));

        response.log().ifValidationFails()
            .statusCode(422)
            .header("Precognition", equalTo("true"))
            .body("errors.password_confirmation", equalTo("The password confirmation does not match."))
            .body("errors", not(hasKey("password")));
    }

    @Test
    void passwordGroupValidReturns204() {
        var response = precognition("password_confirmation", form("", "", "secret12", "secret12"));

        response.log().ifValidationFails()
            .statusCode(204)
            .header("Precognition-Success", equalTo("true"));
    }

    @Test
    void finalSubmitInvalidReturnsAllErrors() {
        var response = given()
            .cookies(cookies)
            .contentType(ContentType.JSON)
            .header("X-Inertia", "true")
            .header("Referer", "http://localhost:8081/features/forms/precognition")
            .body(form("alice", "", "", ""))
            .redirects().follow(false)
            .when().post("/features/forms/precognition")
            .then()
                .log().ifValidationFails()
                .statusCode(303)
                .header("Location", endsWith("/features/forms/precognition"))
                .extract();

        given()
            .cookies(cookies)
            .header("X-Inertia", "true")
            .when().get("/features/forms/precognition")
            .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("props.errors.email", equalTo("The email field is required."))
                .body("props.errors.password", equalTo("The password field is required."))
                .body("props.errors.password_confirmation", equalTo("The password confirmation field is required."))
                .body("props.errors", not(hasKey("username")));
    }

    @Test
    void finalSubmitValidReturnsMessage() {
        var response = given()
            .cookies(cookies)
            .contentType(ContentType.JSON)
            .header("X-Inertia", "true")
            .header("Referer", "http://localhost:8081/features/forms/precognition")
            .body(form("alice", "alice@example.com", "secret12", "secret12"))
            .redirects().follow(false)
            .when().post("/features/forms/precognition")
            .then()
                .log().ifValidationFails()
                .statusCode(303)
                .header("Location", endsWith("/features/forms/precognition"))
                .extract();

        given()
            .cookies(cookies)
            .header("X-Inertia", "true")
            .when().get("/features/forms/precognition")
            .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("props.message", equalTo("Account created for alice!"));
    }
}