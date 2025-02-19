package org.example.Server.provider;

import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {
    private Map<String, Object> interfaceProvider;

    public ServiceProvider() {
        interfaceProvider = new HashMap<>();
    }

    public void provideServiceInterface(Object service) {
        String serviceName = service.getClass().getName();
        Class<?>[] interfaceNames = service.getClass().getInterfaces();

        for (Class<?> interfaceName : interfaceNames) {
            interfaceProvider.put(interfaceName.getName(), service);
        }
    }

    public Object getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }
}
