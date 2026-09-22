package io.github.diovamny.inertia.core.ssr;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

/**
 * Classifies SSR sidecar failures into the structured error contract (H13).
 *
 * <p>Every classification carries a machine-readable {@code type} and a
 * human-actionable {@code hint}. The sidecar is a trust boundary: failure
 * details are logged server-side and never rendered to the client (the
 * renderers fall back to client-side rendering with empty SSR placeholders).
 *
 * <p>Types the adapters can determine locally: {@code unreachable} (sidecar
 * not listening / DNS), {@code timeout} (connect or read budget exceeded),
 * {@code error-status} (sidecar answered HTTP 4xx/5xx), {@code unknown}.
 * Sidecar-reported {@code browserApi} / {@code sourceLocation} details are a
 * sidecar-protocol concern and travel inside a successful render payload, not
 * in this classification.
 */
public final class SsrFailureClassifier {

    private SsrFailureClassifier() {
    }

    /**
     * A classified SSR failure.
     *
     * @param type machine-readable failure type
     * @param hint actionable hint for operators
     */
    public record SsrFailure(String type, String hint) {
    }

    /**
     * Classify a thrown failure.
     *
     * @param failure the thrown failure, may be {@code null}
     * @return the classification, never {@code null}
     */
    public static SsrFailure classify(Throwable failure) {
        if (failure == null) {
            return new SsrFailure("unknown", "SSR render failed without an exception; check sidecar logs");
        }
        var current = failure;
        while (current != null) {
            if (current instanceof TimeoutException || current instanceof SocketTimeoutException
                    || current instanceof java.io.InterruptedIOException) {
                return new SsrFailure("timeout",
                    "SSR sidecar exceeded the configured connect/read timeout; raise inertia.ssr-*-timeout or scale the sidecar");
            }
            if (current instanceof ConnectException || current instanceof UnknownHostException
                    || current instanceof java.net.NoRouteToHostException) {
                return new SsrFailure("unreachable",
                    "SSR sidecar is not reachable at the configured inertia.ssr-url; start it or disable inertia.ssr-enabled");
            }
            current = current.getCause();
        }
        return new SsrFailure("unknown",
            "SSR render failed (" + failure.getClass().getSimpleName() + "); check sidecar logs");
    }

    /**
     * Classify a non-2xx sidecar HTTP status.
     *
     * @param status the HTTP status code
     * @return the classification, never {@code null}
     */
    public static SsrFailure classifyHttpStatus(int status) {
        return new SsrFailure("error-status",
            "SSR sidecar answered HTTP " + status + "; the page component or its data threw during render");
    }
}
