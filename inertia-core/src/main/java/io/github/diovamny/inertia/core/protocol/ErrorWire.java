package io.github.diovamny.inertia.core.protocol;

import java.util.Map;

/**
 * Validation-error wire shape (protocol: validation, error bags).
 *
 * <p>Requests may scope errors to a named error bag
 * ({@code X-Inertia-Error-Bag}); the server namespaces the errors under that
 * bag so multiple forms keep separate errors. The per-field shape (single
 * message vs. message array) is decided by
 * {@code ValidationErrors.toWireMap(boolean)}; only the bag namespacing
 * lives here.</p>
 */
public final class ErrorWire {

    private ErrorWire() {
    }

    /**
     * Namespace wire errors under an error bag.
     *
     * @param wire the per-field errors (map or any prebuilt errors object)
     * @param bag the bag name from {@code X-Inertia-Error-Bag}, if any
     * @return the wire value unchanged for a blank bag (or {@code null}
     *         wire), else namespaced as a single-entry map
     */
    public static Object wrapBag(Object wire, String bag) {
        if (wire == null || bag == null || bag.isBlank()) {
            return wire;
        }
        return Map.of(bag, wire);
    }
}
