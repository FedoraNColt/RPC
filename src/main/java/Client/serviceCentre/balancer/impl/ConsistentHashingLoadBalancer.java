package Client.serviceCentre.balancer.impl;

import Client.serviceCentre.balancer.LoadBalancer;

import java.util.*;

import static util.HashUtil.getHash;

public class ConsistentHashingLoadBalancer implements LoadBalancer {
    // Number of virtual nodes per real node to improve distribution balance
    private static final int VIRTUAL_NUM = 5;
    // A sorted map storing the hash values of virtual nodes as keys and corresponding real nodes as values
    private final SortedMap<Integer, String> shardMap = new TreeMap<>();
    // Set of actual server nodes
    private final Set<String> realNodes = new HashSet<>();

    /**
     * Simulates a load balancing request by generating a random UUID as the request node
     * and selecting the corresponding server using consistent hashing.
     *
     * @param addressList The list of available server nodes
     * @return The selected server node
     */
    @Override
    public String balance(List<String> addressList) {
        // Generate a random UUID as the requesting node
        String randomStr = UUID.randomUUID().toString();
        return getServer(randomStr, addressList);
    }

    /**
     * Adds a new real server node along with its virtual nodes to the consistent hash ring.
     *
     * @param node The real node to be added
     */
    @Override
    public void addNode(String node) {
        if (!realNodes.contains(node)) {
            addRealAndVirtualNodes(node);
        }
    }

    /**
     * Removes a real node and its associated virtual nodes from the consistent hash ring.
     *
     * @param node The real node to be removed
     */
    @Override
    public void removeNode(String node) {
        if (realNodes.contains(node)) {
            realNodes.remove(node);
            System.out.println("Real Node " + node + " is removed.");
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VN" + i;
                int hash = getHash(virtualNode);
                shardMap.remove(hash);
                System.out.println("Virtual Node [" + virtualNode + "] is removed.");
            }
        }
    }

    private void init(List<String> serverList) {
        for (String server : serverList) {
            addRealAndVirtualNodes(server);
        }
    }

    /**
     * Selects the appropriate server for a given request node using consistent hashing.
     *
     * @param node        The requesting node (e.g., client making a request)
     * @param serviceList The list of available server nodes
     * @return The selected server node
     */
    private String getServer(String node, List<String> serviceList) {
        init(serviceList);
        int hash = getHash(node);
        Integer key = null;
        SortedMap<Integer, String> subMap = shardMap.tailMap(hash);
        if (subMap.isEmpty()) {
            key = shardMap.lastKey();
        } else {
            key = subMap.firstKey();
        }
        String virtualNode = serviceList.get(key);
        return virtualNode.substring(0, virtualNode.indexOf("&&VN"));
    }

    private void addRealAndVirtualNodes(String node) {
        realNodes.add(node);
        System.out.println("Real Node [" + node + "] is added.");
        for (int i = 0; i < VIRTUAL_NUM; i++) {
            String virtualNode = node + "&&VN" + i;
            int hash = getHash(virtualNode);
            shardMap.put(hash, virtualNode);
            System.out.println("Virtual Node [" + virtualNode + "] is added.");
        }
    }
}
