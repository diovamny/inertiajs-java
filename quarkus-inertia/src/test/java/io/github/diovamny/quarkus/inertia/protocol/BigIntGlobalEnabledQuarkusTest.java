package io.github.diovamny.quarkus.inertia.protocol;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.util.Map;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

/**
 * Global {@code inertia.bigint-enabled=true}: pages opt out per response via
 * {@code inertia.preserveBigIntegers(false)} and fall back to the plain wire.
 */
@QuarkusTest
@TestProfile(BigIntGlobalEnabledQuarkusTest.GlobalOnProfile.class)
class BigIntGlobalEnabledQuarkusTest {

    public static class GlobalOnProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("inertia.bigint-enabled", "true");
        }
    }

    @Test
    void globalFlagMarksUnsafeLongsByDefault() {
        given()
            .header("X-Inertia", "true")
            .when().get("/bigint-test")
            .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body(containsString("\"id\":{\"$bigint\":\"9223372036854775807\"}"))
                .body(containsString("\"preserveBigIntegers\":true"))
                .body(containsString("\"safe\":42"));
    }

    @Test
    void facadeOptOutRestoresPlainWireForTheResponse() {
        given()
            .header("X-Inertia", "true")
            .when().get("/bigint-test/opt-out")
            .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body(containsString("\"id\":9223372036854775807"))
                .body(not(containsString("$bigint")))
                .body(not(containsString("preserveBigIntegers")));
    }
}
