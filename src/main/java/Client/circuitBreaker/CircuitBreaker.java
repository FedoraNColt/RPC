package Client.circuitBreaker;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreaker {
    @Getter
    private CircuitBreakerState state = CircuitBreakerState.CLOSED; // Initial state of the circuit breaker: CLOSED
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger requestCount = new AtomicInteger(0);

    private final int failureThreshold;
    private final double halfOpenSuccessRate;
    private final long retryTimePeriod;
    private long lastFailureTime = 0; // Timestamp of the last failure

    /**
     * Constructor to initialize the circuit breaker.
     *
     * @param failureThreshold    The maximum number of failures allowed before opening the circuit.
     * @param halfOpenSuccessRate The required success rate for transitioning from HALF_OPEN to CLOSED.
     * @param retryTimePeriod     The time period (in milliseconds) before retrying after the circuit is OPEN.
     */
    public CircuitBreaker(int failureThreshold, double halfOpenSuccessRate, long retryTimePeriod) {
        this.failureThreshold = failureThreshold;
        this.halfOpenSuccessRate = halfOpenSuccessRate;
        this.retryTimePeriod = retryTimePeriod;
    }

    /**
     * Determines whether a request is allowed based on the current circuit breaker state.
     *
     * @return true if the request is allowed, false otherwise.
     */
    public synchronized boolean allowRequest() {
        long currentTime = System.currentTimeMillis();
        System.out.println("Before circuit breaker switch. Current failure count: " + failureCount);
        switch (state) {
            case OPEN:
                if (currentTime - lastFailureTime > retryTimePeriod) {
                    // If the reset time period has elapsed, transition to HALF_OPEN and allow a request
                    state = CircuitBreakerState.HALF_OPEN;
                    resetCounts();
                    return true;
                }
                System.out.println("Circuit breaker activated.");
                return false;
            case HALF_OPEN:
                requestCount.incrementAndGet();
                return true;
            default:
                return true;
        }
    }

    /**
     * Records a successful request.
     * In HALF_OPEN state, it tracks success count and determines if the circuit should close.
     */
    public synchronized void recordSuccess() {
        if (state == CircuitBreakerState.HALF_OPEN) {
            successCount.incrementAndGet();
            // If success rate meets or exceeds the threshold, transition to CLOSED state
            if (successCount.get() >= halfOpenSuccessRate * requestCount.get()) {
                state = CircuitBreakerState.CLOSED; // Restore normal operation
                resetCounts();
            }
        } else {
            resetCounts();
        }
    }

    /**
     * Records a failed request.
     * If failures exceed the threshold, the circuit transitions to OPEN state.
     */
    public synchronized void recordFailure() {
        lastFailureTime = System.currentTimeMillis();// Record failure timestamp
        failureCount.incrementAndGet(); // Increment failure count
        System.out.println("Failure recorded. Current failure count: " + failureCount);
        if (state == CircuitBreakerState.HALF_OPEN) {
            // If a failure occurs in HALF_OPEN, transition to OPEN
            state = CircuitBreakerState.OPEN;
        } else if (failureCount.get() >= failureThreshold) {
            // If failures exceed the threshold, transition to OPEN
            state = CircuitBreakerState.OPEN;
        }
    }

    /**
     * Resets all counters.
     */
    private void resetCounts() {
        failureCount.set(0);
        successCount.set(0);
        requestCount.set(0);
    }
}
