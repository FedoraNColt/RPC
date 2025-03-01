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
        System.out.println("Before circuit breaker switch. Current failure count: " + failureCount.get());
        switch (state) {
            case OPEN:
                if (currentTime - lastFailureTime > retryTimePeriod) {
                    System.out.println("Retry period elapsed. Transitioning from OPEN to HALF_OPEN state.");
                    state = CircuitBreakerState.HALF_OPEN;
                    resetCounts();
                    return true;
                }
                System.out.println("Circuit breaker is OPEN. Request denied.");
                return false;
            case HALF_OPEN:
                requestCount.incrementAndGet();
                System.out.println("HALF_OPEN state: Allowing limited request. Request count: " + requestCount.get());
                return true;
            case CLOSED:
                return true;
            default:
                throw new IllegalStateException("Unexpected CircuitBreaker state: " + state);
        }
    }

    /**
     * Records a successful request.
     * In HALF_OPEN state, it tracks success count and determines if the circuit should close.
     */
    public synchronized void recordSuccess() {
        if (state == CircuitBreakerState.HALF_OPEN) {
            successCount.incrementAndGet();
            System.out.println("HALF_OPEN state: Recorded success.\nSuccess count: " + successCount.get() +
                    " out of " + requestCount.get() + " requests.");
            // If success rate meets or exceeds the threshold, transition to CLOSED state
            if (successCount.get() >= halfOpenSuccessRate * requestCount.get()) {
                System.out.println("Success threshold met. Transitioning to CLOSED state.");
                state = CircuitBreakerState.CLOSED; // Restore normal operation
                resetCounts();
            }
        } else if (state == CircuitBreakerState.CLOSED) {
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
     * Records a response code.
     * 200 is considered a success and 500 a failure.
     *
     * @param code Response code to record.
     */
    public void record(int code) {
        switch (code) {
            case 200:
                recordSuccess();
                break;
            case 500:
                recordFailure();
                break;
            default:
                System.out.println("Unrecognized response code: " + code);
                break;
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
