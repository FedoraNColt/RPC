package Client.serviceCentre;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;

public class ZKServiceCentre implements ServiceCentre {
    // Curator provides the zookeeper client
    private CuratorFramework client;
    // zookeeper root path node
    private static final String ROOT_PATH = "MyRPC";

    // Initializes the Zookeeper client and establishes a connection to the Zookeeper server
    public ZKServiceCentre() {
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
    }

    @Override
    // Retrieves the service address based on the service name (interface name)
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            // Retrieve all child nodes under the given service name
            // These child nodes typically store service instance addresses in the format "IP:port"
            List<String> strings = client.getChildren().forPath("/" + serviceName);

            // Select the first available instance (can be extended to implement load balancing)
            String address = strings.get(0);

            // Convert the "IP:port" string into an InetSocketAddress for easier client communication
            return parseAddress(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Parses a string formatted as "IP:port" into an InetSocketAddress
    private InetSocketAddress parseAddress(String serviceName) {
        String[] res = serviceName.split(":");
        return new InetSocketAddress(res[0], Integer.parseInt(res[1]));
    }
}
