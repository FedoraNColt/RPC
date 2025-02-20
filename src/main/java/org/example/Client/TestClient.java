package org.example.Client;

import org.example.Client.proxy.ClientProxy;
import org.example.common.pojo.User;
import org.example.common.service.UserService;

public class TestClient {
    public static void main(String[] args) {
        // ClientProxy clientProxy = new ClientProxy("127.0.0.1", 9999, 0);
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
