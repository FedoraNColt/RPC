package Server.provider;

import Server.serviceRegister.ServiceRegister;
import Server.serviceRegister.impl.ZKServiceRegister;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {
    private Map<String, Object> interfaceProvider;
    private String host;
    private int port;
    private ServiceRegister serviceRegister;

    public ServiceProvider(String host, int port) {
        this.host = host;
        this.port = port;
        serviceRegister = new ZKServiceRegister();
        interfaceProvider = new HashMap<>();
    }

    public void provideServiceInterface(Object service) {
        Class<?>[] interfaceNames = service.getClass().getInterfaces();

        for (Class<?> clazz : interfaceNames) {
            interfaceProvider.put(clazz.getName(), service);
            serviceRegister.register(clazz.getName(), new InetSocketAddress(host, port));
        }
    }

    public Object getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }
}
