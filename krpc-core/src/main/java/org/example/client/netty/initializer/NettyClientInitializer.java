package org.example.client.netty.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.example.client.netty.handler.NettyClientHandler;
import org.example.common.codec.MessageDecoder;
import org.example.common.codec.MessageEncoder;
import org.example.common.serializer.JsonSerializer;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

//        // Message format: [Length][Message body], solving the issue of packet sticking
//        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
//        // Calculate the length of the current message to be sent and write it into the first 4 bytes
//        pipeline.addLast(new LengthFieldPrepender(4));
//
//        // Use Java serialization, as Netty's built-in encoder/decoder supports transmitting this structure
//        pipeline.addLast(new ObjectEncoder());
//        // Use Netty's ObjectDecoder to decode the byte stream into Java objects
//        // Pass a ClassResolver object to the ObjectDecoder constructor to resolve class names and load the corresponding classes
//        pipeline.addLast(new ObjectDecoder(new ClassResolver() {
//            @Override
//            public Class<?> resolve(String className) throws ClassNotFoundException {
//                return Class.forName(className);
//            }
//        }));

        pipeline.addLast(new MessageEncoder(new JsonSerializer()));
        pipeline.addLast(new MessageDecoder());
        pipeline.addLast(new NettyClientHandler());
    }
}
