package org.example.server.provider;

import org.example.server.ratelimit.provider.RateLimitProvider;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ServiceProviderTest {

    private ServiceProvider serviceProvider;
    private TestService testService;

    public interface TestService {
        String testMethod(String input);
        int calculateValue(int a, int b);
    }

    public static class TestServiceImpl implements TestService {
        @Override
        public String testMethod(String input) {
            return "processed: " + input;
        }

        @Override
        public int calculateValue(int a, int b) {
            return a + b;
        }
    }

    @Before
    public void setUp() {
        serviceProvider = new ServiceProvider("localhost", 8080);
        testService = new TestServiceImpl();
    }

    @Test
    public void testProvideServiceInterface() {
        // Provide the service interface
        serviceProvider.provideServiceInterface(testService, true);
        
        // Verify the service is registered
        String interfaceName = TestService.class.getName();
        Object registeredService = serviceProvider.getService(interfaceName);
        
        assertNotNull("Service should be registered", registeredService);
        assertEquals("Service should be the same instance", testService, registeredService);
        assertTrue("Service should implement the interface", registeredService instanceof TestService);
    }

    @Test
    public void testGetServiceNotFound() {
        String nonExistentInterface = "com.example.NonExistentService";
        Object service = serviceProvider.getService(nonExistentInterface);
        
        assertNull("Non-existent service should return null", service);
    }

    @Test
    public void testMultipleServiceInterfaces() {
        // Create a service that implements multiple interfaces
        MultiInterfaceService multiService = new MultiInterfaceService();
        serviceProvider.provideServiceInterface(multiService, false);
        
        // Check both interfaces are registered
        Object service1 = serviceProvider.getService(TestService.class.getName());
        Object service2 = serviceProvider.getService(AnotherTestService.class.getName());
        
        assertNotNull("First interface should be registered", service1);
        assertNotNull("Second interface should be registered", service2);
        assertEquals("Both should point to same instance", service1, service2);
    }

    @Test
    public void testRateLimitProvider() {
        RateLimitProvider rateLimitProvider = serviceProvider.getRateLimitProvider();
        
        assertNotNull("Rate limit provider should not be null", rateLimitProvider);
        
        // Test getting rate limiter for a service
        String serviceName = "org.example.TestService";
        assertNotNull("Rate limiter should be created for service", 
                     rateLimitProvider.getRateLimit(serviceName));
    }

    @Test
    public void testServiceProviderWithDifferentPorts() {
        ServiceProvider provider1 = new ServiceProvider("localhost", 8080);
        ServiceProvider provider2 = new ServiceProvider("localhost", 8081);
        
        assertNotNull("Provider 1 should be created", provider1);
        assertNotNull("Provider 2 should be created", provider2);
        assertNotNull("Provider 1 should have rate limit provider", provider1.getRateLimitProvider());
        assertNotNull("Provider 2 should have rate limit provider", provider2.getRateLimitProvider());
    }

    // Additional test interfaces and implementations
    public interface AnotherTestService {
        void doSomething();
    }

    public static class MultiInterfaceService implements TestService, AnotherTestService {
        @Override
        public String testMethod(String input) {
            return "multi: " + input;
        }

        @Override
        public int calculateValue(int a, int b) {
            return a * b;
        }

        @Override
        public void doSomething() {
            // Implementation
        }
    }
}