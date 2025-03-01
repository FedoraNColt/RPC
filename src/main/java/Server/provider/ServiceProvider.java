package Server.provider;

import Server.ratelimit.provider.RateLimitProvider;
import Server.serviceRegister.ServiceRegister;
import Server.serviceRegister.impl.ZKServiceRegister;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {
    private Map<String, Object> interfaceProvider;
    private String host;
    private int port;
    private ServiceRegister serviceRegister;
    @Getter
    private RateLimitProvider rateLimitProvider;

    public ServiceProvider(String host, int port) {
        // Pass in the network address of service provider
        this.host = host;
        this.port = port;
        serviceRegister = new ZKServiceRegister();
        interfaceProvider = new HashMap<>();
        rateLimitProvider = new RateLimitProvider();
    }

    public void provideServiceInterface(Object service, boolean canRetry) {
        // Retrieves all implemented interfaces of the service
        Class<?>[] interfaceNames = service.getClass().getInterfaces();

        for (Class<?> clazz : interfaceNames) {
            // Stores the mapping between interface names and service implementations locally
            interfaceProvider.put(clazz.getName(), service);
            // Registers the service interface in the service registry (e.g., Zookeeper)
            serviceRegister.register(clazz.getName(), new InetSocketAddress(host, port), canRetry);
        }
    }

    public Object getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }
}
