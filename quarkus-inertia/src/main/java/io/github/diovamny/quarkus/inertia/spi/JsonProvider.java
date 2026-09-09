package io.github.diovamny.quarkus.inertia.spi;

/**
 * Abstraction over the JSON serializer used for the page object payload
 * (and for props wrapped with {@code inertia.rawJson(...)}).
 *
 * <p>The default implementation delegates to the Jackson ObjectMapper tied
 * to the current request; implement this interface as a CDI bean to use a
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
