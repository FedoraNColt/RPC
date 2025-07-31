package org.example.client.serviceCenter.balancer;

import org.example.client.serviceCenter.balancer.impl.ConsistentHashingLoadBalancer;
import org.example.client.serviceCenter.balancer.impl.RandomLoadBalancer;
import org.example.client.serviceCenter.balancer.impl.RoundLoadBalancer;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class LoadBalancerTest {

    private List<String> servers;

    @Before
    public void setUp() {
        servers = Arrays.asList("server1:8080", "server2:8080", "server3:8080");
    }

    @Test
    public void testConsistentHashingLoadBalancer() {
        ConsistentHashingLoadBalancer balancer = new ConsistentHashingLoadBalancer();
        
        // Test basic balance functionality
        String server = balancer.balance(servers);
        assertNotNull("Selected server should not be null", server);
        assertTrue("Selected server should be in the list", servers.contains(server));
        
        // Test consistency - same request should return same server
        String server1 = balancer.getServer("user123", servers);
        String server2 = balancer.getServer("user123", servers);
        assertEquals("Same request should return same server", server1, server2);
        
        // Test distribution
        Map<String, Integer> distribution = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            String selected = balancer.getServer("request" + i, servers);
            distribution.put(selected, distribution.getOrDefault(selected, 0) + 1);
        }
        
        // Each server should get some requests (not perfect distribution but should be reasonable)
        for (String server : servers) {
            assertTrue("Server " + server + " should handle some requests", 
                      distribution.getOrDefault(server, 0) > 0);
        }
    }

    @Test 
    public void testConsistentHashingLoadBalancerAddRemoveNodes() {
        ConsistentHashingLoadBalancer balancer = new ConsistentHashingLoadBalancer();
        
        // Test adding nodes
        balancer.addNode("server4:8080");
        List<String> extendedServers = new ArrayList<>(servers);
        extendedServers.add("server4:8080");
        
        String server = balancer.balance(extendedServers);
        assertNotNull(server);
        assertTrue(extendedServers.contains(server));
        
        // Test removing nodes
        balancer.removeNode("server4:8080");
        server = balancer.balance(servers);
        assertNotNull(server);
        assertTrue(servers.contains(server));
    }

    @Test
    public void testRandomLoadBalancer() {
        RandomLoadBalancer balancer = new RandomLoadBalancer();
        
        // Test basic functionality
        String server = balancer.balance(servers);
        assertNotNull("Selected server should not be null", server);
        assertTrue("Selected server should be in the list", servers.contains(server));
        
        // Test distribution over multiple calls
        Map<String, Integer> distribution = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            String selected = balancer.balance(servers);
            distribution.put(selected, distribution.getOrDefault(selected, 0) + 1);
        }
        
        // Each server should get some requests (randomness should distribute load)
        for (String server : servers) {
            assertTrue("Server " + server + " should handle some requests", 
                      distribution.getOrDefault(server, 0) > 0);
        }
        
        // Check that distribution is reasonably balanced (within 40% of average)
        int total = distribution.values().stream().mapToInt(Integer::intValue).sum();
        int average = total / servers.size();
        for (int count : distribution.values()) {
            assertTrue("Distribution should be reasonably balanced", 
                      Math.abs(count - average) < average * 0.4);
        }
    }

    @Test
    public void testRoundLoadBalancer() {
        RoundLoadBalancer balancer = new RoundLoadBalancer();
        
        // Test round-robin behavior
        List<String> selectedServers = new ArrayList<>();
        for (int i = 0; i < servers.size() * 2; i++) {
            String server = balancer.balance(servers);
            assertNotNull("Selected server should not be null", server);
            assertTrue("Selected server should be in the list", servers.contains(server));
            selectedServers.add(server);
        }
        
        // Verify round-robin pattern
        for (int i = 0; i < servers.size(); i++) {
            assertEquals("Round-robin should cycle through servers", 
                        selectedServers.get(i), selectedServers.get(i + servers.size()));
        }
    }

    @Test
    public void testLoadBalancersWithEmptyList() {
        List<String> emptyList = new ArrayList<>();
        
        ConsistentHashingLoadBalancer consistentBalancer = new ConsistentHashingLoadBalancer();
        assertNull("Consistent hashing should return null for empty list", 
                  consistentBalancer.balance(emptyList));
        
        RandomLoadBalancer randomBalancer = new RandomLoadBalancer();
        assertNull("Random balancer should return null for empty list", 
                  randomBalancer.balance(emptyList));
        
        RoundLoadBalancer roundBalancer = new RoundLoadBalancer();
        assertNull("Round balancer should return null for empty list", 
                  roundBalancer.balance(emptyList));
    }

    @Test
    public void testLoadBalancersWithSingleServer() {
        List<String> singleServer = Arrays.asList("onlyserver:8080");
        
        ConsistentHashingLoadBalancer consistentBalancer = new ConsistentHashingLoadBalancer();
        assertEquals("Consistent hashing should return the only server", 
                    "onlyserver:8080", consistentBalancer.balance(singleServer));
        
        RandomLoadBalancer randomBalancer = new RandomLoadBalancer();
        assertEquals("Random balancer should return the only server", 
                    "onlyserver:8080", randomBalancer.balance(singleServer));
        
        RoundLoadBalancer roundBalancer = new RoundLoadBalancer();
        assertEquals("Round balancer should return the only server", 
                    "onlyserver:8080", roundBalancer.balance(singleServer));
    }

    @Test
    public void testLoadBalancerAddRemoveNodes() {
        RandomLoadBalancer randomBalancer = new RandomLoadBalancer();
        RoundLoadBalancer roundBalancer = new RoundLoadBalancer();
        
        // Test adding nodes
        randomBalancer.addNode("newserver:8080");
        roundBalancer.addNode("newserver:8080");
        
        // Test removing nodes
        randomBalancer.removeNode("server1:8080");
        roundBalancer.removeNode("server1:8080");
        
        // These should not throw exceptions (implementation specific behavior)
        assertNotNull(randomBalancer);
        assertNotNull(roundBalancer);
    }
}