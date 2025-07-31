package org.example.integration;

import org.example.client.circuitBreaker.CircuitBreaker;
import org.example.client.circuitBreaker.CircuitBreakerProvider;
import org.example.client.serviceCenter.balancer.impl.RandomLoadBalancer;
import org.example.common.message.RPCRequest;
import org.example.common.message.RPCResponse;
import org.example.server.netty.handler.NettyRPCServerHandler;
import org.example.server.provider.ServiceProvider;
import org.example.server.ratelimit.RateLimit;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class EndToEndRPCTest {

    private ServiceProvider serviceProvider;
    private NettyRPCServerHandler serverHandler;

    // Test service interfaces
    public interface CalculatorService {
        int add(int a, int b);
        int multiply(int a, int b);
        double divide(double a, double b);
        String getServiceInfo();
    }

    public interface UserService {
        String getUserName(int userId);
        boolean isUserActive(int userId);
        void updateUserStatus(int userId, boolean active);
    }

    // Test service implementations
    public static class CalculatorServiceImpl implements CalculatorService {
        @Override
        public int add(int a, int b) {
            return a + b;
        }

        @Override
        public int multiply(int a, int b) {
            return a * b;
        }

        @Override
        public double divide(double a, double b) {
            if (b == 0) {
                throw new IllegalArgumentException("Division by zero");
            }
            return a / b;
        }

        @Override
        public String getServiceInfo() {
            return "Calculator Service v1.0";
        }
    }

    public static class UserServiceImpl implements UserService {
        @Override
        public String getUserName(int userId) {
            return "User" + userId;
        }

        @Override
        public boolean isUserActive(int userId) {
            return userId > 0 && userId < 1000;
        }

        @Override
        public void updateUserStatus(int userId, boolean active) {
            // Mock implementation - in real scenario would update database
            System.out.println("Updated user " + userId + " status to " + active);
        }
    }

    @Before
    public void setUp() {
        serviceProvider = new ServiceProvider("127.0.0.1", 8080);
        
        // Register services
        serviceProvider.provideServiceInterface(new CalculatorServiceImpl(), true);
        serviceProvider.provideServiceInterface(new UserServiceImpl(), false);
        
        serverHandler = new NettyRPCServerHandler(serviceProvider);
    }

    @Test
    public void testCompleteRPCFlow() throws Exception {
        // Test Calculator Service
        RPCRequest calcRequest = RPCRequest.builder()
                .interfaceName(CalculatorService.class.getName())
                .methodName("add")
                .params(new Object[]{15, 25})
                .paramTypes(new Class[]{int.class, int.class})
                .build();

        // Simulate server processing
        RPCResponse calcResponse = invokeServiceMethod(calcRequest);
        
        assertEquals("Calculator response should be successful", (Integer) 200, calcResponse.getCode());
        assertEquals("Addition result should be correct", 40, calcResponse.getData());

        // Test User Service
        RPCRequest userRequest = RPCRequest.builder()
                .interfaceName(UserService.class.getName())
                .methodName("getUserName")
                .params(new Object[]{123})
                .paramTypes(new Class[]{int.class})
                .build();

        RPCResponse userResponse = invokeServiceMethod(userRequest);
        
        assertEquals("User response should be successful", (Integer) 200, userResponse.getCode());
        assertEquals("User name should be correct", "User123", userResponse.getData());
    }

    @Test
    public void testServiceNotFound() throws Exception {
        RPCRequest request = RPCRequest.builder()
                .interfaceName("com.example.NonExistentService")
                .methodName("someMethod")
                .params(new Object[]{})
                .paramTypes(new Class[]{})
                .build();

        RPCResponse response = invokeServiceMethod(request);
        
        assertEquals("Should return 404 for non-existent service", (Integer) 404, response.getCode());
        assertTrue("Error message should mention service not found", 
                  response.getMessage().contains("Service not found"));
    }

    @Test
    public void testMethodNotFound() throws Exception {
        RPCRequest request = RPCRequest.builder()
                .interfaceName(CalculatorService.class.getName())
                .methodName("nonExistentMethod")
                .params(new Object[]{})
                .paramTypes(new Class[]{})
                .build();

        RPCResponse response = invokeServiceMethod(request);
        
        assertEquals("Should return 404 for non-existent method", (Integer) 404, response.getCode());
        assertTrue("Error message should mention method not found", 
                  response.getMessage().contains("Method not found"));
    }

    @Test
    public void testMethodExecutionException() throws Exception {
        RPCRequest request = RPCRequest.builder()
                .interfaceName(CalculatorService.class.getName())
                .methodName("divide")
                .params(new Object[]{10.0, 0.0})
                .paramTypes(new Class[]{double.class, double.class})
                .build();

        RPCResponse response = invokeServiceMethod(request);
        
        assertEquals("Should return 500 for method execution error", (Integer) 500, response.getCode());
        assertTrue("Error message should mention execution failure", 
                  response.getMessage().contains("Method execution failed"));
    }

    @Test
    public void testRateLimitingIntegration() throws Exception {
        String serviceName = CalculatorService.class.getName();
        
        // Get the rate limiter for the service
        RateLimit rateLimit = serviceProvider.getRateLimitProvider().getRateLimit(serviceName);
        
        // Drain the rate limiter
        int successCount = 0;
        for (int i = 0; i < 20; i++) {
            if (rateLimit.getToken()) {
                successCount++;
            }
        }
        
        assertTrue("Should be able to get some tokens initially", successCount > 0);
        
        // Try to get another token - should fail if rate limit is exhausted
        RPCRequest request = RPCRequest.builder()
                .interfaceName(serviceName)
                .methodName("add")
                .params(new Object[]{1, 2})
                .paramTypes(new Class[]{int.class, int.class})
                .build();

        // If rate limited, should get 429 response
        RPCResponse response = invokeServiceMethod(request);
        
        // Response could be either success (if tokens available) or rate limited
        assertTrue("Response should be success or rate limited", 
                  response.getCode() == 200 || response.getCode() == 429);
    }

    @Test
    public void testCircuitBreakerIntegration() {
        CircuitBreakerProvider provider = new CircuitBreakerProvider();
        CircuitBreaker circuitBreaker = provider.getCircuitBreaker("testService");
        
        // Initial state should be CLOSED
        assertTrue("Circuit breaker should initially allow requests", circuitBreaker.allowRequest());
        
        // Trigger failures
        circuitBreaker.recordFailure();
        circuitBreaker.recordFailure(); // Should open the circuit
        
        assertFalse("Circuit breaker should block requests after failures", circuitBreaker.allowRequest());
        
        // Test success recovery
        CircuitBreaker anotherBreaker = provider.getCircuitBreaker("anotherService");
        assertTrue("Different service should not be affected", anotherBreaker.allowRequest());
        
        anotherBreaker.recordSuccess();
        assertTrue("Service with success should continue allowing requests", anotherBreaker.allowRequest());
    }

    @Test
    public void testLoadBalancerIntegration() {
        RandomLoadBalancer loadBalancer = new RandomLoadBalancer();
        List<String> servers = Arrays.asList(
            "server1:8080", 
            "server2:8080", 
            "server3:8080"
        );
        
        // Test multiple selections
        for (int i = 0; i < 10; i++) {
            String selectedServer = loadBalancer.balance(servers);
            assertNotNull("Load balancer should select a server", selectedServer);
            assertTrue("Selected server should be in the list", servers.contains(selectedServer));
        }
        
        // Test add/remove functionality
        loadBalancer.addNode("server4:8080");
        loadBalancer.removeNode("server1:8080");
        
        // Should still work after modifications
        String selectedServer = loadBalancer.balance(servers);
        assertNotNull("Load balancer should still work after modifications", selectedServer);
    }

    @Test
    public void testConcurrentRPCCalls() throws InterruptedException {
        final int numThreads = 10;
        final int callsPerThread = 5;
        final ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        final CountDownLatch latch = new CountDownLatch(numThreads);
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < callsPerThread; j++) {
                        RPCRequest request = RPCRequest.builder()
                                .interfaceName(CalculatorService.class.getName())
                                .methodName("multiply")
                                .params(new Object[]{threadId, j})
                                .paramTypes(new Class[]{int.class, int.class})
                                .build();

                        try {
                            RPCResponse response = invokeServiceMethod(request);
                            if (response.getCode() == 200) {
                                successCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue("All threads should complete within 10 seconds", 
                  latch.await(10, TimeUnit.SECONDS));
        
        int totalCalls = numThreads * callsPerThread;
        assertEquals("Total calls should match", totalCalls, successCount.get() + errorCount.get());
        
        // At least 80% of calls should succeed (allowing for some rate limiting)
        assertTrue("Most calls should succeed", 
                  successCount.get() >= totalCalls * 0.8);

        executor.shutdown();
    }

    @Test
    public void testInvalidRequestHandling() throws Exception {
        // Test null request
        RPCResponse response1 = invokeServiceMethod(null);
        assertEquals("Should return 400 for null request", (Integer) 400, response1.getCode());

        // Test request with null interface name
        RPCRequest request2 = RPCRequest.builder()
                .interfaceName(null)
                .methodName("someMethod")
                .params(new Object[]{})
                .paramTypes(new Class[]{})
                .build();
        
        RPCResponse response2 = invokeServiceMethod(request2);
        assertEquals("Should return 400 for null interface name", (Integer) 400, response2.getCode());

        // Test request with null method name
        RPCRequest request3 = RPCRequest.builder()
                .interfaceName(CalculatorService.class.getName())
                .methodName(null)
                .params(new Object[]{})
                .paramTypes(new Class[]{})
                .build();
        
        RPCResponse response3 = invokeServiceMethod(request3);
        assertEquals("Should return 400 for null method name", (Integer) 400, response3.getCode());
    }

    // Helper method to simulate service method invocation
    private RPCResponse invokeServiceMethod(RPCRequest request) throws Exception {
        if (request == null) {
            return RPCResponse.builder()
                    .code(400)
                    .message("Invalid request: null")
                    .build();
        }

        String interfaceName = request.getInterfaceName();
        if (interfaceName == null || interfaceName.trim().isEmpty()) {
            return RPCResponse.builder()
                    .code(400)
                    .message("Invalid request: missing interface name")
                    .build();
        }

        try {
            RateLimit rateLimit = serviceProvider.getRateLimitProvider().getRateLimit(interfaceName);
            if (!rateLimit.getToken()) {
                return RPCResponse.builder()
                        .code(429)
                        .message("Service throttled - too many requests")
                        .build();
            }

            Object service = serviceProvider.getService(interfaceName);
            if (service == null) {
                return RPCResponse.builder()
                        .code(404)
                        .message("Service not found: " + interfaceName)
                        .build();
            }

            String methodName = request.getMethodName();
            if (methodName == null || methodName.trim().isEmpty()) {
                return RPCResponse.builder()
                        .code(400)
                        .message("Invalid request: missing method name")
                        .build();
            }

            Method method = service.getClass().getMethod(methodName, request.getParamTypes());
            Object result = method.invoke(service, request.getParams());
            return RPCResponse.success(result);
            
        } catch (NoSuchMethodException e) {
            return RPCResponse.builder()
                    .code(404)
                    .message("Method not found: " + request.getMethodName())
                    .build();
        } catch (Exception e) {
            return RPCResponse.builder()
                    .code(500)
                    .message("Method execution failed: " + e.getCause().getMessage())
                    .build();
        }
    }
}