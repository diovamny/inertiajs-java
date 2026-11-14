package io.github.diovamny.inertia.core.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Wire metadata of an individual live prop.
 *
 * <p>Contains only the information that travels in the page-object JSON.
 * The resolver (a server-side supplier) is <strong>not</strong> part of this
 * model; it is owned by the adapter's per-request context, keeping wire
 * models free of server logic.</p>
 *
 * @param listeners the channels and events that trigger live updates
 * @param throttle  minimum interval in milliseconds between updates, or
 *                  {@code null} to omit the field from the wire
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LivePropMetadata(
    List<LivePropListener> listeners,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) Integer throttle
) {

    /**
     * Create metadata without throttling.
     *
     * @param listeners the channels and events that trigger live updates
     */
    public LivePropMetadata(List<LivePropListener> listeners) {
        this(listeners, null);
    }
}
