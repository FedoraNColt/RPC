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
// Pass in the Class object of the service interface to encapsulate it into a request using reflection
// In the RPCClientProxy class, add an RPCClient variable to allow passing different clients (simple, netty)
// and invoke the common sendRequest interface to send requests
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

    public ClientProxy() throws InterruptedException {
        serviceCentre = new ZKServiceCenter();
        rpcClient = new NettyRPCClient(serviceCentre);
        circuitBreakerProvider = new CircuitBreakerProvider();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Construct the request
        RPCRequest rpcRequest = RPCRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .build();

        CircuitBreaker circuitBreaker = circuitBreakerProvider.getCircuitBreaker(method.getName());
        if (!circuitBreaker.allowRequest()) {
            return null;
        }

        RPCResponse rpcResponse;
        if (serviceCentre.checkRetry(rpcRequest.getInterfaceName())) {
            rpcResponse = new GuavaRetry().sendServiceWithRetry(rpcRequest, rpcClient);
        } else {
            rpcResponse = rpcClient.sendRequest(rpcRequest);
        }

        circuitBreaker.record(rpcResponse.getCode());
        return rpcResponse.getData();
    }

    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }
}
