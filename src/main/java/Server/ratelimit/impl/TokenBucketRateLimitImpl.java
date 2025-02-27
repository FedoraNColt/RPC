package Server.ratelimit.impl;

import Server.ratelimit.RateLimit;

public class TokenBucketRateLimitImpl implements RateLimit {
    // Token generation rate (in milliseconds)
    private static int RATE;
    // Maximum capacity of the token bucket
    private static int CAPACITY;
    // Current number of tokens in the bucket
    private volatile int currentCapacity;
    // Timestamp of the last token request
    private volatile long timeStamp = System.currentTimeMillis();

    public TokenBucketRateLimitImpl(int rate, int capacity) {
        RATE = rate;
        CAPACITY = capacity;
        currentCapacity = capacity;
    }

    @Override
    public synchronized boolean getToken() {
        // If there are tokens available, consume one and return true
        if (currentCapacity > 0) {
            currentCapacity--;
            return true;
        }
        // If no tokens are available, attempt to generate new tokens
        long currentTime = System.currentTimeMillis();
        long delta = currentTime - timeStamp;
        // Check if enough time has passed to generate new tokens
        if (delta >= RATE) {
            // If the elapsed time is at least twice the token generation interval (RATE),
            // then generate (elapsed time / RATE) - 1 tokens.
            if (delta / RATE >= 2) {
                currentCapacity += (int) (delta / RATE) - 1;
            }
            // Ensure the token count does not exceed the bucket's maximum capacity
            currentCapacity = Math.min(currentCapacity, CAPACITY);
            // Update the timestamp to the current time
            timeStamp = currentTime;
            return true; // Return true, indicating a token was successfully consumed
        }
        // If not enough time has passed to generate new tokens, return false
        return false;
    }
}
