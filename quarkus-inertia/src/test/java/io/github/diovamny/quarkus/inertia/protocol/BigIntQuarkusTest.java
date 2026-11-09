package io.github.diovamny.quarkus.inertia.protocol;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Per-response BigInt opt-in with the global {@code inertia.bigint-enabled}
 * flag disabled: unsafe longs must travel as {@code {"$bigint":"..."}}
 * markers and the page must carry {@code preserveBigIntegers: true}.
 */
@QuarkusTest
class BigIntQuarkusTest {

    @Test
    void defaultStaysPlainWhenGlobalDisabled() {
        given()
            .header("X-Inertia", "true")
            .when().get("/bigint-test")
            .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body(containsString("\"id\":9223372036854775807"))
                .body(not(containsString("$bigint")))
                .body(not(containsString("preserveBigIntegers")));
    }

    @Test
    void facadeOptInMarksUnsafeLongsAndStampsPageFlag() {
        given()
            .header("X-Inertia", "true")
            .when().get("/bigint-test/opt-in")
            .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body(containsString("\"id\":{\"$bigint\":\"9223372036854775807\"}"))
                .body(containsString("\"preserveBigIntegers\":true"))
                .body(containsString("\"safe\":42"));
    }
}
