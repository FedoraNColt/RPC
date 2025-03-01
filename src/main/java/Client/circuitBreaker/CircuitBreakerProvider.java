package Client.circuitBreaker;

import java.util.HashMap;
import java.util.Map;

public class CircuitBreakerProvider {
    private Map<String, CircuitBreaker> circuitBreakerMap = new HashMap<>();

    public synchronized CircuitBreaker getCircuitBreaker(String serviceName) {
        if (!circuitBreakerMap.containsKey(serviceName)) {
            CircuitBreaker circuitBreaker = new CircuitBreaker(1, 0.5, 10000);
            System.out.println("serviceName = " + serviceName + " opens a new circuit breaker");
            circuitBreakerMap.put(serviceName, circuitBreaker);
            return circuitBreaker;
        }
        return circuitBreakerMap.get(serviceName);
    }
}
