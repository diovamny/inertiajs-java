package ${package};

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class WelcomeControllerTest {

    @Test
    void initialVisitReturnsHtmlShell() {
        given()
            .when().get("/")
            .then()
            .statusCode(200)
            .body(containsString("id=\"app\""));
    }

    @Test
    void inertiaVisitReturnsJsonPageObject() {
        given()
            .header("X-Inertia", "true")
            .when().get("/")
            .then()
            .statusCode(200)
            .header("X-Inertia", equalTo("true"));
    }
}
