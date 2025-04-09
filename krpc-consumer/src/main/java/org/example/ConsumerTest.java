package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.client.proxy.ClientProxy;
import org.example.pojo.User;
import org.example.service.UserService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConsumerTest {

    private static final int THREAD_POOL_SIZE = 20;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) throws InterruptedException {
        // Outdated  way to initialize ClientProxy with a specific server address and port
        // ClientProxy clientProxy = new ClientProxy("127.0.0.1", 9999);

        // Uses a dynamic service discovery mechanism (e.g., Zookeeper) to retrieve the service address
        ClientProxy clientProxy = new ClientProxy();
        UserService proxy  = clientProxy.getProxy(UserService.class);

        for (int i = 0; i < 120; i++) {
            final Integer id = i;
            if (i % 30 == 0) {
                Thread.sleep(10000);
            }
            executorService.submit(() -> {
                try {
                    User user = proxy.getUserByUserId(id);
                    log.info("Get user from server: user = {}", user);

                    User u = User.builder()
                            .id(id)
                            .userName("User" + id)
                            .gender(true)
                            .build();
                    Integer userId = proxy.insertUserId(u);
                    if (userId != null) {
                        log.info("Insert user into server: userId = {}", userId);
                    } else {
                        log.warn("Failed to insert user: userId = {}", id);
                    }
                } catch (NullPointerException e) {
                    log.error("User not found");
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
            System.err.println("Some tasks did not finish in time.");
            executorService.shutdownNow();
        }

        log.info("All tasks completed. Done!");
    }
}
