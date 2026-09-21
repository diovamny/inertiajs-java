package io.github.diovamny.quarkus.inertia.head;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.Map;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

/**
 * G-06: server head tags are published as the {@code head} page prop when
 * {@code inertia.server-head=true}.
 */
@QuarkusTest
@TestProfile(HeadQuarkusTest.ServerHeadProfile.class)
class HeadQuarkusTest {

    public static class ServerHeadProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                "inertia.server-head", "true",
                "inertia.meta-title-template", "%s | App");
        }
    }

    @Test
    void serverHeadTagsArePublishedAsHeadProp() {
        given()
            .header("X-Inertia", "true")
            .when().get("/head-test")
            .then()
                .statusCode(200)
                .body("component", equalTo("HeadPage"))
                .body("props.head", hasSize(3))
                .body("props.head[0].key", equalTo("title"))
                .body("props.head[0].attributes.text", equalTo("Hi | App"))
                .body("props.head[1].key", equalTo("meta-description"))
                .body("props.head[2].key", equalTo("link-canonical"));
    }
}
