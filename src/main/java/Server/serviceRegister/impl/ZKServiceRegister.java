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

    // Registers a service instance in the service registry
    @Override
    public void register(String serviceName, InetSocketAddress serviceAddress) {
        try {
            // Create a persistent node for the service if it doesn't exist
            // When the service provider is down, only keep the service and delete the service address
            if (client.checkExists().forPath("/" + serviceName) == null) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath("/" + serviceName);
            }
            // Construct the path for the service instance, where each child node represents an instance
            String path = "/" + serviceName + "/" + getServiceAddress(serviceAddress);
            // Ephemeral nodes are temporary and will be removed when the service disconnects
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(path);
        } catch (Exception e) {
            System.out.println("Service already exists: " + serviceName);
        }
    }

    // Converts an InetSocketAddress into a string formatted as "IP:port"
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() +
                ":" +
                serverAddress.getPort();
    }
}
