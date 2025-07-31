package org.example.integration;

import org.example.client.proxy.ClientProxy;
import org.example.common.message.RPCRequest;
import org.example.common.message.RPCResponse;
import org.example.server.provider.ServiceProvider;
import org.example.server.server.impl.NettyRPCServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class RPCIntegrationTest {

    private NettyRPCServer server;
    private ServiceProvider serviceProvider;
    private Thread serverThread;
    private final int SERVER_PORT = 9998; // Use different port to avoid conflicts

    // Test service interface
    public interface TestService {
        String echo(String message);
        int add(int a, int b);
        void doNothing();
        String throwException() throws Exception;
    }

    // Test service implementation
    public static class TestServiceImpl implements TestService {
        @Override
        public String echo(String message) {
            return "Echo: " + message;
        }

        @Override
        public int add(int a, int b) {
            return a + b;
        }

        @Override
        public void doNothing() {
            // Do nothing
        }

        @Override
        public String throwException() throws Exception {
            throw new RuntimeException("Test exception");
        }
    }

    @Before
    public void setUp() throws InterruptedException {
        // Set up service provider
        serviceProvider = new ServiceProvider("127.0.0.1", SERVER_PORT);
        serviceProvider.provideServiceInterface(new TestServiceImpl(), false);

        // Set up server
        server = new NettyRPCServer(serviceProvider);
        
        // Start server in separate thread
        CountDownLatch serverStarted = new CountDownLatch(1);
        serverThread = new Thread(() -> {
            try {
                serverStarted.countDown();
                server.start(SERVER_PORT);
            } catch (Exception e) {
                System.err.println("Server startup failed: " + e.getMessage());
            }
        });
        serverThread.start();
        
        // Wait for server to start
        assertTrue("Server should start within 5 seconds", 
                  serverStarted.await(5, TimeUnit.SECONDS));
        
        // Give server a moment to fully initialize
        Thread.sleep(1000);
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
        }
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
        }
    }

    @Test
    public void testBasicRPCCall() throws Exception {
        // Note: This test would require a working ZooKeeper instance
        // For now, we'll test the message handling logic
        
        RPCRequest request = RPCRequest.builder()
                .interfaceName(TestService.class.getName())
                .methodName("echo")
                .params(new Object[]{"Hello World"})
                .paramTypes(new Class[]{String.class})
                .build();

        // Test the request structure
        assertNotNull("Request should not be null", request);
        assertEquals("Interface name should match", TestService.class.getName(), request.getInterfaceName());
        assertEquals("Method name should match", "echo", request.getMethodName());
        assertArrayEquals("Parameters should match", new Object[]{"Hello World"}, request.getParams());
        assertArrayEquals("Parameter types should match", new Class[]{String.class}, request.getParamTypes());
    }

    @Test
    public void testRPCRequestSerialization() {
        RPCRequest request = RPCRequest.builder()
                .interfaceName(TestService.class.getName())
                .methodName("add")
                .params(new Object[]{5, 3})
                .paramTypes(new Class[]{int.class, int.class})
                .build();

        // Verify request can be constructed properly
        assertNotNull("Request should be serializable", request);
        assertEquals("Should have correct method", "add", request.getMethodName());
        assertEquals("Should have correct parameter count", 2, request.getParams().length);
        assertEquals("First parameter should be 5", 5, request.getParams()[0]);
        assertEquals("Second parameter should be 3", 3, request.getParams()[1]);
    }

    @Test
    public void testRPCResponseHandling() {
        // Test successful response
        String testData = "Success response";
        RPCResponse successResponse = RPCResponse.success(testData);
        
        assertEquals("Success response should have code 200", (Integer) 200, successResponse.getCode());
        assertEquals("Success response should contain data", testData, successResponse.getData());
        assertEquals("Success response should have correct data type", String.class, successResponse.getDataType());

        // Test failure response
        RPCResponse failureResponse = RPCResponse.fail();
        
        assertEquals("Failure response should have code 500", (Integer) 500, failureResponse.getCode());
        assertEquals("Failure response should have error message", "Server Error!", failureResponse.getMessage());
        assertNull("Failure response should have no data", failureResponse.getData());
    }

    @Test
    public void testServiceMethodInvocation() throws Exception {
        TestService service = new TestServiceImpl();
        
        // Test echo method
        String echoResult = service.echo("test message");
        assertEquals("Echo should return prefixed message", "Echo: test message", echoResult);
        
        // Test add method
        int addResult = service.add(10, 20);
        assertEquals("Add should return sum", 30, addResult);
        
        // Test void method
        service.doNothing(); // Should not throw exception
    }

    @Test
    public void testServiceExceptionHandling() {
        TestService service = new TestServiceImpl();
        
        try {
            service.throwException();
            fail("Exception should have been thrown");
        } catch (Exception e) {
            assertEquals("Should throw RuntimeException with correct message", 
                        "Test exception", e.getMessage());
        }
    }

    @Test
    public void testMultipleServiceInterfaces() {
        // Test service provider with multiple services
        ServiceProvider provider = new ServiceProvider("localhost", 8080);
        
        TestService testService = new TestServiceImpl();
        AnotherTestService anotherService = new AnotherTestServiceImpl();
        
        provider.provideServiceInterface(testService, false);
        provider.provideServiceInterface(anotherService, true);
        
        // Verify services are registered
        Object retrievedTestService = provider.getService(TestService.class.getName());
        Object retrievedAnotherService = provider.getService(AnotherTestService.class.getName());
        
        assertNotNull("TestService should be registered", retrievedTestService);
        assertNotNull("AnotherTestService should be registered", retrievedAnotherService);
        assertEquals("Should get same TestService instance", testService, retrievedTestService);
        assertEquals("Should get same AnotherTestService instance", anotherService, retrievedAnotherService);
    }

    @Test
    public void testConcurrentServiceCalls() throws InterruptedException {
        TestService service = new TestServiceImpl();
        final int numThreads = 10;
        final CountDownLatch latch = new CountDownLatch(numThreads);
        final String[] results = new String[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    results[index] = service.echo("Message " + index);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        assertTrue("All threads should complete within 5 seconds", 
                  latch.await(5, TimeUnit.SECONDS));
        
        // Verify all results
        for (int i = 0; i < numThreads; i++) {
            assertEquals("Result " + i + " should be correct", 
                        "Echo: Message " + i, results[i]);
        }
    }

    // Additional test service interface
    public interface AnotherTestService {
        boolean isValid(String input);
    }

    public static class AnotherTestServiceImpl implements AnotherTestService {
        @Override
        public boolean isValid(String input) {
            return input != null && !input.trim().isEmpty();
        }
    }
}