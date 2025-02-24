package common.serializer;

/**
 * Interface for providing serialization and deserialization functionality for objects
 * Includes a static factory method to return a specific serializer instance based on a type code
 */
public interface Serializer {
    /**
     * Serializes an object into a byte array.
     *
     * @param obj The object to be serialized.
     * @return A byte array representing the serialized object.
     */
    byte[] serialize(Object obj);

    /**
     * Deserializes a byte array back into a Java object.
     *
     * @param bytes The byte array to be deserialized.
     * @param messageType The message type, which may be needed for certain deserialization formats.
     *                    - If using Java's built-in serialization, `messageType` is not required.
     *                    - Other serialization methods may require `messageType` to reconstruct the object.
     * @return The deserialized Java object.
     */
    Object deserialize(byte[] bytes, int messageType);

    /**
     * Returns the type of serializer being used.
     *
     * @return The serializer type:
     *         - 0: Java's built-in serialization.
     *         - 1: JSON serialization.
     */
    int getType();

    /**
     * Static factory method for retrieving a serializer instance based on a type code.
     *
     * @param code The serialization method identifier.
     * @return A specific serializer instance:
     *         - 0 returns an ObjectSerializer (Java built-in serialization).
     *         - 1 returns a JsonSerializer (JSON-based serialization).
     *         - Returns `null` if the code does not match any existing serializer.
     */
    static Serializer getSerializerByCode(int code) {
        switch (code) {
            case 0:
                return new ObjectSerializer();
            case 1:
                return new JsonSerializer();
            default:
                return null;
        }
    }
}
