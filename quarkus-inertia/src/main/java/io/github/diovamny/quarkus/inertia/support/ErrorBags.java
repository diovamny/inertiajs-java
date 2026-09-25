package io.github.diovamny.quarkus.inertia.support;

import io.vertx.core.Vertx;

import io.github.diovamny.quarkus.inertia.protocol.InertiaContextLocals;

/**
 * Named error-bag wrapping for manually flashed validation errors (Fase E).
 *
 * <p>Same contract as the Spring twin: the validation-exception path already
 * wraps flashed errors under the {@code X-Inertia-Error-Bag} target, but the
 * manual builders flashed raw field errors and silently dropped the bag.
 * Every builder now routes through {@link #wrap(Object)} so both paths honor
 * {@code errors: {<bag>: {...}}} with a bag and flat {@code errors} without
 * one.</p>
 */
public final class ErrorBags {

    /**
     * Per-request key carrying the {@code X-Inertia-Error-Bag} target. Kept
     * as a literal (instead of referencing the protocol extractor) so this
     * leaf package never cycles back into the protocol layer.
     */
    static final String ERROR_BAG_KEY = "inertia-error-bag";

    private ErrorBags() {
    }

    /**
     * Wrap raw field errors under the current request's error bag, if any.
     *
     * @param errors field-to-message map (or any prebuilt errors object)
     * @return {@code Map.of(bag, errors)} when the request targets a named
     *         bag, otherwise {@code errors} unchanged
     */
    public static Object wrap(Object errors) {
        return wrap(errors, currentErrorBag());
    }

    /**
     * Wrap raw field errors under an explicit bag name.
     *
     * @param errors field-to-message map (or any prebuilt errors object)
     * @param bag    the bag name; blank or {@code null} disables wrapping
     * @return {@code Map.of(bag, errors)} when a bag is given, otherwise
     *         {@code errors} unchanged
     */
    public static Object wrap(Object errors, String bag) {
        // Bag namespacing lives in inertia-core (ErrorWire); this class only
        // resolves the current request's bag target.
        return io.github.diovamny.inertia.core.protocol.ErrorWire.wrapBag(errors, bag);
    }

    private static String currentErrorBag() {
        try {
            return InertiaContextLocals.getString(Vertx.currentContext(), ERROR_BAG_KEY);
        } catch (Exception ignored) {
            // No Vert.x context (plain unit tests): no bag, no wrapping.
            return null;
        }
    }
}
