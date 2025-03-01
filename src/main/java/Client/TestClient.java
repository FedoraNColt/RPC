package Client;

import Client.proxy.ClientProxy;
import common.pojo.User;
import common.service.UserService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestClient {
    public static void main(String[] args) throws InterruptedException {
        // Outdated  way to initialize ClientProxy with a specific server address and port
        // ClientProxy clientProxy = new ClientProxy("127.0.0.1", 9999);

        // Uses a dynamic service discovery mechanism (e.g., Zookeeper) to retrieve the service address
        ClientProxy clientProxy = new ClientProxy();
        UserService proxy  = clientProxy.getProxy(UserService.class);

        ExecutorService executorService = Executors.newFixedThreadPool(10);


        for (int i = 0; i < 120; i++) {
            Integer id = i;
            if (i % 30 == 0) {
                Thread.sleep(10000);
            }
            executorService.submit(() -> {
                try {
                    User user = proxy.getUserByUserId(id);
                    System.out.println("Get user from server: user = " + user);

                    User u = User.builder()
                            .id(id)
                            .userName("User" + id.toString())
                            .sex(true)
                            .build();
                    Integer userId = proxy.insertUserId(u);
                    System.out.println("Insert user into server: user = " + userId);
                } catch (NullPointerException e) {
                    System.out.println("User not found");
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
            System.err.println("Some tasks did not finish in time.");
            executorService.shutdownNow();
        }

        System.out.println("All tasks completed. Done!");

//        User user = proxy.getUserByUserId(1);
//        System.out.println("Get user from server: user = " + user);
//
//        User u = User.builder()
//                .id(100)
//                .userName("gzj")
//                .sex(true)
//                .build();
//        Integer id = proxy.insertUserId(u);
//        System.out.println("Insert user into server: user = " + id);
    }
}
