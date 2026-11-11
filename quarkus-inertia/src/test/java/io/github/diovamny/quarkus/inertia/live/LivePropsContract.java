package io.github.diovamny.quarkus.inertia.live;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Transport-neutral live-prop contract: every assertion runs against whatever
 * base path the concrete transport provides (JAX-RS or Vert.x reactive routes).
 *
 * <p>Mirrors the Spring Phase 3 tests A-E and adds the throttle contract.</p>
 */
abstract class LivePropsContract {

    abstract String basePath();

    abstract void resetCounters();

    abstract int orderResolved();

    abstract int deferredResolved();

    @BeforeEach
    void reset() {
        resetCounters();
    }

    @Test
    void fullVisitResolvesLivePropAndEmitsMetadata() {
        given()
            .header("X-Inertia", "true")
        .when()
            .get(basePath() + "/order")
        .then()
            .statusCode(200)
            .body("component", equalTo("Live/Show"))
            .body("props.order.id", equalTo(1))
            .body("props.order.status", equalTo("shipped"))
            .body("props.order.revision", equalTo(1))
            .body("liveProps.order.listeners[0].channel.name", equalTo("orders.1"))
            .body("liveProps.order.listeners[0].channel.type", equalTo("private"))
            .body("liveProps.order.listeners[0].events[0]", equalTo("OrderUpdated"));

        assertThat(orderResolved()).isEqualTo(1);
    }

    @Test
    void htmlVisitEmbedsResolvedLivePropAndMetadata() {
        given()
        .when()
            .get(basePath() + "/order")
        .then()
            .statusCode(200)
            .contentType(containsString("text/html"))
            .body(containsString("<div id=\"app\""))
            .body(containsString("\"liveProps\""))
            .body(containsString("\"orders.1\""))
            .body(containsString("\"revision\":1"));

        assertThat(orderResolved()).isEqualTo(1);
    }

    @Test
    void partialReloadOfLivePropResolvesItAgain() {
        given()
            .header("X-Inertia", "true")
            .header("X-Inertia-Partial-Component", "Live/Show")
            .header("X-Inertia-Partial-Data", "order")
        .when()
            .get(basePath() + "/order")
        .then()
            .statusCode(200)
            .body("props.order.revision", equalTo(1))
            .body("props", not(hasKey("other")))
            .body("liveProps.order.listeners[0].channel.name", equalTo("orders.1"));

        assertThat(orderResolved()).isEqualTo(1);
    }

    @Test
    void foreignPartialReloadNeverRunsTheLiveResolver() {
        given()
            .header("X-Inertia", "true")
            .header("X-Inertia-Partial-Component", "Live/Show")
            .header("X-Inertia-Partial-Data", "other")
        .when()
            .get(basePath() + "/order")
        .then()
            .statusCode(200)
            .body("props.other", equalTo("o"))
            .body("props", not(hasKey("order")))
            .body("liveProps.order.listeners[0].channel.name", equalTo("orders.1"));

        assertThat(orderResolved()).isZero();
    }

    @Test
    void deferCompositionEmitsBothMetadataSectionsWithoutResolving() {
        given()
            .header("X-Inertia", "true")
        .when()
            .get(basePath() + "/order-defer")
        .then()
            .statusCode(200)
            .body("deferredProps.default", hasItem("stats"))
            .body("liveProps.stats.listeners[0].channel.name", equalTo("stats"))
            .body("liveProps.stats.listeners[0].channel.type", equalTo("public"))
            .body("liveProps.stats.listeners[0].events[0]", equalTo("StatsUpdated"))
            .body("props", not(hasKey("stats")));

        assertThat(deferredResolved()).isZero();
    }

    @Test
    void deferCompositionResolvesOnSelectedPartialReload() {
        given()
            .header("X-Inertia", "true")
            .header("X-Inertia-Partial-Component", "Live/Show")
            .header("X-Inertia-Partial-Data", "stats")
        .when()
            .get(basePath() + "/order-defer")
        .then()
            .statusCode(200)
            .body("props.stats.total", equalTo(42))
            .body("liveProps.stats.listeners[0].channel.name", equalTo("stats"));

        assertThat(deferredResolved()).isEqualTo(1);
    }

    @Test
    void throttleIsEmittedOnlyWhenConfigured() {
        given()
            .header("X-Inertia", "true")
        .when()
            .get(basePath() + "/throttle")
        .then()
            .statusCode(200)
            .body("liveProps.poll.throttle", equalTo(2000))
            .body("liveProps.poll.listeners[0].channel.name", equalTo("poll"))
            .body("liveProps.poll.listeners[0].channel.type", equalTo("public"))
            .body("props.poll", equalTo("p"));

        given()
            .header("X-Inertia", "true")
        .when()
            .get(basePath() + "/order")
        .then()
            .statusCode(200)
            .body("liveProps.order.throttle", nullValue());
    }
}
