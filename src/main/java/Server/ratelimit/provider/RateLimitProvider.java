package Server.ratelimit.provider;

import Server.ratelimit.RateLimit;
import Server.ratelimit.impl.TokenBucketRateLimitImpl;

import java.util.HashMap;
import java.util.Map;

public class RateLimitProvider {
    // Stores a mapping between interface names and their corresponding rate limiter instances
    private final Map<String, RateLimit> rateLimits = new HashMap<>();

    /**
     * Retrieves the rate limiter instance associated with the given interface name.
     * If no instance exists for the specified interface, a new one is created and returned.
     *
     * @param interfaceName The name of the interface
     * @return The rate limiter instance corresponding to the interface
     */
    public RateLimit getRateLimit(String interfaceName) {
        if (!rateLimits.containsKey(interfaceName)) {
            RateLimit rateLimit = new TokenBucketRateLimitImpl(100, 10);
            rateLimits.put(interfaceName, rateLimit);
            return rateLimit;
        }
        return rateLimits.get(interfaceName);
    }
}
