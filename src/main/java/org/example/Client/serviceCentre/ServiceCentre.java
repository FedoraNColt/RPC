package org.example.Client.serviceCentre;

import java.net.InetSocketAddress;

public interface ServiceCentre {
    InetSocketAddress serviceDiscovery(String serviceName);
}
