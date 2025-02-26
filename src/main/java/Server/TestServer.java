package Server;

import Server.provider.ServiceProvider;
import Server.server.RPCServer;
import Server.server.impl.NettyRPCServer;
import common.service.UserService;
import common.service.impl.UserServiceImpl;

public class TestServer {
    public static void main(String[] args) {
        UserService userService = new UserServiceImpl();

        ServiceProvider serviceProvider = new ServiceProvider("127.0.0.1", 9999);
        serviceProvider.provideServiceInterface(userService, true);

        RPCServer rpcServer = new NettyRPCServer(serviceProvider);
        rpcServer.start(9999);
    }
}
