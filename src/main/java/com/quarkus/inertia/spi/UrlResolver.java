package com.quarkus.inertia.spi;

/**
 * Optional hook to resolve the page URL before it is sent to the client.
 * Implement as a CDI bean to enable.
 */
@FunctionalInterface
public interface UrlResolver {
    String resolve(String url);
}
