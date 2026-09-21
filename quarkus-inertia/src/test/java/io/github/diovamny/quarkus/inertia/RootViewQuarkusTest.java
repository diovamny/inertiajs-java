package io.github.diovamny.quarkus.inertia;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

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
    void nonceProviderStampsScriptTag() {
        var html = given()
            .when().get("/root-view/nonce")
            .then()
                .statusCode(200)
                .extract().asString();
        assertThat(html).contains("NONCE-ROOT-MARKER");
        assertThat(html).contains("nonce=\"test-nonce-123\"");
    }

    @Test
    void pagesWithoutNonceProviderStayUnchanged() {
        var html = given()
            .when().get("/root-view/default")
            .then()
                .statusCode(200)
                .extract().asString();
        assertThat(html).doesNotContain("nonce=");
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
