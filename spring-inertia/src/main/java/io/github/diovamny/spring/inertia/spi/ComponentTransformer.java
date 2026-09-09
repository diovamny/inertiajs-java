package io.github.diovamny.spring.inertia.spi;

/**
 * Optional hook to transform the component name before it is sent to the
 * client. Implement as a Spring bean to enable.
 */
@FunctionalInterface
public interface ComponentTransformer {

    /**
     * Transform the component name before it is sent to the client.
     *
     * @param component the original component name
     * @return the transformed component name
     */
    String transform(String component);
}
