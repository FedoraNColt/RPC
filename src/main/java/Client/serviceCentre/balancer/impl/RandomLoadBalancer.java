package Client.serviceCentre.balancer.impl;

import Client.serviceCentre.balancer.LoadBalancer;

import java.util.List;
import java.util.Random;

public class RandomLoadBalancer implements LoadBalancer {
    private static final Random random = new Random();

    @Override
    public String balance(List<String> addressList) {
        int randomIdx = random.nextInt(addressList.size());
        String address = addressList.get(randomIdx);
        System.out.println("Service Address " + address + " is chosen by the load balancer.");
        return address;
    }

    @Override
    public void addNode(String node) {

    }

    @Override
    public void removeNode(String node) {

    }
}
