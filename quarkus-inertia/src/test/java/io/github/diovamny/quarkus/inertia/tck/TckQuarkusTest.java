package io.github.diovamny.quarkus.inertia.tck;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

import io.github.diovamny.inertia.tck.InertiaTckRunner;

/**
 * G-01: certifies the Quarkus JAX-RS stack against the normative protocol suite.
 */
@QuarkusTest
class TckQuarkusTest {

    @Test
    void normativeProtocolSuiteIsGreen() {
        InertiaTckRunner.assertGreen(
            "http://localhost:" + io.restassured.RestAssured.port, "quarkus");
    }
}
