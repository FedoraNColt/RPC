package org.example.Server;

import org.example.Server.provider.ServiceProvider;
import org.example.Server.server.RPCServer;
import org.example.Server.server.impl.NettyRPCServer;
import org.example.common.service.UserService;
import org.example.common.service.impl.UserServiceImpl;

public class TestServer {
    public static void main(String[] args) {
        UserService userService = new UserServiceImpl();

        ServiceProvider serviceProvider = new ServiceProvider("127.0.0.1", 9999);
        serviceProvider.provideServiceInterface(userService);

        RPCServer rpcServer = new NettyRPCServer(serviceProvider);
        rpcServer.start(9999);
    }
}
