package org.example.server.serviceRegister.impl;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.example.server.serviceRegister.ServiceRegister;

import java.net.InetSocketAddress;

public class ZKServiceRegister implements ServiceRegister {
    private CuratorFramework client;
    private static final String ROOT_PATH = "MyRPC";
    private static final String RETRY = "CanRetry";

    public ZKServiceRegister() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.client = CuratorFrameworkFactory.builder()
                    .connectString("127.0.0.1:2181")
                    .sessionTimeoutMs(40000)
                    .retryPolicy(retryPolicy)
                    .namespace(ROOT_PATH)
                    .build();
        this.client.start();
        
        try {
            // Wait for connection to be established
            this.client.blockUntilConnected();
            System.out.println("Connected to ZooKeeper successfully.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to connect to ZooKeeper", e);
        }
    }

    // Registers a service instance in the service registry
    @Override
    public void register(String serviceName, InetSocketAddress serviceAddress, boolean canRetry) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be null or empty");
        }
        if (serviceAddress == null) {
            throw new IllegalArgumentException("Service address cannot be null");
        }

        try {
            // Create persistent root node for the service type (survives provider restarts)
            String serviceRoot = "/" + serviceName;
            if (client.checkExists().forPath(serviceRoot) == null) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(serviceRoot);
                System.out.println("Created service root path: " + serviceRoot);
            }

            // Create ephemeral node for this specific service instance (auto-deleted when provider disconnects)
            String serviceInstancePath = serviceRoot + "/" + getServiceAddress(serviceAddress);
            
            // Check if this exact service instance is already registered
            if (client.checkExists().forPath(serviceInstancePath) != null) {
                System.out.println("Service instance already registered: " + serviceInstancePath);
                return;
            }

            // Register the service instance (ephemeral node ensures cleanup on disconnect)
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(serviceInstancePath);
            System.out.println("Registered service instance: " + serviceInstancePath);

            // Add service to retry whitelist if retry is enabled
            if (canRetry) {
                String retryRoot = "/" + RETRY;
                if (client.checkExists().forPath(retryRoot) == null) {
                    client.create()
                            .creatingParentsIfNeeded()
                            .withMode(CreateMode.PERSISTENT)
                            .forPath(retryRoot);
                }
                
                String retryPath = retryRoot + "/" + serviceName;
                if (client.checkExists().forPath(retryPath) == null) {
                    client.create()
                            .creatingParentsIfNeeded()
                            .withMode(CreateMode.EPHEMERAL)
                            .forPath(retryPath);
                    System.out.println("Registered service for retry: " + retryPath);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Failed to register service " + serviceName + " at " + getServiceAddress(serviceAddress) + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Service registration failed", e);
        }
    }

    // Converts an InetSocketAddress into a string formatted as "IP:port"
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() +
                ":" +
                serverAddress.getPort();
    }

    /**
     * Shutdown the ZooKeeper client and release resources
     */
    public void shutdown() {
        if (client != null) {
            try {
                client.close();
                System.out.println("ZooKeeper service register shutdown completed.");
            } catch (Exception e) {
                System.err.println("Error shutting down ZooKeeper client: " + e.getMessage());
            }
        }
    }
}
