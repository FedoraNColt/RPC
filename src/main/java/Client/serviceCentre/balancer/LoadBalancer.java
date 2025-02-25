package Client.serviceCentre.balancer;

import java.util.List;

public interface LoadBalancer {
    // Defines the load balancing strategy and returns the selected server address
    String balance(List<String> addressList);

    // Adds a new server node to the load balancing pool
    void addNode(String node);

    // Removes a server node from the load balancing pool
    void removeNode(String node);
}
