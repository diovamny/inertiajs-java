package io.github.diovamny.quarkus.inertia.csrf;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(CsrfRefreshPolicyTest.LazyRefreshProfile.class)
class CsrfRefreshPolicyTest {

    public static class LazyRefreshProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("inertia.csrf-refresh-policy", "lazy");
        }
    }

    @Test
    void firstVisitStillIssuesTokenCookie() {
        var response = given()
            .when().get("/csrf-test")
            .then()
                .statusCode(200)
                .extract();
        assertThat(response.cookie("XSRF-TOKEN")).isNotBlank();
    }

    @Test
    void secondIdempotentVisitWithValidCookieSkipsReemission() {
        var first = given()
            .when().get("/csrf-test")
            .then()
                .statusCode(200)
                .extract();
        var second = given()
            .cookies(first.cookies())
            .when().get("/csrf-test")
            .then()
                .statusCode(200)
                .extract();
        assertThat(second.headers().getValues("Set-Cookie"))
            .noneMatch(h -> h.startsWith("XSRF-TOKEN="));
    }

    @Test
    void stateChangingRequestWithValidTokenStillSucceeds() {
        var first = given()
            .when().get("/csrf-test")
            .then()
                .statusCode(200)
                .extract();
        var token = first.cookie("XSRF-TOKEN");
        given()
            .cookies(first.cookies())
            .header("X-XSRF-TOKEN", token)
            .when().post("/csrf-test")
            .then()
                .statusCode(200);
    }
}
