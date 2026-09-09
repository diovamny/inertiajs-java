package io.github.dg.quarkus.inertia.protocol;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class EtagQuarkusTest {

    @Test
    void inertiaResponseCarriesEtag() {
        var etag = given()
            .header("X-Inertia", "true")
            .when().get("/root-view/default")
            .then()
                .statusCode(200)
                .header("Vary", org.hamcrest.Matchers.containsString("X-Inertia"))
                .extract().header("ETag");

        assertThat(etag).isNotBlank();
    }

    @Test
    void matchingIfNoneMatchReturns304() {
        var headers = given()
            .header("X-Inertia", "true")
            .when().get("/root-view/default")
            .then()
                .statusCode(200)
                .extract().headers();

        var etag = headers.getValue("ETag");

        given()
            .header("X-Inertia", "true")
            .header("If-None-Match", etag)
            .when().get("/root-view/default")
            .then()
                .statusCode(304);
    }
}
