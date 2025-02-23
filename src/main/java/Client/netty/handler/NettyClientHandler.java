package Client.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import common.message.RPCResponse;

public class NettyClientHandler extends SimpleChannelInboundHandler<RPCResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCResponse rpcResponse) throws Exception {
        // After receiving the response, assign an alias to the channel so that sendRequest can read the response
        AttributeKey<RPCResponse> key = AttributeKey.valueOf("RPCResponse");
        // Bind the RPCResponse returned by the server to the current Channel's attributes
        ctx.channel().attr(key).set(rpcResponse);
        // Close the current channel (short connection mode)
        ctx.channel().close();
    }

    // Capture exceptions during execution, handle them, and release resources
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
