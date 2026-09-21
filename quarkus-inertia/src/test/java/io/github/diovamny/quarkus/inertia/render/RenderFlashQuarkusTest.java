package io.github.diovamny.quarkus.inertia.render;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class RenderFlashQuarkusTest {

    @Test
    void renderFlashSurvivesIntoNextPage() {
        var render = given()
            .header("X-Inertia", "true")
            .when().get("/render-flash-test/render")
            .then()
                .statusCode(200)
                .extract();

        given()
            .cookies(render.cookies())
            .header("X-Inertia", "true")
            .when().get("/render-flash-test/show")
            .then()
                .statusCode(200)
                .body("props.message", equalTo("Chained"));
    }
}
