package Client.serviceCentre;

import java.net.InetSocketAddress;

public interface ServiceCentre {
    // Search the address according to the service name
    InetSocketAddress serviceDiscovery(String serviceName);
}
