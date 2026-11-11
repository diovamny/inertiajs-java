package io.github.diovamny.inertia.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Broadcast channel of a live prop.
 *
 * <p>Pure wire data: it carries only the channel name and type consumed by
 * the client. Server-side resolvers never live here.</p>
 *
 * @param name the channel name (for example {@code orders.1})
 * @param type the channel visibility: {@code public}, {@code private} or
 *             {@code presence}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LiveChannel(String name, String type) {

    /**
     * Create a public channel.
     *
     * @param name the channel name
     * @return the public channel
     */
    public static LiveChannel publicChannel(String name) {
        return new LiveChannel(name, "public");
    }

    /**
     * Create a private channel.
     *
     * @param name the channel name
     * @return the private channel
     */
    public static LiveChannel privateChannel(String name) {
        return new LiveChannel(name, "private");
    }

    /**
     * Create a presence channel.
     *
     * @param name the channel name
     * @return the presence channel
     */
    public static LiveChannel presence(String name) {
        return new LiveChannel(name, "presence");
    }
}
