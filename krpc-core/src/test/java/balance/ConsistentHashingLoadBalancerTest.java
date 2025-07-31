package balance;

import org.example.client.serviceCenter.balancer.impl.ConsistentHashingLoadBalancer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConsistentHashingLoadBalancerTest {

    private ConsistentHashingLoadBalancer loadBalancer;

    @Before
    public void setUp() {
        loadBalancer = new ConsistentHashingLoadBalancer();
    }

    @Test
    public void testGetServer() {
        List<String> nodes = Arrays.asList("server1", "server2", "server3");
        loadBalancer.balance(nodes);

        String server = loadBalancer.getServer("request-1", nodes);
        assertNotNull("server should not be null", server);
        assertTrue("server should be one of the real nodes", nodes.contains(server));
    }

}
