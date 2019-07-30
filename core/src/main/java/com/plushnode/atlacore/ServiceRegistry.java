package com.plushnode.atlacore;

import java.util.HashMap;
import java.util.Map;

public class ServiceRegistry {
    // Stores a map from a class type to the service implementation.
    private static Map<Class<? extends Service>, Service> mapping = new HashMap<>();

    // Stops any existing service and starts the new one.
    public static void register(Class<? extends Service> type, Service implementation) {
        Service previous = mapping.put(type, implementation);

        if (previous != null) {
            previous.stop();
        }

        implementation.start();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Service> T get(Class<? extends T> type) {
        return (T)mapping.get(type);
    }
}
