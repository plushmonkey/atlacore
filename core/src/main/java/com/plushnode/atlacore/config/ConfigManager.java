package com.plushnode.atlacore.config;


import java.util.Observable;

public class ConfigManager extends Observable {
    private static ConfigManager instance;
    private Configuration config;

    private ConfigManager() {

    }

    public static ConfigManager getInstance() {
        if (instance == null)
            instance = new ConfigManager();
        return instance;
    }

    public Configuration getConfig() {
        return config;
    }

    public void onConfigReload() {
        setChanged();
        notifyObservers(config);
    }
}
