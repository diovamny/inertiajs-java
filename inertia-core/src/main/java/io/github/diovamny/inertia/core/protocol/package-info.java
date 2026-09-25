/**
 * Framework-agnostic Inertia.js v3 protocol decisions shared by the Spring
 * and Quarkus adapters.
 *
 * <p>Every class here is pure (JDK types plus {@code model} in, plain values
 * out): request/response/session handling stays in the adapters, which pass
 * already-extracted booleans, header values and prop maps. Deliberately
 * <em>not</em> unified: per-framework URL identity ({@code isExternal}),
 * null-method handling and once-TTL clocks, whose divergences are pinned by
 * adapter tests.</p>
 */
package io.github.diovamny.inertia.core.protocol;
