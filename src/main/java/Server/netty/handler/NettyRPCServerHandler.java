package Server.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import Server.provider.ServiceProvider;
import common.message.RPCRequest;
import common.message.RPCResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@AllArgsConstructor
public class NettyRPCServerHandler extends SimpleChannelInboundHandler<RPCRequest> {
    private ServiceProvider serviceProvider;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCRequest rpcRequest) throws Exception {
        // receive requests, read and call service
        RPCResponse rpcResponse = getResponse(rpcRequest);
        ctx.writeAndFlush(rpcResponse);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private RPCResponse getResponse(RPCRequest rpcRequest) {
        String interfaceName = rpcRequest.getInterfaceName();
        Object service = serviceProvider.getService(interfaceName);
        Method method = null;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            Object res = method.invoke(service, rpcRequest.getParams());
            return RPCResponse.success(res);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println("Errors when executing " + rpcRequest.getMethodName());
            return RPCResponse.fail();
        }
    }
}
