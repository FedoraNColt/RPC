package Client.serviceCentre.balancer;

import java.util.List;

public interface LoadBalancer {
    String balance(List<String> addressList);

    void addNode(String node);

    void removeNode(String node);
}
