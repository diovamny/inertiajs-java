package com.quarkus.inertia.protocol;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class InertiaHttpExceptionQuarkusTest {

    @Test
    void renderWithStatusReturnsJsonPageWithStatusForInertiaRequests() {
        var body = given()
            .header("X-Inertia", "true")
            .when().get("/error-test/render-403")
            .then()
                .statusCode(403)
                .header("X-Inertia", "true")
                .header("X-Inertia-Component", "ErrorPage")
                .extract().jsonPath();

        assertThat(body.getString("component")).isEqualTo("ErrorPage");
        assertThat(body.getInt("props.status")).isEqualTo(403);
        assertThat(body.getString("props.message")).isEqualTo("Forbidden");
    }

    @Test
    void renderWithStatusReturnsHtmlWithStatusForRegularRequests() {
        var html = given()
            .when().get("/error-test/render-404")
            .then()
                .statusCode(404)
                .contentType("text/html")
                .extract().asString();

        assertThat(html).contains("ErrorPage");
        assertThat(html).contains("Not Found");
    }

    @Test
    void renderWithStatus200KeepsNormalStatus() {
        given()
            .header("X-Inertia", "true")
            .when().get("/error-test/render-200")
            .then()
                .statusCode(200);
    }

    @Test
    void webApplicationExceptionBecomesInertiaPageForInertiaRequests() {
        var body = given()
            .header("X-Inertia", "true")
            .when().get("/error-test/throw-403")
            .then()
                .statusCode(403)
                .header("X-Inertia", "true")
                .extract().jsonPath();

        assertThat(body.getString("component")).isEqualTo("ErrorPage");
        assertThat(body.getInt("props.status")).isEqualTo(403);
    }

    @Test
    void unhandledExceptionBecomesInertiaErrorPageForInertiaRequests() {
        var body = given()
            .header("X-Inertia", "true")
            .when().get("/error-test/throw-500")
            .then()
                .statusCode(500)
                .header("X-Inertia", "true")
                .extract().jsonPath();

        assertThat(body.getString("component")).isEqualTo("ErrorPage");
        assertThat(body.getInt("props.status")).isEqualTo(500);
        assertThat(body.getString("props.message")).isEqualTo("boom");
    }

    @Test
    void unhandledExceptionStaysPlain500ForRegularRequests() {
        given()
            .when().get("/error-test/throw-500")
            .then()
                .statusCode(500);
    }

    @Test
    void illegalArgumentExceptionBecomes400ErrorPageForInertiaRequests() {
        var body = given()
            .header("X-Inertia", "true")
            .when().get("/error-test/throw-400")
            .then()
                .statusCode(400)
                .header("X-Inertia", "true")
                .extract().jsonPath();

        assertThat(body.getString("component")).isEqualTo("ErrorPage");
        assertThat(body.getInt("props.status")).isEqualTo(400);
        assertThat(body.getString("props.message")).isEqualTo("invalid input");
    }

    @Test
    void securityExceptionBecomes403ErrorPageForInertiaRequests() {
        var body = given()
            .header("X-Inertia", "true")
            .when().get("/error-test/throw-security")
            .then()
                .statusCode(403)
                .header("X-Inertia", "true")
                .extract().jsonPath();

        assertThat(body.getString("component")).isEqualTo("ErrorPage");
        assertThat(body.getInt("props.status")).isEqualTo(403);
        assertThat(body.getString("props.message")).isEqualTo("denied");
    }

    @Test
    void illegalStateExceptionBecomes409ErrorPageForInertiaRequests() {
        var body = given()
            .header("X-Inertia", "true")
            .when().get("/error-test/throw-409")
            .then()
                .statusCode(409)
                .header("X-Inertia", "true")
                .extract().jsonPath();

        assertThat(body.getString("component")).isEqualTo("ErrorPage");
        assertThat(body.getInt("props.status")).isEqualTo(409);
        assertThat(body.getString("props.message")).isEqualTo("invalid state");
    }

    @Test
    void validationExceptionBecomes422ErrorPageForInertiaRequests() {
        var body = given()
            .header("X-Inertia", "true")
            .when().get("/error-test/throw-422")
            .then()
                .statusCode(422)
                .header("X-Inertia", "true")
                .extract().jsonPath();

        assertThat(body.getString("component")).isEqualTo("ErrorPage");
        assertThat(body.getInt("props.status")).isEqualTo(422);
        assertThat(body.getString("props.message")).isEqualTo("business rule");
    }

    @Test
    void mappedExceptionsKeepSemanticStatusForRegularRequests() {
        given()
            .when().get("/error-test/throw-400")
            .then()
                .statusCode(400);

        given()
            .when().get("/error-test/throw-409")
            .then()
                .statusCode(409);

        given()
            .when().get("/error-test/throw-422")
            .then()
                .statusCode(422);

        given()
            .when().get("/error-test/throw-security")
            .then()
                .statusCode(403);
    }
}
