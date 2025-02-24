package common.codec;

import common.message.MessageType;
import common.message.RPCRequest;
import common.message.RPCResponse;
import common.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

@AllArgsConstructor
// MessageToByteEncoder is a Netty-provided abstract class to simplify encoder implementations,
// allowing to efficiently convert Java objects into byte data
public class MessageEncoder extends MessageToByteEncoder {
    private Serializer serializer;

    /**
     * This method is automatically called by Netty when writing data, converting Java objects into binary format
     *
     * @param ctx  The Netty-provided context object, which contains channel and handler-related information
     * @param msg  The message object to be encoded
     * @param out  The ByteBuf buffer where the encoded byte data will be written
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        System.out.println(msg.getClass());
        if (msg instanceof RPCRequest) {
            out.writeShort(MessageType.REQUEST.getCode());
        } else if (msg instanceof RPCResponse) {
            out.writeShort(MessageType.RESPONSE.getCode());
        }

        out.writeShort(serializer.getType());
        byte[] serializedBytes = serializer.serialize(msg);
        out.writeInt(serializedBytes.length);
        out.writeBytes(serializedBytes);
    }
}
