package com.quarkus.inertia.protocol;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class InertiaRedirectQuarkusTest {

    @Test
    void backWithFlashesSuccessAfterRedirect() {
        var session = given()
            .when().get("/back-redirect-test")
            .then().statusCode(200).extract();
        var cookies = session.cookies();
        var token = session.cookie("XSRF-TOKEN");

        given()
            .cookies(cookies)
            .header("X-XSRF-TOKEN", token)
            .header("X-Inertia", "true")
            .header("Referer", "http://localhost:8081/back-redirect-test")
            .redirects().follow(false)
            .when().post("/back-redirect-test/flash")
            .then()
                .log().ifValidationFails()
                .statusCode(303)
                .header("Location", endsWith("/back-redirect-test"));

        given()
            .cookies(cookies)
            .header("X-Inertia", "true")
            .when().get("/back-redirect-test")
            .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("props.success", equalTo("Registro actualizado correctamente."));
    }

    @Test
    void backWithErrorsLandsInPropsErrorsAfterRedirect() {
        var session = given()
            .when().get("/back-redirect-test")
            .then().statusCode(200).extract();
        var cookies = session.cookies();
        var token = session.cookie("XSRF-TOKEN");

        given()
            .cookies(cookies)
            .header("X-XSRF-TOKEN", token)
            .header("X-Inertia", "true")
            .header("Referer", "http://localhost:8081/back-redirect-test")
            .redirects().follow(false)
            .when().post("/back-redirect-test/errors")
            .then()
                .log().ifValidationFails()
                .statusCode(303)
                .header("Location", endsWith("/back-redirect-test"));

        given()
            .cookies(cookies)
            .header("X-Inertia", "true")
            .when().get("/back-redirect-test")
            .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("props.errors.email", equalTo("El correo electrónico no es válido."));
    }

    @Test
    void chainedWithAndWithErrorsLandsBothInProps() {
        var session = given()
            .when().get("/back-redirect-test")
            .then().statusCode(200).extract();
        var cookies = session.cookies();
        var token = session.cookie("XSRF-TOKEN");

        given()
            .cookies(cookies)
            .header("X-XSRF-TOKEN", token)
            .header("X-Inertia", "true")
            .header("Referer", "http://localhost:8081/back-redirect-test")
            .redirects().follow(false)
            .when().post("/back-redirect-test/chain")
            .then()
                .log().ifValidationFails()
                .statusCode(303);

        given()
            .cookies(cookies)
            .header("X-Inertia", "true")
            .when().get("/back-redirect-test")
            .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("props.success", equalTo("ok"))
                .body("props.errors.email", equalTo("invalid"));
    }

    @Test
    void redirectWithFlashLandsInPropsAfterRedirect() {
        var session = given()
            .when().get("/back-redirect-test")
            .then().statusCode(200).extract();
        var cookies = session.cookies();
        var token = session.cookie("XSRF-TOKEN");

        given()
            .cookies(cookies)
            .header("X-XSRF-TOKEN", token)
            .header("X-Inertia", "true")
            .redirects().follow(false)
            .when().post("/back-redirect-test/redirect-flash")
            .then()
                .log().ifValidationFails()
                .statusCode(303)
                .header("Location", endsWith("/back-redirect-test"));

        given()
            .cookies(cookies)
            .header("X-Inertia", "true")
            .when().get("/back-redirect-test")
            .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("props.success", equalTo("Contact created."));
    }
}
