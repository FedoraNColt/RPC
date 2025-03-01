package Client.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceCache {
    // Stores service names and their corresponding address lists
    private static Map<String, List<String>> cache = new HashMap<>();

    /**
     * Adds a service and its address to the local cache.
     *
     * @param serviceName The name of the service.
     * @param address The address of the service instance.
     */
    public void addServiceToCache(String serviceName, String address) {
        if (cache.containsKey(serviceName)) {
            List<String> addressList = cache.get(serviceName);
            addressList.add(address);
        } else {
            List<String> addressList = new ArrayList<>();
            addressList.add(address);
            cache.put(serviceName, addressList);
        }
        System.out.println("Service " + serviceName + " with address of " + address +" added to the local cache");
    }

    /**
     * Replaces an old service address with a new one in the cache.
     *
     * @param serviceName The name of the service.
     * @param oldAddress The existing address to be replaced.
     * @param newAddress The new address to be added.
     */
    public void replaceServiceAddress(String serviceName, String oldAddress, String newAddress) {
        if (cache.containsKey(serviceName)) {
            List<String> addressList = cache.get(serviceName);
            addressList.remove(oldAddress);
            addressList.add(newAddress);
        } else {
            System.out.println("Update failed: Service does not exist.");
        }
    }

    /**
     * Retrieves a list of service addresses from the cache.
     *
     * @param serviceName The name of the service.
     * @return A list of service addresses, or null if the service is not found.
     */
    public List<String> getServiceFromCache(String serviceName) {
        if (!cache.containsKey(serviceName)) return null;
        return cache.get(serviceName);
    }


    /**
     * Removes a service address from the local cache.
     *
     * @param serviceName The name of the service.
     * @param address The address to be removed.
     */
    public void removeServiceAddress(String serviceName, String address) {
        if (cache.containsKey(serviceName)) {
            List<String> addressList = cache.get(serviceName);
            if (addressList.contains(address)) {
                addressList.remove(address);
                System.out.println("Removed service: " + serviceName + " with address: " + address + " from the local cache.");
            } else {
                System.out.println("Remove Failed: Service address doesn't exist.");
            }
        } else {
            System.out.println("Remove Failed: Service name doesn't exist.");
        }
    }
}
