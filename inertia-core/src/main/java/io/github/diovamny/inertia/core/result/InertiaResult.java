package io.github.diovamny.inertia.core.result;

/**
 * Strongly typed Inertia outcome, replacing untyped {@code Object} /
 * {@code Uni<Object>} controller returns.
 *
 * <p>Adapters accept these values through typed overloads
 * ({@code render}/{@code redirect}/{@code location}) and serve them through
 * the regular pipeline; the legacy untyped overloads stay available for
 * backward compatibility.</p>
 */
public sealed interface InertiaResult
        permits InertiaPageResult, InertiaRedirectResult, InertiaLocationResult {
}
