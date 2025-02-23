package v1.Client.proxy;

import lombok.AllArgsConstructor;
import v1.Client.rpcClient.RPCClient;
import v1.Client.rpcClient.impl.NettyRPCClient;
import v1.Client.rpcClient.impl.SimpleSocketRPCClient;
import v1.common.message.RPCRequest;
import v1.common.message.RPCResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@AllArgsConstructor
public class ClientProxy implements InvocationHandler {
    private RPCClient rpcClient;

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

    public ClientProxy() {
        rpcClient = new NettyRPCClient();
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
        RPCResponse rpcResponse = rpcClient.sendRequest(rpcRequest);
        return rpcResponse.getData();
    }

    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }
}
