package org.example.common.message;

import org.junit.Test;
import static org.junit.Assert.*;

public class RPCResponseTest {

    @Test
    public void testRPCResponseSuccess() {
        String testData = "Success Result";
        RPCResponse response = RPCResponse.success(testData);

        assertEquals((Integer) 200, response.getCode());
        assertEquals(testData, response.getData());
        assertEquals(String.class, response.getDataType());
        assertNull(response.getMessage());
    }

    @Test
    public void testRPCResponseSuccessWithComplexObject() {
        TestUser user = new TestUser("John", 25);
        RPCResponse response = RPCResponse.success(user);

        assertEquals((Integer) 200, response.getCode());
        assertEquals(user, response.getData());
        assertEquals(TestUser.class, response.getDataType());
        assertNull(response.getMessage());
    }

    @Test
    public void testRPCResponseFail() {
        RPCResponse response = RPCResponse.fail();

        assertEquals((Integer) 500, response.getCode());
        assertEquals("Server Error!", response.getMessage());
        assertNull(response.getData());
        assertNull(response.getDataType());
    }

    @Test
    public void testRPCResponseBuilder() {
        Integer code = 404;
        String message = "Not Found";
        String data = "Error Data";

        RPCResponse response = RPCResponse.builder()
                .code(code)
                .message(message)
                .data(data)
                .dataType(String.class)
                .build();

        assertEquals(code, response.getCode());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
        assertEquals(String.class, response.getDataType());
    }

    @Test
    public void testRPCResponseConstructors() {
        // Test no-args constructor
        RPCResponse response1 = new RPCResponse();
        assertNull(response1.getCode());
        assertNull(response1.getMessage());
        assertNull(response1.getData());
        assertNull(response1.getDataType());

        // Test all-args constructor
        Integer code = 200;
        String message = "OK";
        String data = "Test Data";
        Class<?> dataType = String.class;

        RPCResponse response2 = new RPCResponse(code, message, data, dataType);
        assertEquals(code, response2.getCode());
        assertEquals(message, response2.getMessage());
        assertEquals(data, response2.getData());
        assertEquals(dataType, response2.getDataType());
    }

    @Test
    public void testRPCResponseSetters() {
        RPCResponse response = new RPCResponse();
        
        Integer code = 201;
        String message = "Created";
        String data = "New Resource";
        Class<?> dataType = String.class;

        response.setCode(code);
        response.setMessage(message);  
        response.setData(data);
        response.setDataType(dataType);

        assertEquals(code, response.getCode());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
        assertEquals(dataType, response.getDataType());
    }

    // Test helper class
    private static class TestUser {
        private String name;
        private int age;

        public TestUser(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestUser testUser = (TestUser) obj;
            return age == testUser.age && name.equals(testUser.name);
        }
    }
}