package com.plushnode.atlacore.protection;

public class PluginNotFoundException extends Exception {
    public PluginNotFoundException(String pluginName) {
        super(pluginName + " not found in plugin manager.");
    }
}
