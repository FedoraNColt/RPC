package org.example.client.circuitBreaker;

import org.junit.Test;
import static org.junit.Assert.*;

public class CircuitBreakerTest {

    @Test
    public void testCircuitBreakerBasicFlow() {
        // Create a circuit breaker with failure threshold 2, half-open success rate 0.5, retry time 1000ms
        CircuitBreaker circuitBreaker = new CircuitBreaker(2, 0.5, 1000);
        
        // Initial state should be CLOSED
        assertEquals(CircuitBreakerState.CLOSED, circuitBreaker.getState());
        
        // Should allow requests in CLOSED state
        assertTrue(circuitBreaker.allowRequest());
        
        // Record one failure
        circuitBreaker.recordFailure();
        assertEquals(CircuitBreakerState.CLOSED, circuitBreaker.getState());
        
        // Record second failure, should switch to OPEN state
        circuitBreaker.recordFailure();
        assertEquals(CircuitBreakerState.OPEN, circuitBreaker.getState());
        
        // Should reject requests in OPEN state
        assertFalse(circuitBreaker.allowRequest());
    }

    @Test
    public void testCircuitBreakerHalfOpenTransition() throws InterruptedException {
        CircuitBreaker circuitBreaker = new CircuitBreaker(1, 0.5, 100); // 100ms retry time
        
        // Record one failure, switch to OPEN
        circuitBreaker.recordFailure();
        assertEquals(CircuitBreakerState.OPEN, circuitBreaker.getState());
        
        // Wait for retry time
        Thread.sleep(150);
        
        // Should switch to HALF_OPEN state
        assertTrue(circuitBreaker.allowRequest());
        assertEquals(CircuitBreakerState.HALF_OPEN, circuitBreaker.getState());
    }

    @Test
    public void testCircuitBreakerSuccessInHalfOpen() throws InterruptedException {
        CircuitBreaker circuitBreaker = new CircuitBreaker(1, 0.5, 100);
        
        // Trigger OPEN state
        circuitBreaker.recordFailure();
        Thread.sleep(150);
        
        // Enter HALF_OPEN state
        circuitBreaker.allowRequest();
        assertEquals(CircuitBreakerState.HALF_OPEN, circuitBreaker.getState());
        
        // Record success, should return to CLOSED state
        circuitBreaker.recordSuccess();
        assertEquals(CircuitBreakerState.CLOSED, circuitBreaker.getState());
    }

    @Test
    public void testCircuitBreakerFailureInHalfOpen() throws InterruptedException {
        CircuitBreaker circuitBreaker = new CircuitBreaker(1, 0.5, 100);
        
        // Trigger OPEN state
        circuitBreaker.recordFailure();
        Thread.sleep(150);
        
        // Enter HALF_OPEN state
        circuitBreaker.allowRequest();
        assertEquals(CircuitBreakerState.HALF_OPEN, circuitBreaker.getState());
        
        // Record failure in HALF_OPEN state, should return to OPEN state
        circuitBreaker.recordFailure();
        assertEquals(CircuitBreakerState.OPEN, circuitBreaker.getState());
    }

    @Test
    public void testCircuitBreakerRecordMethod() {
        CircuitBreaker circuitBreaker = new CircuitBreaker(1, 0.5, 1000);
        
        // Record successful response code
        circuitBreaker.record(200);
        assertEquals(CircuitBreakerState.CLOSED, circuitBreaker.getState());
        
        // Record failure response code
        circuitBreaker.record(500);
        assertEquals(CircuitBreakerState.OPEN, circuitBreaker.getState());
    }
} 