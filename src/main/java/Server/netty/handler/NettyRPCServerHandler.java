package Server.netty.handler;

import Server.ratelimit.RateLimit;
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
        // Receive requests, read and call the service
        RPCResponse rpcResponse = getResponse(rpcRequest);
        ctx.writeAndFlush(rpcResponse);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * Executes an RPC request by acquiring a rate-limit token and invoking the target method via reflection.
     *
     * @param rpcRequest The incoming RPC request containing the service name, method name, parameter types, and arguments.
     * @return An {@code RpcResponse} indicating either a success with the method result or a failure if throttled or an error occurs.
     */
    private RPCResponse getResponse(RPCRequest rpcRequest) {
        String interfaceName = rpcRequest.getInterfaceName();
        // Acquire a rate-limit token for this interface to manage traffic
        RateLimit rateLimit = serviceProvider.getRateLimitProvider().getRateLimit(interfaceName);
        if (!rateLimit.getToken()) {
            // If acquiring the token fails, apply rate limiting and quickly return a failed response
            System.out.println("Service throttled! Returning failure response.");
            return RPCResponse.fail();
        }

        // Get the corresponding service implementation class on the server side
        Object service = serviceProvider.getService(interfaceName);
        Method method = null;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            // Invoke the method using reflection and get the result
            Object res = method.invoke(service, rpcRequest.getParams());
            return RPCResponse.success(res);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println("Errors when executing " + rpcRequest.getMethodName());
            return RPCResponse.fail();
        }
    }
}
