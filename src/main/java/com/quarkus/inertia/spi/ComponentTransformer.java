package com.quarkus.inertia.spi;

/**
 * Optional hook to transform the component name before it is sent to the
 * client. Implement as a CDI bean to enable.
 */
@FunctionalInterface
public interface ComponentTransformer {
    String transform(String component);
}
