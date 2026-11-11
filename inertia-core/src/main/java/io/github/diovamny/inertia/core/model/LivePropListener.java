package io.github.diovamny.inertia.core.model;

import java.util.List;

/**
 * Listener attached to a live-prop channel and the events it reacts to.
 *
 * <p>Pure wire data: it carries only the channel and event names consumed by
 * the client. Server-side resolvers never live here.</p>
 *
 * @param channel the broadcast channel
 * @param events  the event names that trigger a live update
 */
public record LivePropListener(LiveChannel channel, List<String> events) {

    /**
     * Convenience constructor with varargs events.
     *
     * @param channel the broadcast channel
     * @param events  the event names that trigger a live update
     */
    public LivePropListener(LiveChannel channel, String... events) {
        this(channel, List.of(events));
    }
}
