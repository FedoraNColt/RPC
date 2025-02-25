package Client.serviceCentre.watcher;

import Client.cache.ServiceCache;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

import java.util.Arrays;

public class ZKwatcher {
    // Zookeeper client
    private final CuratorFramework client;
    // Local service cache
    private final ServiceCache serviceCache;

    /**
     * Constructor to initialize the Zookeeper watcher.
     *
     * @param client The Zookeeper client.
     * @param serviceCache The local cache for storing and managing service addresses.
     */
    public ZKwatcher(CuratorFramework client, ServiceCache serviceCache) {
        this.client = client;
        this.serviceCache = serviceCache;
    }

    /**
     * Watches for updates to the specified Zookeeper node.
     * This method listens for changes such as node creation, updates, and deletions.
     *
     * @param path The path of the Zookeeper node to watch.
     * @throws InterruptedException If the thread is interrupted while setting up the watcher.
     */
    public void watchToUpdate(String path) throws InterruptedException {
        // Used to monitor node changes under the specified path and update the local cache when changes occur.
        // CuratorCache is an API provided by Curator for conveniently monitoring node changes.
        // It listens for changes in the specified path, and in this case, it is monitoring the root path.
        CuratorCache curatorCache = CuratorCache.build(client, "/");
        // Registers a listener to handle node events such as creation, update, and deletion
        curatorCache.listenable().addListener(new CuratorCacheListener() {

            /**
             * Handles node events and updates the local cache accordingly.
             *
             * @param type       The event type (`NODE_CREATED`, `NODE_CHANGED`, `NODE_DELETED`).
             * @param childData  The previous data of the node (null for `NODE_CREATED`).
             * @param childData1 The updated data of the node (null for `NODE_DELETED`).
             */
            @Override
            public void event(Type type, ChildData childData, ChildData childData1) {
                switch (type.name()) {
                    // When the watcher is first established, it may detect pre-existing nodes and trigger this event
                    case "NODE_CREATED":
                        String[] pathArray = parsePath(childData1);
                        if (pathArray.length > 2) {
                            String serviceName = pathArray[1];
                            String address = pathArray[2];
                            serviceCache.addServiceToCache(serviceName, address);
                        }
                        break;
                    case "NODE_CHANGE":
                        if (childData.getData() != null) {
                            System.out.println("Previous Data: " + Arrays.toString(childData.getData()));
                        } else {
                            System.out.println("No Previous Data exists");
                        }
                        String[] oldPathArray = parsePath(childData);
                        String[] newPathArray = parsePath(childData1);
                        serviceCache.replaceServiceAddress(oldPathArray[1], oldPathArray[2], newPathArray[2]);
                        System.out.println("Updated Data: " + Arrays.toString(childData1.getData()));
                        break;
                    case "NODE_DELETED":
                        String[] deletedPathArray = parsePath(childData);
                        if (deletedPathArray.length > 2) {
                            String serviceName = deletedPathArray[1];
                            String address = deletedPathArray[2];
                            serviceCache.removeServiceAddress(serviceName, address);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        // Starts listening for events
        curatorCache.start();
    }

    /**
     * Parses the full path of a node and extracts its components.
     *
     * @param childData The node data containing the path.
     * @return An array of path components split by "/".
     */
    private String[] parsePath(ChildData childData) {
        String path = new String(childData.getData());
        return path.split("/");
    }
}
