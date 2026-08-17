package io.github.dg.spring.inertia.spi;

/**
 * Abstraction over the JSON serializer used for the page object payload
 * (and for props wrapped with {@code inertia.rawJson(...)}).
 *
 * <p>The default implementation delegates to the Jackson {@code ObjectMapper}
 * managed by Spring Boot; implement this interface as a Spring bean to use a
 * different serializer.</p>
 */
public interface JsonProvider {

    /**
     * Serialize a value to a JSON string.
     *
     * @param value the value to serialize
     * @return the JSON document
     */
    String toJson(Object value);

    /**
     * Deserialize a JSON string into a value of the given type.
     *
     * @param json the JSON document
     * @param type the target type
     * @param <T>  the target type parameter
     * @return the deserialized value
     */
    <T> T fromJson(String json, Class<T> type);
}