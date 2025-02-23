package Client;

import common.message.RPCRequest;
import common.message.RPCResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class IOClient {
    public static RPCResponse sendRequest(String host, int port, RPCRequest rpcRequest) {
        try {
            // Establish TCP connection with the server via socket
            Socket socket = new Socket(host, port);
            // Serialize and send the object
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            // Receive and deserialize the object
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            // Serialize the rpcRequest Object and send it to the server by the output stream
            oos.writeObject(rpcRequest);
            // Refresh the output stream to make sure the object is sent
            oos.flush();

            // Read the serialized response object from the input stream and deserialize it to RPCResponse Object
            return (RPCResponse) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
