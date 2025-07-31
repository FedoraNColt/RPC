package org.example.client.circuitBreaker;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CircuitBreakerProviderTest {

    private CircuitBreakerProvider provider;

    @Before
    public void setUp() {
        provider = new CircuitBreakerProvider();
    }

    @Test
    public void testGetCircuitBreaker() {
        String serviceName = "testService";
        CircuitBreaker circuitBreaker = provider.getCircuitBreaker(serviceName);
        
        assertNotNull("Circuit breaker should not be null", circuitBreaker);
        assertEquals("Initial state should be CLOSED", CircuitBreakerState.CLOSED, circuitBreaker.getState());
    }

    @Test
    public void testGetSameCircuitBreakerForSameService() {
        String serviceName = "testService";
        CircuitBreaker circuitBreaker1 = provider.getCircuitBreaker(serviceName);
        CircuitBreaker circuitBreaker2 = provider.getCircuitBreaker(serviceName);
        
        assertSame("Should return same circuit breaker instance for same service", 
                  circuitBreaker1, circuitBreaker2);
    }

    @Test
    public void testGetDifferentCircuitBreakersForDifferentServices() {
        String serviceName1 = "testService1";
        String serviceName2 = "testService2";
        
        CircuitBreaker circuitBreaker1 = provider.getCircuitBreaker(serviceName1);
        CircuitBreaker circuitBreaker2 = provider.getCircuitBreaker(serviceName2);
        
        assertNotSame("Should return different circuit breaker instances for different services", 
                     circuitBreaker1, circuitBreaker2);
    }

    @Test
    public void testCircuitBreakerIsolation() {
        String serviceName1 = "testService1";
        String serviceName2 = "testService2";
        
        CircuitBreaker circuitBreaker1 = provider.getCircuitBreaker(serviceName1);
        CircuitBreaker circuitBreaker2 = provider.getCircuitBreaker(serviceName2);
        
        // Trigger failure in first circuit breaker
        circuitBreaker1.recordFailure();
        circuitBreaker1.recordFailure(); // This should open the circuit
        
        assertEquals("First circuit breaker should be OPEN", 
                    CircuitBreakerState.OPEN, circuitBreaker1.getState());
        assertEquals("Second circuit breaker should remain CLOSED", 
                    CircuitBreakerState.CLOSED, circuitBreaker2.getState());
        
        // Second circuit breaker should still allow requests
        assertTrue("Second circuit breaker should allow requests", circuitBreaker2.allowRequest());
        assertFalse("First circuit breaker should not allow requests", circuitBreaker1.allowRequest());
    }

    @Test
    public void testCircuitBreakerWithNullServiceName() {
        CircuitBreaker circuitBreaker = provider.getCircuitBreaker(null);
        assertNotNull("Should handle null service name gracefully", circuitBreaker);
    }

    @Test
    public void testCircuitBreakerWithEmptyServiceName() {
        CircuitBreaker circuitBreaker1 = provider.getCircuitBreaker("");
        CircuitBreaker circuitBreaker2 = provider.getCircuitBreaker("");
        
        assertNotNull("Should handle empty service name", circuitBreaker1);
        assertSame("Should return same instance for same empty service name", 
                  circuitBreaker1, circuitBreaker2);
    }

    @Test
    public void testMultipleServicesWithDifferentStates() {
        String[] services = {"service1", "service2", "service3"};
        CircuitBreaker[] breakers = new CircuitBreaker[services.length];
        
        // Get circuit breakers for all services
        for (int i = 0; i < services.length; i++) {
            breakers[i] = provider.getCircuitBreaker(services[i]);
        }
        
        // Set different states
        breakers[0].recordFailure(); // 1 failure, still CLOSED
        breakers[1].recordFailure(); 
        breakers[1].recordFailure(); // 2 failures, should be OPEN
        // breakers[2] remains CLOSED
        
        assertEquals("Service 1 should be CLOSED", CircuitBreakerState.CLOSED, breakers[0].getState());
        assertEquals("Service 2 should be OPEN", CircuitBreakerState.OPEN, breakers[1].getState());
        assertEquals("Service 3 should be CLOSED", CircuitBreakerState.CLOSED, breakers[2].getState());
        
        // Verify request allowance
        assertTrue("Service 1 should allow requests", breakers[0].allowRequest());
        assertFalse("Service 2 should not allow requests", breakers[1].allowRequest());
        assertTrue("Service 3 should allow requests", breakers[2].allowRequest());
    }

    @Test
    public void testCircuitBreakerConcurrency() throws InterruptedException {
        final String serviceName = "concurrentService";
        final int numThreads = 10;
        Thread[] threads = new Thread[numThreads];
        final CircuitBreaker[] breakers = new CircuitBreaker[numThreads];
        
        // Create threads that get circuit breakers concurrently
        for (int i = 0; i < numThreads; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                breakers[index] = provider.getCircuitBreaker(serviceName);
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // All threads should get the same circuit breaker instance
        CircuitBreaker expected = breakers[0];
        for (int i = 1; i < numThreads; i++) {
            assertSame("All threads should get same circuit breaker instance", 
                      expected, breakers[i]);
        }
    }
}