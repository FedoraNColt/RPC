package org.example.common.message;

import org.junit.Test;
import static org.junit.Assert.*;

public class RPCRequestTest {

    @Test
    public void testRPCRequestBuilder() {
        String interfaceName = "org.example.service.UserService";
        String methodName = "getUserByUserId";
        Object[] params = {123};
        Class<?>[] paramTypes = {Integer.class};

        RPCRequest request = RPCRequest.builder()
                .interfaceName(interfaceName)
                .methodName(methodName)
                .params(params)
                .paramTypes(paramTypes)
                .build();

        assertEquals(interfaceName, request.getInterfaceName());
        assertEquals(methodName, request.getMethodName());
        assertArrayEquals(params, request.getParams());
        assertArrayEquals(paramTypes, request.getParamTypes());
    }

    @Test
    public void testRPCRequestConstructor() {
        String interfaceName = "org.example.service.UserService";
        String methodName = "insertUserId";
        Object[] params = {new Object()};
        Class<?>[] paramTypes = {Object.class};

        RPCRequest request = new RPCRequest(interfaceName, methodName, params, paramTypes);

        assertEquals(interfaceName, request.getInterfaceName());
        assertEquals(methodName, request.getMethodName());
        assertArrayEquals(params, request.getParams());
        assertArrayEquals(paramTypes, request.getParamTypes());
    }

    @Test
    public void testRPCRequestDefaultConstructor() {
        RPCRequest request = new RPCRequest();
        
        assertNull(request.getInterfaceName());
        assertNull(request.getMethodName());
        assertNull(request.getParams());
        assertNull(request.getParamTypes());
    }

    @Test
    public void testRPCRequestSetters() {
        RPCRequest request = new RPCRequest();
        
        String interfaceName = "org.example.service.TestService";
        String methodName = "testMethod";
        Object[] params = {"test"};
        Class<?>[] paramTypes = {String.class};

        request.setInterfaceName(interfaceName);
        request.setMethodName(methodName);
        request.setParams(params);
        request.setParamTypes(paramTypes);

        assertEquals(interfaceName, request.getInterfaceName());
        assertEquals(methodName, request.getMethodName());
        assertArrayEquals(params, request.getParams());
        assertArrayEquals(paramTypes, request.getParamTypes());
    }
}