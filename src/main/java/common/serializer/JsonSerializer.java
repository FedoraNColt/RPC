package common.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import common.message.RPCRequest;
import common.message.RPCResponse;

public class JsonSerializer implements Serializer {

    /**
     * Serializes an object into a byte array using FastJSON.
     *
     * @param obj The object to be serialized.
     * @return A byte array representing the serialized object.
     */
    @Override
    public byte[] serialize(Object obj) {
        return JSONObject.toJSONBytes(obj);
    }

    /**
     * Deserializes a byte array into a Java object based on the message type.
     *
     * @param bytes The serialized byte array.
     * @param messageType The type of message being deserialized:
     *                    - 0: RPC request
     *                    - 1: RPC response
     * @return The deserialized Java object.
     */
    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;

        switch (messageType) {
            case 0:
                // Convert the byte array into an RPCRequest object
                RPCRequest rpcRequest = JSON.parseObject(bytes, RPCRequest.class);

                // Create an array to store the deserialized request parameters
                Object[] objs = new Object[rpcRequest.getParamTypes().length];

                // Convert parameters to their expected types
                for (int i = 0; i < objs.length; i++) {
                    Class<?> paramType = rpcRequest.getParamTypes()[i]; // Target parameter type
                    Object param = rpcRequest.getParams()[i]; // Actual parameter value

                    // If the parameter type is not compatible, use FastJSON for conversion
                    if (!paramType.isAssignableFrom(param.getClass())) {
                        objs[i] = JSONObject.toJavaObject((JSONObject) param, paramType);
                    } else {
                        objs[i] = param;
                    }
                }

                // Set the converted parameters in the request object
                rpcRequest.setParams(objs);
                obj = rpcRequest;
                break;

            case 1:
                // Convert the byte array into an RPCResponse object
                RPCResponse rpcResponse = JSON.parseObject(bytes, RPCResponse.class);

                // Retrieve the expected return data type
                Class<?> dataType = rpcResponse.getDataType();
                Object data = rpcResponse.getData();

                // If the data type is not compatible, use FastJSON for conversion
                if (!dataType.isAssignableFrom(data.getClass())) {
                    rpcResponse.setData(JSONObject.toJavaObject((JSONObject) data, dataType));
                }

                obj = rpcResponse;
                break;

            default:
                throw new RuntimeException("This type of message is not supported");
        }

        return obj;
    }

    /**
     * Returns the type of serializer.
     *
     * @return Serializer type identifier (1 for JSON serialization).
     */
    @Override
    public int getType() {
        return 1;
    }
}