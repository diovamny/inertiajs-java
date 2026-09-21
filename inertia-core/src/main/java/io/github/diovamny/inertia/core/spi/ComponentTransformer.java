package io.github.diovamny.inertia.core.spi;

/**
 * Optional hook to transform the component name before it is sent to the
   * client. Register an implementation with the adapter to enable.
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
