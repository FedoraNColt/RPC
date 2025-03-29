package org.example.client.circuitBreaker;

public enum CircuitBreakerState {
    CLOSED,
    OPEN,
    HALF_OPEN,
}
