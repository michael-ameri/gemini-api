package swiss.ameri.gemini.spi;

/**
 * Used to (un-) marshal java objects (mainly {@code record}s) to JSON Strings.
 * To keep this library dependency free, no implementation is provided directly.
 * {@code swiss.ameri:gemini-gson} provides an example implementation using a gson dependency.
 */
public interface JsonParser {

    /**
     * This method serializes the specified object into its equivalent JSON representation.
     */
    String toJson(Object object);

    /**
     * This method deserializes the specified JSON into an object of the specified class.
     */
    <T> T fromJson(String json, Class<T> clazz);

}
