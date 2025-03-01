package common.codec;

import common.message.MessageType;
import common.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Reads the message type (request or response)
        short messageType = in.readShort();
        // Validates if the message type is supported (REQUEST or RESPONSE)
        if (messageType != MessageType.REQUEST.getCode() && messageType != MessageType.RESPONSE.getCode()) {
            System.out.println("This type of message is not supported");
        }

        Short serializerType = in.readShort();
        // Retrieves the appropriate serializer instance based on the identifier
        Serializer serializer = Serializer.getSerializerByCode(serializerType);
        if (serializer == null) {
            throw new Exception("Serializer not found");
        }

        // Reads the length of the serialized data
        int length = in.readInt();
        byte[] bytes = new byte[length];
        // Reads the serialized bytes into the allocated array
        in.readBytes(bytes);
        System.out.println("bytes = " + new String(bytes));
        // Deserializes the byte array into a Java object
        Object deserialized = serializer.deserialize(bytes, messageType);
        // Adds the deserialized object to the output list for further processing in the pipeline
        out.add(deserialized);
    }
}
