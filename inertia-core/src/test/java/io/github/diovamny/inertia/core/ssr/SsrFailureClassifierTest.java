package io.github.diovamny.inertia.core.ssr;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * H13: SSR failures classify into the structured error contract
 * (type + actionable hint, never leaked to the client).
 */
class SsrFailureClassifierTest {

    @Test
    void connectionRefusedIsUnreachable() {
        var failure = SsrFailureClassifier.classify(new ConnectException("Connection refused"));
        assertEquals("unreachable", failure.type());
        assertTrue(failure.hint().contains("inertia.ssr-url"));
    }

    @Test
    void unknownHostIsUnreachable() {
        var failure = SsrFailureClassifier.classify(new UnknownHostException("ssr.local"));
        assertEquals("unreachable", failure.type());
    }

    @Test
    void timeoutSurfacesThroughWrappers() {
        var wrapped = new RuntimeException("request failed",
            new java.io.InterruptedIOException("timeout"));
        assertEquals("timeout", SsrFailureClassifier.classify(wrapped).type());
        assertEquals("timeout",
            SsrFailureClassifier.classify(new TimeoutException()).type());
        assertEquals("timeout",
            SsrFailureClassifier.classify(new SocketTimeoutException("Read timed out")).type());
    }

    @Test
    void errorStatusCarriesTheCode() {
        var failure = SsrFailureClassifier.classifyHttpStatus(500);
        assertEquals("error-status", failure.type());
        assertTrue(failure.hint().contains("500"));
    }

    @Test
    void unknownFailuresStayStructured() {
        var failure = SsrFailureClassifier.classify(new IllegalStateException("boom"));
        assertEquals("unknown", failure.type());
        assertTrue(failure.hint().contains("IllegalStateException"));
        assertNotNull(SsrFailureClassifier.classify(null).type());
    }
}
