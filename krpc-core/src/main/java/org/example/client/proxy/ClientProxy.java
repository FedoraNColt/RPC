package org.example.client.proxy;

import org.example.client.circuitBreaker.CircuitBreaker;
import org.example.client.circuitBreaker.CircuitBreakerProvider;
import org.example.client.retry.GuavaRetry;
import org.example.client.serviceCenter.ServiceCenter;
import org.example.client.serviceCenter.ZKServiceCenter;
import lombok.AllArgsConstructor;
import org.example.client.rpcClient.RPCClient;
import org.example.client.rpcClient.impl.NettyRPCClient;
import org.example.common.message.RPCRequest;
import org.example.common.message.RPCResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@AllArgsConstructor
/**
 * ClientProxy creates dynamic proxies for service interfaces to enable RPC calls.
 * It intercepts method calls, converts them to RPC requests, handles circuit breaking,
 * retry logic, and forwards requests through the configured RPC client.
 */
public class ClientProxy implements InvocationHandler {
    private RPCClient rpcClient;
    private ServiceCenter serviceCentre;
    private CircuitBreakerProvider circuitBreakerProvider;

//    public ClientProxy(String host, int port, int choose) {
//        switch (choose) {
//            case 0:
//                rpcClient = new NettyRPCClient(host, port);
//                break;
//            case 1:
//                rpcClient = new SimpleSocketRPCClient();
//                break;
//        }
//    }

    /**
     * Constructor that initializes the ClientProxy with default implementations.
     * Sets up ZooKeeper service discovery, Netty RPC client, and circuit breaker provider.
     * 
     * @throws InterruptedException if ZooKeeper connection is interrupted
     */
    public ClientProxy() throws InterruptedException {
        serviceCentre = new ZKServiceCenter();
        rpcClient = new NettyRPCClient(serviceCentre);
        circuitBreakerProvider = new CircuitBreakerProvider();
    }

    /**
     * Intercepts method calls on proxy objects and converts them to RPC requests.
     * Handles circuit breaking, retry logic, and response processing.
     * 
     * @param proxy the proxy instance
     * @param method the method being called
     * @param args the method arguments
     * @return the result from the remote service
     * @throws Throwable if circuit breaker is open or RPC call fails
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Build RPC request from method call information
        RPCRequest rpcRequest = RPCRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .build();

        // Check circuit breaker before making the call
        CircuitBreaker circuitBreaker = circuitBreakerProvider.getCircuitBreaker(method.getName());
        if (!circuitBreaker.allowRequest()) {
            throw new RuntimeException("Service " + method.getDeclaringClass().getName() + "." + method.getName() + " is currently unavailable due to circuit breaker");
        }

        // Send request with or without retry based on service configuration
        RPCResponse rpcResponse;
        if (serviceCentre.checkRetry(rpcRequest.getInterfaceName())) {
            rpcResponse = new GuavaRetry().sendServiceWithRetry(rpcRequest, rpcClient);
        } else {
            rpcResponse = rpcClient.sendRequest(rpcRequest);
        }

        // Record response in circuit breaker and return result
        circuitBreaker.record(rpcResponse.getCode());
        return rpcResponse.getData();
    }

    /**
     * Creates a dynamic proxy for the specified service interface.
     * 
     * @param clazz the service interface class
     * @param <T> the service interface type
     * @return a proxy instance that implements the service interface
     */
    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }
}
