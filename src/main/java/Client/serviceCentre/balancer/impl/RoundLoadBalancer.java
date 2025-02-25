package Client.serviceCentre.balancer.impl;

import Client.serviceCentre.balancer.LoadBalancer;

import java.util.List;

public class RoundLoadBalancer implements LoadBalancer {
    private int idx = -1;

    @Override
    public String balance(List<String> addressList) {
        idx++;
        idx = idx % addressList.size();
        String address = addressList.get(idx);
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
