package org.example;

import org.example.server.provider.ServiceProvider;
import org.example.server.server.RPCServer;
import org.example.server.server.impl.NettyRPCServer;
import org.example.service.UserService;
import org.example.service.impl.UserServiceImpl;

public class TestServer {
    public static void main(String[] args) {
        UserService userService = new UserServiceImpl();

        ServiceProvider serviceProvider = new ServiceProvider("127.0.0.1", 9999);
        serviceProvider.provideServiceInterface(userService, true);

        RPCServer rpcServer = new NettyRPCServer(serviceProvider);
        rpcServer.start(9999);
    }
}
