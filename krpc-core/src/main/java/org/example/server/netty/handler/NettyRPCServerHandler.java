package org.example.server.netty.handler;

import org.example.common.message.RPCRequest;
import org.example.common.message.RPCResponse;
import org.example.server.ratelimit.RateLimit;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import org.example.server.provider.ServiceProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@AllArgsConstructor
public class NettyRPCServerHandler extends SimpleChannelInboundHandler<RPCRequest> {
    private ServiceProvider serviceProvider;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCRequest rpcRequest) throws Exception {
        // Process the RPC request and send response back to client
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
        if (rpcRequest == null) {
            System.err.println("Received null RPC request");
            return RPCResponse.builder()
                    .code(400)
                    .message("Invalid request: null")
                    .build();
        }

        String interfaceName = rpcRequest.getInterfaceName();
        if (interfaceName == null || interfaceName.trim().isEmpty()) {
            System.err.println("Received RPC request with null/empty interface name");
            return RPCResponse.builder()
                    .code(400)
                    .message("Invalid request: missing interface name")
                    .build();
        }

        try {
            // Acquire a rate-limit token for this interface to manage traffic
            RateLimit rateLimit = serviceProvider.getRateLimitProvider().getRateLimit(interfaceName);
            if (!rateLimit.getToken()) {
                // If acquiring the token fails, apply rate limiting and quickly return a failed response
                System.out.println("Service " + interfaceName + " throttled! Returning failure response.");
                return RPCResponse.builder()
                        .code(429)
                        .message("Service throttled - too many requests")
                        .build();
            }

            // Get the corresponding service implementation class on the server side
            Object service = serviceProvider.getService(interfaceName);
            if (service == null) {
                System.err.println("No service implementation found for interface: " + interfaceName);
                return RPCResponse.builder()
                        .code(404)
                        .message("Service not found: " + interfaceName)
                        .build();
            }

            String methodName = rpcRequest.getMethodName();
            if (methodName == null || methodName.trim().isEmpty()) {
                System.err.println("Received RPC request with null/empty method name for interface: " + interfaceName);
                return RPCResponse.builder()
                        .code(400)
                        .message("Invalid request: missing method name")
                        .build();
            }

            Method method = service.getClass().getMethod(methodName, rpcRequest.getParamTypes());
            // Invoke the method using reflection and get the result
            Object res = method.invoke(service, rpcRequest.getParams());
            return RPCResponse.success(res);
            
        } catch (NoSuchMethodException e) {
            System.err.println("Method not found: " + rpcRequest.getMethodName() + " in service: " + interfaceName);
            return RPCResponse.builder()
                    .code(404)
                    .message("Method not found: " + rpcRequest.getMethodName())
                    .build();
        } catch (IllegalAccessException e) {
            System.err.println("Access denied when invoking method: " + rpcRequest.getMethodName());
            e.printStackTrace();
            return RPCResponse.builder()
                    .code(403)
                    .message("Access denied to method: " + rpcRequest.getMethodName())
                    .build();
        } catch (InvocationTargetException e) {
            System.err.println("Error executing method: " + rpcRequest.getMethodName() + " - " + e.getTargetException().getMessage());
            e.getTargetException().printStackTrace();
            return RPCResponse.builder()
                    .code(500)
                    .message("Method execution failed: " + e.getTargetException().getMessage())
                    .build();
        } catch (Exception e) {
            System.err.println("Unexpected error processing request for interface: " + interfaceName);
            e.printStackTrace();
            return RPCResponse.builder()
                    .code(500)
                    .message("Server error: " + e.getMessage())
                    .build();
        }
    }
}
