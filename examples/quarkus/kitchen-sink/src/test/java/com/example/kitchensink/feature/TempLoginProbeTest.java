package com.example.kitchensink.feature;

import static io.restassured.RestAssured.given;

import java.util.Map;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/** Temporary M4b probe: does login work WITH X-Inertia in @QuarkusTest? */
@QuarkusTest
class TempLoginProbeTest {

    @Test
    void loginWithInertiaHeader() {
        var landing = given()
            .redirects().follow(false)
            .when().get("/login")
            .then()
                .statusCode(200)
                .extract();
        var xsrf = landing.cookie("XSRF-TOKEN");
        System.out.println("M4BPROBE xsrf-present=" + (xsrf != null));
        var login = given()
            .cookies(landing.cookies())
            .contentType(ContentType.JSON)
            .header("X-Inertia", "true")
            .header("X-XSRF-TOKEN", xsrf)
            .header("Referer", "/login")
            .body(Map.of("email", "test@example.com", "password", "password"))
            .redirects().follow(false)
            .when().post("/login")
            .then()
                .extract();
        System.out.println("M4BPROBE post-status=" + login.statusCode()
            + " location=" + login.header("Location"));
        var dash = given()
            .cookies(landing.cookies())
            .cookies(login.cookies())
            .header("X-Inertia", "true")
            .redirects().follow(false)
            .when().get("/dashboard")
            .then()
                .extract();
        System.out.println("M4BPROBE dash-status=" + dash.statusCode()
            + " xloc=" + dash.header("X-Inertia-Location")
            + " body=" + dash.asString().substring(0, Math.min(200, dash.asString().length())));
    }
}
