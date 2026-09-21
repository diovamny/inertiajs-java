package io.github.diovamny.spring.inertia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.github.diovamny.inertia.tck.InertiaTckRunner;

/**
 * G-01: certifies the Spring MVC stack against the normative protocol suite.
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=true",
    "inertia.ssr-enabled=false"
})
class TckSpringTest {

    @LocalServerPort
    private int port;

    @Test
    void normativeProtocolSuiteIsGreen() {
        InertiaTckRunner.assertGreen("http://localhost:" + port, "spring");
    }
}
