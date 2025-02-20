package org.example.Client.proxy;

import lombok.AllArgsConstructor;
import org.example.Client.rpcClient.RPCClient;
import org.example.Client.rpcClient.impl.NettyRPCClient;
import org.example.Client.rpcClient.impl.SimpleSocketRPCClient;
import org.example.common.message.RPCRequest;
import org.example.common.message.RPCResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@AllArgsConstructor
public class ClientProxy implements InvocationHandler {
    private RPCClient rpcClient;

    public ClientProxy(String host, int port, int choose) {
        switch (choose) {
            case 0:
                rpcClient = new NettyRPCClient();
                break;
            case 1:
                rpcClient = new SimpleSocketRPCClient(host, port);
                break;
        }
    }

    public ClientProxy() {
        rpcClient = new NettyRPCClient();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RPCRequest rpcRequest = RPCRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .build();
        RPCResponse rpcResponse = rpcClient.sendRequest(rpcRequest);
        return rpcResponse.getData();
    }

    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }
}
