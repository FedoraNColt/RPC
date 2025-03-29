package org.example.client.serviceCenter;

import java.net.InetSocketAddress;

public interface ServiceCenter {
    // Search the address according to the service name
    InetSocketAddress serviceDiscovery(String serviceName);

    // Checks whether a service request is eligible for a retry or not
    boolean checkRetry(String serviceName);
}
