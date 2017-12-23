package com.plushnode.atlacore.protection;

import java.util.HashMap;
import java.util.Map;

public class ProtectionFactory {
    private Map<String, MethodCreator> creators = new HashMap<>();

    public ProtectionFactory() {

    }

    public void registerProtectMethod(String method, MethodCreator creator) {
        this.creators.put(method.toLowerCase(), creator);
    }

    public ProtectMethod getProtectMethod(String name) throws PluginNotFoundException {
        MethodCreator creator = creators.get(name.toLowerCase());
        ProtectMethod method = null;

        if (creator != null)
            method = creator.create();

        return method;
    }

    public interface MethodCreator {
        ProtectMethod create() throws PluginNotFoundException;
    }
}
