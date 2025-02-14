package org.example.part1.Client;

import org.example.part1.Client.proxy.ClientProxy;
import org.example.part1.common.pojo.User;
import org.example.part1.common.service.UserService;

public class TestClient {
    public static void main(String[] args) {
        ClientProxy clientProxy = new ClientProxy("127.0.0.1", 9999);
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
