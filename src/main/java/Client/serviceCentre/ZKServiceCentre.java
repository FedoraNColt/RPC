package Client.serviceCentre;

import Client.cache.ServiceCache;
import Client.serviceCentre.balancer.impl.ConsistentHashingLoadBalancer;
import Client.serviceCentre.watcher.ZKwatcher;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Implementation of ServiceCenter that uses ZooKeeper for service discovery.
 */
public class ZKServiceCentre implements ServiceCentre {
    // Curator provides the zookeeper client
    private CuratorFramework client;
    // zookeeper root path node
    private static final String ROOT_PATH = "MyRPC";
    private static final String RETRY = "CanRetry";
    private ServiceCache serviceCache;

    /**
     * Initializes the ZooKeeper client and establishes a connection.
     * The client connects to the ZooKeeper server and starts listening for updates.
     *
     * @throws InterruptedException if the thread is interrupted while waiting for ZooKeeper connection.
     */
    public ZKServiceCentre() throws InterruptedException {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        // The server address of zookeeper is fixed, so both service providers and clients must connect to it.
        // sessionTimeoutMs: Defines the session timeout duration, and it is related to the tickTime value in the zoo.cfg configuration file
        // Zookeeper automatically adjusts the final session timeout value based on the minSessionTimeout and maxSessionTimeout parameters:
        //      - The default minSessionTimeout is 2 times tickTime
        //      - The default maxSessionTimeout is 20 times tickTime
        // Uses heartbeats to monitor connectivity
        this.client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .sessionTimeoutMs(40000)
                .retryPolicy(retryPolicy)
                .namespace(ROOT_PATH)
                .build();
        this.client.start();
        System.out.println("Connected to zookeeper successfully.");

        this.serviceCache = new ServiceCache();

        ZKwatcher zKwatcher = new ZKwatcher(client, serviceCache);

        zKwatcher.watchToUpdate(ROOT_PATH);
    }

    /**
     * Discovers a service instance by retrieving its address from ZooKeeper.
     * If the service is cached, it returns a cached address; otherwise, it queries ZooKeeper.
     *
     * @param serviceName The name of the service to discover.
     * @return The address of a service instance, or null if the service is not found.
     */
    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            // Checks if the service is already stored in the cache
            List<String> addressList = serviceCache.getServiceFromCache(serviceName);
            if (addressList == null) {
                // Retrieves all child nodes under the service name in ZooKeeper
                // Each child node represents an instance of the service with an address in "IP:port" format
                addressList = client.getChildren().forPath("/" + serviceName);
            }

            // Select the available instance by load balancing
            String address = new ConsistentHashingLoadBalancer().balance(addressList);
            // Convert the "IP:port" string into an InetSocketAddress for easier client communication
            return parseAddress(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks whether the given service is eligible for retry.
     *
     * @param serviceName The name of the service.
     * @return true if the service is allowed to retry, false otherwise.
     */
    @Override
    public boolean checkRetry(String serviceName) {
        boolean canRetry = false;
        try {
            List<String> serviceList = client.getChildren().forPath("/" + RETRY);
            for (String service : serviceList) {
                if (service.equals(serviceName)) {
                    System.out.println("Service " + serviceName + " is on the whitelist and can be retried.");
                    canRetry = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return canRetry;
    }

    /**
     * Parses a service address string formatted as "IP:port" into an InetSocketAddress.
     *
     * @param address The service address string in "IP:port" format.
     * @return The corresponding InetSocketAddress object.
     */
    private InetSocketAddress parseAddress(String address) {
        String[] res = address.split(":");
        return new InetSocketAddress(res[0], Integer.parseInt(res[1]));
    }
}
