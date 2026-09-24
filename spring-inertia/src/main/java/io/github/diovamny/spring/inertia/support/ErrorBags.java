package io.github.diovamny.spring.inertia.support;

import java.util.Map;

import io.github.diovamny.spring.inertia.internal.InertiaRequestContext;

/**
 * Named error-bag wrapping for manually flashed validation errors (Fase E).
 *
 * <p>The validation-exception path ({@code PrecognitionHandler}) already
 * wraps flashed errors under the {@code X-Inertia-Error-Bag} target, but the
 * manual builders ({@code back().withErrors(...)},
 * {@code render(...).withErrors(...)}) flashed raw field errors and silently
 * dropped the bag. Every builder now routes through {@link #wrap(Object)} so
 * both paths honor the same contract: with a bag, the page receives
 * {@code errors: {<bag>: {...}}}; without one, {@code errors} stays flat.</p>
 */
public final class ErrorBags {

    /**
     * Request attribute carrying the {@code X-Inertia-Error-Bag} target. Kept
     * as a literal (instead of referencing the protocol extractor) so this
     * leaf package never cycles back into the protocol layer; it mirrors
     * {@code InertiaHeaderExtractor#CONTEXT_ERROR_BAG}.
     */
    static final String ERROR_BAG_ATTRIBUTE = "inertia-error-bag";

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
        if (errors == null || bag == null || bag.isBlank()) {
            return errors;
        }
        return Map.of(bag, errors);
    }

    private static String currentErrorBag() {
        var bag = InertiaRequestContext.get(ERROR_BAG_ATTRIBUTE);
        return bag != null ? String.valueOf(bag) : null;
    }
}
