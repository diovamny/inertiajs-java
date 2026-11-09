package io.github.diovamny.quarkus.inertia.session;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;
import io.github.diovamny.quarkus.inertia.config.InertiaConfigValidator;

@QuarkusTest
@TestProfile(InertiaSessionDisabledTest.SessionOffProfile.class)
class InertiaSessionDisabledTest {

    public static class SessionOffProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("inertia.session.enabled", "false");
        }
    }

    @Inject
    InertiaConfig config;

    @Inject
    Event<StartupEvent> startupEvent;

    @Test
    void warnsWhenSessionDisabled() {
        assertThat(config.sessionEnabled()).isFalse();
        var records = new ArrayList<LogRecord>();
        var logger = org.jboss.logmanager.Logger.getLogger(InertiaConfigValidator.class.getName());
        var handler = new Handler() {
            @Override public void publish(LogRecord record) { records.add(record); }
            @Override public void flush() { }
            @Override public void close() { }
        };
        logger.addHandler(handler);
        try {
            startupEvent.fire(new StartupEvent());
        } finally {
            logger.removeHandler(handler);
        }
        assertThat(records)
            .anyMatch(r -> r.getLevel().intValue() == java.util.logging.Level.WARNING.intValue()
                && r.getMessage().contains("inertia.session.enabled=false"));
    }

    @Test
    void noSessionHandlerRegistered() {
        var response = given()
            .when().get("/csrf-test")
            .then()
                .statusCode(200)
                .extract();
        assertThat(response.cookie("vertx-web.session")).isNull();
        assertThat(response.cookie("XSRF-TOKEN")).isNull();
    }
}
