package io.github.diovamny.quarkus.inertia.security;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Contract matrix for the Quarkus Security integration: anonymous, roles,
 * real rest-csrf round-trips, 409 challenges, 303 CSRF recovery, 403 pages,
 * malicious referers and reactive-route coverage.
 */
@QuarkusTest
class InertiaQuarkusSecurityTest {

    @BeforeEach
    void resetCounters() {
        SecureResource.SUBMIT_INVOCATIONS.set(0);
        SecureRoutes.SUBMIT_INVOCATIONS.set(0);
    }

    private static String baseUrl() {
        return "http://localhost:" + io.restassured.RestAssured.port;
    }

    @Test
    void anonymousHtmlVisitPassesThrough401() {
        given()
            .when().get("/sec/secure")
            .then()
                .statusCode(401);
    }

    @Test
    void anonymousInertiaVisitIs409WithoutPageHeaders() {
        given()
            .header("X-Inertia", "true")
            .when().get("/sec/secure")
            .then()
                .statusCode(409)
                .header("X-Inertia-Location", "/login")
                .header("X-Inertia", org.hamcrest.Matchers.nullValue());
    }

    @Test
    @TestSecurity(user = "user", roles = "user")
    void userRoleSeesForbiddenInertiaPageOnAdmin() {
        given()
            .header("X-Inertia", "true")
            .when().get("/sec/admin")
            .then()
                .statusCode(403)
                .header("X-Inertia", "true")
                .body("component", equalTo("Errors/Forbidden"))
                .body("props.status", equalTo(403));
    }

    @Test
    void adminBasicAuthAccessesAdmin() {
        given()
            .auth().preemptive().basic("admin", "adminpass")
            .header("X-Inertia", "true")
            .when().get("/sec/admin")
            .then()
                .statusCode(200)
                .body("component", equalTo("Sec/Admin"));
    }

    @Test
    void userBasicAuthIsForbiddenOnAdmin() {
        given()
            .auth().preemptive().basic("user", "userpass")
            .header("X-Inertia", "true")
            .when().get("/sec/admin")
            .then()
                .statusCode(403)
                .body("component", equalTo("Errors/Forbidden"));
    }

    @Test
    void wrongPasswordIsDeniedWithoutChallenge() {
        // Failed Basic credentials on a protected path are denied (403,
        // no re-challenge) while missing credentials get the 401 challenge:
        // either way the page never leaks.
        given()
            .auth().preemptive().basic("user", "wrongpass")
            .when().get("/sec/secure")
            .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "user", roles = "user")
    void identitySummaryIsSharedAsProps() {
        given()
            .header("X-Inertia", "true")
            .when().get("/sec/secure")
            .then()
                .statusCode(200)
                .body("props.auth.user.name", equalTo("user"))
                .body("props.auth.user.roles", org.hamcrest.Matchers.hasItem("user"));
    }

    @Test
    void restCsrfRoundTripReachesControllerExactlyOnce() {
        var session = given()
            .when().get("/sec/public")
            .then()
                .statusCode(200)
                .cookie("XSRF-TOKEN", notNullValue())
                .extract();

        given()
            .redirects().follow(false)
            .auth().preemptive().basic("user", "userpass")
            .cookies(session.cookies())
            .header("X-Inertia", "true")
            .header("X-XSRF-TOKEN", session.cookie("XSRF-TOKEN"))
            .contentType("application/json")
            .body(Map.of("ok", true))
            .when().post("/sec/submit")
            .then()
                .statusCode(303)
                .header("Location", "/sec/secure");
        assertThat(SecureResource.SUBMIT_INVOCATIONS.get()).isEqualTo(1);
    }

    @Test
    void restCsrfMissingTokenIs303WithFlashAndSkipsController() {
        var session = given()
            .when().get("/sec/public")
            .then()
                .statusCode(200)
                .extract();

        given()
            .redirects().follow(false)
            .auth().preemptive().basic("user", "userpass")
            .cookies(session.cookies())
            .header("X-Inertia", "true")
            .header("Referer", baseUrl() + "/sec/secure")
            .contentType("application/json")
            .body(Map.of("ok", true))
            .when().post("/sec/submit")
            .then()
                .statusCode(303)
                .header("Location", containsString("/sec/secure"));
        assertThat(SecureResource.SUBMIT_INVOCATIONS.get()).isZero();

        given()
            .redirects().follow(false)
            .auth().preemptive().basic("user", "userpass")
            .cookies(session.cookies())
            .header("X-Inertia", "true")
            .when().get("/sec/secure")
            .then()
                .statusCode(200)
                .body("props.error", equalTo("La página expiró. Vuelve a intentarlo."));
    }

    @Test
    void restCsrfWrongTokenIs303() {
        var session = given()
            .when().get("/sec/public")
            .then()
                .statusCode(200)
                .extract();

        given()
            .redirects().follow(false)
            .auth().preemptive().basic("user", "userpass")
            .cookies(session.cookies())
            .header("X-Inertia", "true")
            .header("X-XSRF-TOKEN", "wrong-token")
            .header("Referer", baseUrl() + "/sec/secure")
            .contentType("application/json")
            .body(Map.of("ok", true))
            .when().post("/sec/submit")
            .then()
                .statusCode(303);
        assertThat(SecureResource.SUBMIT_INVOCATIONS.get()).isZero();
    }

    @Test
    void maliciousRefererFallsBackToSafePath() {
        var session = given()
            .when().get("/sec/public")
            .then()
                .statusCode(200)
                .extract();

        given()
            .redirects().follow(false)
            .auth().preemptive().basic("user", "userpass")
            .cookies(session.cookies())
            .header("X-Inertia", "true")
            .header("Referer", "https://evil.example/phish")
            .contentType("application/json")
            .body(Map.of("ok", true))
            .when().post("/sec/submit")
            .then()
                .statusCode(303)
                .header("Location", "/");
    }

    @Test
    void reactivePublicPageRendersInFrameworkMode() {
        given()
            .header("X-Inertia", "true")
            .when().get("/rsec/ping")
            .then()
                .statusCode(200)
                .body("component", equalTo("Rsec/Ping"));
    }

    @Test
    void reactiveAdapterCsrfEnforcedInFrameworkMode() {
        var session = given()
            .when().get("/rsec/ping")
            .then()
                .statusCode(200)
                .cookie("XSRF-TOKEN", notNullValue())
                .extract();

        given()
            .redirects().follow(false)
            .cookies(session.cookies())
            .header("X-Inertia", "true")
            .header("X-XSRF-TOKEN", session.cookie("XSRF-TOKEN"))
            .when().post("/rsec/submit")
            .then()
                .statusCode(303);
        assertThat(SecureRoutes.SUBMIT_INVOCATIONS.get()).isEqualTo(1);

        given()
            .redirects().follow(false)
            .cookies(session.cookies())
            .header("X-Inertia", "true")
            .header("X-XSRF-TOKEN", "wrong-token")
            .header("Referer", baseUrl() + "/rsec/ping")
            .when().post("/rsec/submit")
            .then()
                .statusCode(303)
                .header("Location", containsString("/rsec/ping"));
        assertThat(SecureRoutes.SUBMIT_INVOCATIONS.get()).isEqualTo(1);
    }
}
