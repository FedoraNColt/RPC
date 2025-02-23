package v1.Server;

import v1.Server.provider.ServiceProvider;
import v1.Server.server.RPCServer;
import v1.Server.server.impl.NettyRPCServer;
import v1.common.service.UserService;
import v1.common.service.impl.UserServiceImpl;

public class TestServer {
    public static void main(String[] args) {
        UserService userService = new UserServiceImpl();

        ServiceProvider serviceProvider = new ServiceProvider("127.0.0.1", 9999);
        serviceProvider.provideServiceInterface(userService);

        RPCServer rpcServer = new NettyRPCServer(serviceProvider);
        rpcServer.start(9999);
    }
}
