package io.github.diovamny.quarkus.inertia.protocol;

import io.vertx.ext.web.RoutingContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test-only session holder for propagating routing context to @Blocking
 * worker threads in Quarkus test mode.
 * Populated by InertiaRequestFilter, accessed by AuthService.
 * Auto-clears on JVM shutdown to prevent Vert.x blocked thread warnings.
 */
public final class TestSessionHolder {

    private static final Map<String, RoutingContext> SESSIONS = new ConcurrentHashMap<>();

    // Auto-clear on JVM shutdown to prevent Vert.x blocked thread warnings during teardown
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(TestSessionHolder::clearAll, "inertia-test-session-cleanup"));
    }

    private TestSessionHolder() {}

    public static void put(String sessionId, RoutingContext rc) {
        SESSIONS.put(sessionId, rc);
    }

    public static RoutingContext get(String sessionId) {
        return SESSIONS.get(sessionId);
    }

    public static void remove(String sessionId) {
        SESSIONS.remove(sessionId);
    }

    public static void clearAll() {
        SESSIONS.clear();
    }
}
