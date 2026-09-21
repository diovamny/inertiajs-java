package io.github.diovamny.inertia.core.spi;

/**
 * Optional hook to resolve the page URL before it is sent to the client.
   * Register an implementation with the adapter to enable.
 */
@FunctionalInterface
public interface UrlResolver {

    /**
     * Resolve the page URL before it is sent to the client.
     *
     * @param url the original URL
     * @return the resolved URL
     */
    String resolve(String url);
}
