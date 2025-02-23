package Server.serviceRegister.impl;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import Server.serviceRegister.ServiceRegister;

import java.net.InetSocketAddress;

public class ZKServiceRegister implements ServiceRegister {
    private CuratorFramework client;
    private static final String ROOT_PATH = "MyRPC";

    public ZKServiceRegister() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.client = CuratorFrameworkFactory.builder()
                    .connectString("127.0.0.1:2181")
                    .sessionTimeoutMs(40000)
                    .retryPolicy(retryPolicy)
                    .namespace(ROOT_PATH)
                    .build();
        this.client.start();
        System.out.println("Connected to zookeeper successfully.");
    }

    @Override
    public void register(String serviceName, InetSocketAddress serviceAddress) {
        try {
            if (client.checkExists().forPath("/" + serviceName) == null) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath("/" + serviceName);
            }
            String path = "/" + serviceName + "/" + getServiceAddress(serviceAddress);
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(path);
        } catch (Exception e) {
            System.out.println("Service already exists: " + serviceName);
        }
    }

    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() +
                ":" +
                serverAddress.getPort();
    }
}
