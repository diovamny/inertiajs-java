package io.github.diovamny.quarkus.inertia.security;

import java.util.Map;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

import static io.restassured.RestAssured.given;

/**
 * §3.17 scenario 11 (Quarkus side): pages beyond
 * {@code inertia.max-page-bytes} fail closed with {@code 413}.
 */
@QuarkusTest
@TestProfile(OversizedPageProfileTest.MaxPageProfile.class)
class OversizedPageProfileTest {

    public static class MaxPageProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("inertia.max-page-bytes", "1024");
        }
    }

    @Test
    void oversizedJsonVisitIs413() {
        given().header("X-Inertia", "true")
            .when().get("/tck/big-page")
            .then().statusCode(413);
    }

    @Test
    void oversizedHtmlVisitIs413() {
        given()
            .when().get("/tck/big-page")
            .then().statusCode(413);
    }

    @Test
    void smallPageStillPassesUnderCap() {
        given().header("X-Inertia", "true")
            .when().get("/tck/page")
            .then().statusCode(200);
    }
}
