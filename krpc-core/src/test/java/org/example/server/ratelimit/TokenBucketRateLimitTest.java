package org.example.server.ratelimit;

import org.example.server.ratelimit.impl.TokenBucketRateLimitImpl;
import org.junit.Test;
import static org.junit.Assert.*;

public class TokenBucketRateLimitTest {

    @Test
    public void testTokenBucketBasicFunctionality() {
        // Create a token bucket with 5 tokens, refilling 2 tokens per second
        TokenBucketRateLimitImpl rateLimit = new TokenBucketRateLimitImpl(5, 2);
        
        // Should be able to get tokens initially
        assertTrue("Should get token from full bucket", rateLimit.getToken());
        assertTrue("Should get second token", rateLimit.getToken());
        assertTrue("Should get third token", rateLimit.getToken());
        assertTrue("Should get fourth token", rateLimit.getToken());
        assertTrue("Should get fifth token", rateLimit.getToken());
        
        // Bucket should now be empty
        assertFalse("Should not get token from empty bucket", rateLimit.getToken());
        assertFalse("Should still not get token", rateLimit.getToken());
    }

    @Test
    public void testTokenBucketRefill() throws InterruptedException {
        // Create a token bucket with 2 tokens, refilling 1 token per 100ms (10 per second)
        TokenBucketRateLimitImpl rateLimit = new TokenBucketRateLimitImpl(2, 10);
        
        // Drain the bucket
        assertTrue("Should get first token", rateLimit.getToken());
        assertTrue("Should get second token", rateLimit.getToken());
        assertFalse("Should not get token from empty bucket", rateLimit.getToken());
        
        // Wait for refill (slightly more than 100ms)
        Thread.sleep(150);
        
        // Should be able to get a token after refill
        assertTrue("Should get token after refill", rateLimit.getToken());
        
        // But not immediately a second one
        assertFalse("Should not get second token immediately", rateLimit.getToken());
    }

    @Test
    public void testTokenBucketMaxCapacity() throws InterruptedException {
        // Create a token bucket with 3 tokens, refilling 10 tokens per second
        TokenBucketRateLimitImpl rateLimit = new TokenBucketRateLimitImpl(3, 10);
        
        // Drain one token
        assertTrue("Should get one token", rateLimit.getToken());
        
        // Wait for more than enough time to refill
        Thread.sleep(500);
        
        // Should still only have max capacity (3 tokens)
        assertTrue("Should get first token", rateLimit.getToken());
        assertTrue("Should get second token", rateLimit.getToken());
        assertTrue("Should get third token", rateLimit.getToken());
        assertFalse("Should not get fourth token (exceeds capacity)", rateLimit.getToken());
    }

    @Test
    public void testTokenBucketConcurrency() throws InterruptedException {
        TokenBucketRateLimitImpl rateLimit = new TokenBucketRateLimitImpl(10, 5);
        final int numThreads = 5;
        final int tokensPerThread = 3;
        
        Thread[] threads = new Thread[numThreads];
        final boolean[] results = new boolean[numThreads * tokensPerThread];
        
        for (int i = 0; i < numThreads; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < tokensPerThread; j++) {
                    results[threadIndex * tokensPerThread + j] = rateLimit.getToken();
                }
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
        
        // Count successful token acquisitions
        int successCount = 0;
        for (boolean result : results) {
            if (result) successCount++;
        }
        
        // Should have gotten exactly 10 tokens (the initial capacity)
        assertEquals("Should get exactly initial capacity tokens", 10, successCount);
    }

    @Test
    public void testTokenBucketZeroCapacity() {
        TokenBucketRateLimitImpl rateLimit = new TokenBucketRateLimitImpl(0, 1);
        
        // Should never get a token with zero capacity
        assertFalse("Should not get token with zero capacity", rateLimit.getToken());
        assertFalse("Should still not get token", rateLimit.getToken());
    }

    @Test
    public void testTokenBucketHighRefillRate() throws InterruptedException {
        // High refill rate: 100 tokens per second
        TokenBucketRateLimitImpl rateLimit = new TokenBucketRateLimitImpl(5, 100);
        
        // Drain the bucket
        for (int i = 0; i < 5; i++) {
            assertTrue("Should get token " + i, rateLimit.getToken());
        }
        assertFalse("Should not get token from empty bucket", rateLimit.getToken());
        
        // Wait a short time (20ms should add ~2 tokens)
        Thread.sleep(20);
        
        // Should be able to get tokens quickly due to high refill rate
        assertTrue("Should get token after short wait", rateLimit.getToken());
    }

    @Test
    public void testTokenBucketEdgeCases() {
        // Test with capacity 1 and very slow refill
        TokenBucketRateLimitImpl slowRateLimit = new TokenBucketRateLimitImpl(1, 1);
        
        assertTrue("Should get the only token", slowRateLimit.getToken());
        assertFalse("Should not get second token", slowRateLimit.getToken());
        
        // Test with large capacity
        TokenBucketRateLimitImpl largeRateLimit = new TokenBucketRateLimitImpl(1000, 1);
        
        // Should be able to get many tokens initially
        for (int i = 0; i < 1000; i++) {
            assertTrue("Should get token " + i, largeRateLimit.getToken());
        }
        assertFalse("Should not get token beyond capacity", largeRateLimit.getToken());
    }
}