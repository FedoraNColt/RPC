package org.example.part1.Client.proxy;

import lombok.AllArgsConstructor;
import org.example.part1.Client.IOClient;
import org.example.part1.common.message.RPCRequest;
import org.example.part1.common.message.RPCResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@AllArgsConstructor
public class ClientProxy implements InvocationHandler {

    private String host;
    private int port;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RPCRequest rpcRequest = RPCRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .build();
        RPCResponse rpcResponse = IOClient.sendRequest(host, port, rpcRequest);
        return rpcResponse.getData();
    }

    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }
}
