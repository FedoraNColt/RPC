package Client;

import Client.proxy.ClientProxy;
import common.pojo.User;
import common.service.UserService;

public class TestClient {
    public static void main(String[] args) throws InterruptedException {
        // Outdated  way to initialize ClientProxy with a specific server address and port
        // ClientProxy clientProxy = new ClientProxy("127.0.0.1", 9999);

        // Uses a dynamic service discovery mechanism (e.g., Zookeeper) to retrieve the service address
        ClientProxy clientProxy = new ClientProxy();
        UserService proxy  = clientProxy.getProxy(UserService.class);

        User user = proxy.getUserByUserId(1);
        System.out.println("Get user from server: user = " + user);

        User u = User.builder()
                .id(100)
                .userName("gzj")
                .sex(true)
                .build();
        Integer id = proxy.insertUserId(u);
        System.out.println("Insert user into server: user = " + id);
    }
}
