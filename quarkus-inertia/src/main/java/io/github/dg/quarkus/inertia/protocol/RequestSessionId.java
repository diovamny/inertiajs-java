package io.github.dg.quarkus.inertia.protocol;

import jakarta.enterprise.context.RequestScoped;

/**
 * Request-scoped holder for the session ID.
 * Quarkus propagates request-scoped beans to {@code @Blocking} worker threads.
 */
@RequestScoped
public class RequestSessionId {

    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean hasSessionId() {
        return sessionId != null && !sessionId.isBlank();
    }
}
