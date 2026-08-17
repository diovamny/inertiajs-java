package io.github.dg.quarkus.inertia.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Entry of the {@code onceProps} metadata sent to the client.
 *
 * <p>The client uses it to track props that should only be shown once (e.g.
 * flash notifications): when the pair appears in the client's history
 * state, the server omits the prop on subsequent visits. {@code expiresAt}
 * (epoch milliseconds) makes the client forget the entry after a deadline.</p>
 *
 * @param prop      the once-prop name
 * @param expiresAt expiry as epoch milliseconds, or {@code null} if the
 *                  entry never expires
 */
@RegisterForReflection
public record OnceProp(String prop, Long expiresAt) {}