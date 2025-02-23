package Client.serviceCentre;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;

public class ZKServiceCentre implements ServiceCentre {
    private CuratorFramework client;
    private static final String ROOT_PATH = "MyRPC";

    public ZKServiceCentre() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
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
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            List<String> strings = client.getChildren().forPath("/" + serviceName);
            String address = strings.get(0);
            return parseAddress(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private InetSocketAddress parseAddress(String serviceName) {
        String[] res = serviceName.split(":");
        return new InetSocketAddress(res[0], Integer.parseInt(res[1]));
    }
}
