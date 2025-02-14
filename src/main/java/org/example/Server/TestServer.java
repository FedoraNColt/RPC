package org.example.part1.Server;

import org.example.part1.Server.provider.ServiceProvider;
import org.example.part1.Server.server.RPCServer;
import org.example.part1.Server.server.impl.SimpleRPCServer;
import org.example.part1.common.service.UserService;
import org.example.part1.common.service.impl.UserServiceImpl;

public class TestServer {
    public static void main(String[] args) {
        UserService userService = new UserServiceImpl();

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.provideServiceInterface(userService);

        RPCServer rpcServer = new SimpleRPCServer(serviceProvider);
        rpcServer.start(9999);
    }
}
