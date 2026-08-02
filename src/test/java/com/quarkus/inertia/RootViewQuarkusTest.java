package com.quarkus.inertia;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class RootViewQuarkusTest {

    @Test
    void setRootViewRendersAlternateTemplate() {
        var html = given()
            .when().get("/root-view")
            .then()
                .statusCode(200)
                .extract().asString();
        assertThat(html).contains("ALT-ROOT-MARKER");
    }

    @Test
    void rootViewOverrideIsPerRequest() {
        var html = given()
            .when().get("/root-view/default")
            .then()
                .statusCode(200)
                .extract().asString();
        assertThat(html).doesNotContain("ALT-ROOT-MARKER");
        assertThat(html).contains("Inertia App");
    }
}
