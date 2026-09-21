package io.github.diovamny.inertia.core.spi;

/**
 * Supplies the per-request Content-Security-Policy nonce stamped onto the
 * Inertia bootstrap payload.
 *
 * <h2>Why no nonce is needed by default</h2>
 * <p>The default bootstrap ({@code <div id="app" data-page="...">}, like
 * Laravel's) carries the page object as inert markup: the browser never
 * executes it, so a strict {@code script-src} policy without
 * {@code 'unsafe-inline'} stays green with no nonce. The JSON companion tag
 * ({@code <script type="application/json">}) is data, not executable script,
 * for the same reason.</p>
 *
 * <p>A nonce becomes necessary only when the application serves executable
 * inline scripts (its own bundles, analytics snippets) or switches the
 * bootstrap to a script-evaluated element. In that case the application owns
 * the CSP filter and the per-request secret; it exposes the current value
 * through this SPI and the renderer replaces the
 * {@code __INERTIA_CSP_NONCE__} placeholder with {@code nonce="..."}
 * (or an empty string when absent).</p>
 *
 * <p>No bean, no nonce: when no {@code NonceProvider} is registered the
 * output is byte-identical to a build without CSP support.</p>
 */
public interface NonceProvider {

    /**
     * The CSP nonce for the current request, or {@code null}/blank when none
     * applies (the renderer then emits no {@code nonce} attribute).
     *
     * @return the nonce, or {@code null}
     */
    String nonce();
}
