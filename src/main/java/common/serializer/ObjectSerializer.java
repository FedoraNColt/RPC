package common.serializer;

import java.io.*;

public class ObjectSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        byte[] bytes = null;
        // Create an in-memory output stream to store the serialized byte data
        // ByteArrayOutputStream is a dynamically resizable byte buffer where serialized data will be written
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // Convert the object into a binary stream and write it to the byte buffer `bos`
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            // Write the object to the output stream, triggering serialization
            oos.writeObject(obj);
            // Force all buffered data to be written into `bos`
            oos.flush();
            // Convert the buffered content into a byte array
            bytes = bos.toByteArray();
            // Close the streams to free resources
            oos.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        // Wrap the byte array into an input stream
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            // Use ObjectInputStream to wrap the ByteArrayInputStream
            ObjectInputStream ois = new ObjectInputStream(bis);
            // Read the serialized object from `ois` and deserialize it back into a Java object
            obj = ois.readObject();
            // Close the streams to free resources
            ois.close();
            bis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public int getType() {
        return 0;
    }
}
