package org.example.client.serviceCenter;

import org.example.client.cache.ServiceCache;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ServiceDiscoveryTest {

    @Mock
    private ServiceCache mockServiceCache;
    
    private MockServiceCenter serviceCenter;

    // Mock implementation for testing without ZooKeeper
    public static class MockServiceCenter implements ServiceCenter {
        private ServiceCache serviceCache;
        
        public MockServiceCenter(ServiceCache serviceCache) {
            this.serviceCache = serviceCache;
        }

        @Override
        public InetSocketAddress serviceDiscovery(String serviceName) {
            if (serviceName == null || serviceName.trim().isEmpty()) {
                return null;
            }
            
            List<String> addresses = serviceCache.getServiceFromCache(serviceName);
            if (addresses == null || addresses.isEmpty()) {
                // Simulate some default services for testing
                switch (serviceName) {
                    case "org.example.service.UserService":
                        addresses = Arrays.asList("127.0.0.1:8080", "127.0.0.1:8081");
                        break;
                    case "org.example.service.TestService":
                        addresses = Arrays.asList("127.0.0.1:9090");
                        break;
                    default:
                        return null;
                }
            }
            
            if (addresses.isEmpty()) {
                return null;
            }
            
            // Simple round-robin for testing
            String address = addresses.get(0);
            String[] parts = address.split(":");
            return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
        }

        @Override
        public boolean checkRetry(String serviceName) {
            // Mock retry logic - services ending with "Retry" can be retried
            return serviceName != null && serviceName.contains("Retry");
        }
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        serviceCenter = new MockServiceCenter(mockServiceCache);
    }

    @Test
    public void testServiceDiscoveryWithCachedService() {
        String serviceName = "org.example.service.CachedService";
        List<String> cachedAddresses = Arrays.asList("192.168.1.100:8080", "192.168.1.101:8080");
        
        when(mockServiceCache.getServiceFromCache(serviceName)).thenReturn(cachedAddresses);
        
        InetSocketAddress address = serviceCenter.serviceDiscovery(serviceName);
        
        assertNotNull("Should return an address for cached service", address);
        assertEquals("Should return first cached address", "192.168.1.100", address.getHostName());
        assertEquals("Should return correct port", 8080, address.getPort());
        
        verify(mockServiceCache).getServiceFromCache(serviceName);
    }

    @Test
    public void testServiceDiscoveryWithKnownService() {
        String serviceName = "org.example.service.UserService";
        
        when(mockServiceCache.getServiceFromCache(serviceName)).thenReturn(null);
        
        InetSocketAddress address = serviceCenter.serviceDiscovery(serviceName);
        
        assertNotNull("Should return an address for known service", address);
        assertEquals("Should return localhost", "127.0.0.1", address.getHostName());
        assertTrue("Should return valid port", address.getPort() > 0);
    }

    @Test
    public void testServiceDiscoveryWithUnknownService() {
        String serviceName = "org.example.service.UnknownService";
        
        when(mockServiceCache.getServiceFromCache(serviceName)).thenReturn(null);
        
        InetSocketAddress address = serviceCenter.serviceDiscovery(serviceName);
        
        assertNull("Should return null for unknown service", address);
    }

    @Test
    public void testServiceDiscoveryWithNullServiceName() {
        InetSocketAddress address = serviceCenter.serviceDiscovery(null);
        assertNull("Should return null for null service name", address);
    }

    @Test
    public void testServiceDiscoveryWithEmptyServiceName() {
        InetSocketAddress address = serviceCenter.serviceDiscovery("");
        assertNull("Should return null for empty service name", address);
        
        address = serviceCenter.serviceDiscovery("   ");
        assertNull("Should return null for whitespace service name", address);
    }

    @Test
    public void testServiceDiscoveryWithEmptyCache() {
        String serviceName = "org.example.service.TestService";
        
        when(mockServiceCache.getServiceFromCache(serviceName)).thenReturn(Collections.emptyList());
        
        InetSocketAddress address = serviceCenter.serviceDiscovery(serviceName);
        
        // Should fall back to default behavior and find the service
        assertNotNull("Should find service even with empty cache", address);
        assertEquals("Should return correct address", "127.0.0.1", address.getHostName());
        assertEquals("Should return correct port", 9090, address.getPort());
    }

    @Test
    public void testCheckRetryFunctionality() {
        // Test services that should allow retry
        assertTrue("Service with 'Retry' should allow retry", 
                  serviceCenter.checkRetry("org.example.service.UserServiceRetry"));
        assertTrue("Another retry service should allow retry", 
                  serviceCenter.checkRetry("TestRetryService"));
        
        // Test services that should not allow retry
        assertFalse("Regular service should not allow retry", 
                   serviceCenter.checkRetry("org.example.service.UserService"));
        assertFalse("Null service should not allow retry", 
                   serviceCenter.checkRetry(null));
        assertFalse("Empty service should not allow retry", 
                   serviceCenter.checkRetry(""));
    }

    @Test
    public void testServiceDiscoveryWithMultipleAddresses() {
        String serviceName = "org.example.service.MultiService";
        List<String> multipleAddresses = Arrays.asList(
            "10.0.0.1:8080", 
            "10.0.0.2:8080", 
            "10.0.0.3:8080"
        );
        
        when(mockServiceCache.getServiceFromCache(serviceName)).thenReturn(multipleAddresses);
        
        InetSocketAddress address = serviceCenter.serviceDiscovery(serviceName);
        
        assertNotNull("Should return an address from multiple options", address);
        assertEquals("Should return first address", "10.0.0.1", address.getHostName());
        assertEquals("Should return correct port", 8080, address.getPort());
    }

    @Test
    public void testServiceDiscoveryPerformance() {
        String serviceName = "org.example.service.PerformanceTest";
        List<String> addresses = Arrays.asList("127.0.0.1:8080");
        
        when(mockServiceCache.getServiceFromCache(serviceName)).thenReturn(addresses);
        
        long startTime = System.currentTimeMillis();
        
        // Perform multiple service discoveries
        for (int i = 0; i < 1000; i++) {
            InetSocketAddress address = serviceCenter.serviceDiscovery(serviceName);
            assertNotNull("Should consistently return address", address);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue("Service discovery should be fast (< 1000ms for 1000 calls)", duration < 1000);
        
        // Verify cache was called for each discovery
        verify(mockServiceCache, times(1000)).getServiceFromCache(serviceName);
    }

    @Test
    public void testServiceDiscoveryWithInvalidAddressFormat() {
        String serviceName = "org.example.service.InvalidAddressService";
        List<String> invalidAddresses = Arrays.asList("invalid-address", "127.0.0.1", "127.0.0.1:invalid-port");
        
        when(mockServiceCache.getServiceFromCache(serviceName)).thenReturn(invalidAddresses);
        
        try {
            InetSocketAddress address = serviceCenter.serviceDiscovery(serviceName);
            // The mock implementation should handle this gracefully
            // In a real implementation, you might want to filter out invalid addresses
            assertNotNull("Should handle invalid addresses gracefully", address);
        } catch (Exception e) {
            // If an exception is thrown, it should be a reasonable exception
            assertTrue("Exception should be related to address parsing", 
                      e.getMessage().contains("address") || e.getMessage().contains("port"));
        }
    }
}